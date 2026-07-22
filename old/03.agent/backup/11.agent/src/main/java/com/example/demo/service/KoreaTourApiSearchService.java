package com.example.demo.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 한국관광공사 TourAPI를 활용한 검색 서비스
 * - 전국의 관광지, 문화시설, 숙박, 음식점 등을 검색
 * - 상세 정보 조회 기능 제공
 * 
 * 학습용으로 단순하게 설계되었지만 모든 핵심 기능을 지원합니다.
 */
@Service
@Primary
@Slf4j
public class KoreaTourApiSearchService implements InternetSearchService {
  
  // ##### 필드 #####
  private final String apiEndpoint;
  private final String apiKey;
  private final WebClient webClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  // API 필수 파라미터
  private static final String MOBILE_OS = "ETC";
  private static final String MOBILE_APP = "TravelAgent";
  private static final String RESPONSE_TYPE = "json";
  
  // 콘텐츠 타입 (관광공사 API 표준)
  private static final String TYPE_ATTRACTION = "12";   // 관광지
  private static final String TYPE_CULTURE = "14";      // 문화시설
  private static final String TYPE_FESTIVAL = "15";     // 축제/행사
  private static final String TYPE_COURSE = "25";       // 여행코스
  private static final String TYPE_LEISURE = "28";      // 레포츠
  private static final String TYPE_ACCOMMODATION = "32"; // 숙박
  private static final String TYPE_SHOPPING = "38";     // 쇼핑
  private static final String TYPE_RESTAURANT = "39";   // 음식점
  
  // ##### 생성자 #####
  public KoreaTourApiSearchService(
      @Value("${korea.tour.api.endpoint}") String apiEndpoint,
      @Value("${korea.tour.api.key}") String apiKey,
      WebClient.Builder webClientBuilder
  ) {
    this.apiEndpoint = apiEndpoint;
    this.apiKey = apiKey;
    this.webClient = webClientBuilder
        .baseUrl(this.apiEndpoint)
        .defaultHeader("Accept", "application/json")
        .build();
    
    log.info("✅ 한국관광공사 TourAPI 검색 서비스 초기화 완료");
    log.info("   - 엔드포인트: {}", apiEndpoint);
  }
  
  // ##### 공개 메서드 (Tool) #####
  
  /**
   * 키워드로 한국 관광지, 맛집, 숙소 등을 검색합니다.
   * 
   * @param query 검색어 (예: "제주도 관광지", "서울 맛집", "부산 호텔")
   * @return 검색 결과 (최대 10개, 상세 정보 포함)
   */
  @Tool(description = "한국 관광지, 맛집, 숙소를 검색합니다. 지역명과 카테고리를 함께 입력하면 더 정확합니다.")
  @Override
  public String search(String query) {
    log.info("🔍 검색 시작: {}", query);
    
    try {
      // 키워드에서 콘텐츠 타입 자동 감지
      String contentTypeId = detectContentType(query);
      
      // API 호출 파라미터 준비
      String[] params;
      if (contentTypeId != null) {
        String badge = getContentTypeBadge(contentTypeId);
        log.info("🎯 타입 감지: {} (ID={})", badge, contentTypeId);
        
        // 숙박은 searchStay2 + areaCode 사용 (keyword 파라미터 미지원)
        if (TYPE_ACCOMMODATION.equals(contentTypeId)) {
          String areaCode = detectAreaCode(query);
          if (areaCode != null) {
            log.info("📍 지역코드 감지: {}", areaCode);
            params = new String[] {
                "areaCode", areaCode,
                "numOfRows", "15",
                "pageNo", "1",
                "arrange", "B"
            };
          } else {
            log.info("📍 지역코드 없음 → 전체 숙박 검색");
            params = new String[] {
                "numOfRows", "15",
                "pageNo", "1",
                "arrange", "B"
            };
          }
          return parseAndFormatResults(callApi("/searchStay2", params), query);
        }

        // 관광지/음식점 등: 지역이 감지되면 areaBasedList2 사용
        // ("제주도 관광지" 같은 자연어는 keyword 검색 시 0건이 나오기 때문)
        String areaCode = detectAreaCode(query);
        if (areaCode != null) {
          log.info("📍 지역코드 감지: {} → areaBasedList2 사용", areaCode);
          params = new String[] {
              "areaCode", areaCode,
              "contentTypeId", contentTypeId,
              "numOfRows", "15",
              "pageNo", "1",
              "arrange", "B"
          };
          return parseAndFormatResults(callApi("/areaBasedList2", params), query);
        }

        // 지역 미감지: 카테고리 단어를 제거한 키워드로 searchKeyword2 + contentTypeId
        params = new String[] {
            "keyword", encodeKeyword(stripCategoryWords(query)),
            "contentTypeId", contentTypeId,
            "numOfRows", "15",
            "pageNo", "1",
            "arrange", "B"
        };
      } else {
        log.info("ℹ️  일반 검색 (타입 미지정)");
        params = new String[] {
            "keyword", encodeKeyword(query),
            "numOfRows", "15",
            "pageNo", "1",
            "arrange", "B"
        };
      }
      
      // API 호출
      String response = callApi("/searchKeyword2", params);
      return parseAndFormatResults(response, query);
      
    } catch (Exception e) {
      log.error("❌ 검색 실패", e);
      return String.format("검색 중 오류가 발생했습니다: %s", e.getMessage());
    }
  }
  
  /**
   * API 응답을 파싱하고 포맷팅
   */
  private String parseAndFormatResults(String response, String query) throws Exception {
      
      // 응답 파싱
      JsonNode root = objectMapper.readTree(response);
      
      // 디버깅: 전체 응답 로깅 (처음 500자만)
      log.debug("📄 API 응답: {}", response.length() > 500 ? response.substring(0, 500) + "..." : response);
      
      // 에러 체크
      JsonNode header = root.path("response").path("header");
      String resultCode = header.path("resultCode").asText("");
      
      // resultCode가 비어있으면 응답 구조 확인
      if (resultCode.isEmpty()) {
        log.warn("⚠️  resultCode가 없습니다. 응답 구조 확인: header={}", header.toString());
      }
      
      if (!resultCode.isEmpty() && !"0000".equals(resultCode)) {
        String resultMsg = header.path("resultMsg").asText("알 수 없는 오류");
        log.error("❌ API 에러 [{}]: {}", resultCode, resultMsg);
        return String.format("검색 실패: %s (코드: %s)", resultMsg, resultCode);
      }
      
      // 결과 추출
      JsonNode body = root.path("response").path("body");
      int totalCount = body.path("totalCount").asInt(0);
      JsonNode items = body.path("items").path("item");
      
      if (!items.isArray() || items.isEmpty()) {
        log.warn("📭 검색 결과 없음 (totalCount={})", totalCount);
        return String.format("'%s'에 대한 검색 결과가 없습니다.\n\n💡 검색 팁:\n" +
            "  • 지역명을 더 구체적으로 입력해보세요 (예: '제주 숙소' → '제주시 호텔')\n" +
            "  • 다른 키워드를 사용해보세요 (숙소/호텔/펜션/게스트하우스)\n" +
            "  • 지역명만 입력해보세요 (예: '제주도')", query);
      }
      
      // 결과 포맷팅 (최대 10개)
      int count = Math.min(items.size(), 10);
      log.info("✅ 검색 완료: 총 {}건 중 {}건 표시", totalCount, count);
      
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("📍 '%s' 검색 결과 (%d건)\n\n", query, count));
      
      for (int i = 0; i < count; i++) {
        JsonNode item = items.get(i);
        
        // 기본 정보
        String title = cleanText(item.path("title").asText("제목 없음"));
        String addr = cleanText(item.path("addr1").asText(""));
        String contentId = item.path("contentid").asText("");  // 숫자 ID
        String contentTypeId = item.path("contenttypeid").asText("");
        String tel = cleanText(item.path("tel").asText(""));
        // 이미지 URL은 검색 결과에 표시하지 않음 (AI가 contentId로 혼동 방지)
        
        // 포맷팅
        sb.append(String.format("[%d] %s", i + 1, title));
        
        // 콘텐츠 타입 뱃지
        String typeBadge = getContentTypeBadge(contentTypeId);
        if (!typeBadge.isEmpty()) {
          sb.append(" ").append(typeBadge);
        }
        sb.append("\n");
        
        // 주소
        if (!addr.isEmpty()) {
          sb.append(String.format("📍 %s\n", addr));
        }
        
        // 연락처
        if (!tel.isEmpty()) {
          sb.append(String.format("📞 %s\n", tel));
        }
        
        // 상세조회 ID (숫자, 이미지 URL 아님)
        if (!contentId.isEmpty()) {
          sb.append(String.format("🔑 상세조회ID(숫자): %s\n", contentId));
        }
        
        // 구분선
        if (i < count - 1) {
          sb.append("\n");
        }
      }
      
      // 더 많은 결과 안내
      if (totalCount > count) {
        sb.append("\n\n💡 총 ").append(totalCount).append("건 중 ").append(count)
          .append("건만 표시됩니다. 더 구체적인 키워드로 검색하면 원하는 결과를 찾기 쉽습니다.");
      }
      
      return sb.toString();
  }
  
  
  /**
   * contentId로 관광지 상세 정보를 조회합니다.
   * 
   * @param contentId 콘텐츠 ID (search 결과에서 확인 가능)
   * @return 상세 정보 (주소, 연락처, 홈페이지, 설명 등)
   */
  @Tool(description = "한국 관광지/맛집/숙소의 상세 정보를 조회합니다. search 결과에서 '🔑 상세조회ID(숫자)' 항목의 숫자 ID만 입력하세요. 이미지 URL(http...)은 입력하면 안됩니다.")
  @Override
  public String fetch(String contentId) {
    // URL이 입력된 경우 즉시 거부 (AI가 이미지 URL을 contentId로 오인하는 경우 방지)
    if (contentId == null || contentId.startsWith("http") || contentId.contains("/")) {
      log.warn("⚠️  잘못된 contentId 입력: {} → 숫자 ID가 필요합니다", contentId);
      return "오류: contentId는 숫자여야 합니다 (예: 126508). " +
             "search 결과의 '🔑 상세조회ID(숫자)' 항목 값을 사용하세요. " +
             "이미지 URL이나 주소는 입력하지 마세요.";
    }
    
    log.info("📖 상세 조회: {}", contentId);
    
    try {
      // detailCommon2 호출 (numOfRows=1, pageNo=1 명시)
      String response = callApi("/detailCommon2",
          "contentId", contentId,
          "numOfRows", "1",
          "pageNo", "1"
      );
      
      // 응답 파싱
      JsonNode root = objectMapper.readTree(response);
      JsonNode header = root.path("response").path("header");
      String resultCode = header.path("resultCode").asText();
      
      if (!"0000".equals(resultCode)) {
        log.error("❌ API 에러: {}", header.path("resultMsg").asText());
        return "상세 정보를 조회할 수 없습니다.";
      }
      
      JsonNode item = root.path("response").path("body").path("items").path("item");
      if (item.isArray() && item.size() > 0) {
        item = item.get(0);
      }
      
      if (item.isMissingNode()) {
        log.warn("⚠️  상세 정보 없음");
        return String.format("ID '%s'에 대한 상세 정보를 찾을 수 없습니다.", contentId);
      }
      
      // 상세 정보 포맷팅 (DummySearchService 스타일)
      String title = cleanText(item.path("title").asText("제목 없음"));
      String contentTypeId = item.path("contenttypeid").asText("");
      String addr1 = cleanText(item.path("addr1").asText(""));
      String addr2 = cleanText(item.path("addr2").asText(""));
      String tel = cleanText(item.path("tel").asText(""));
      String homepage = cleanText(item.path("homepage").asText(""));
      String overview = cleanText(item.path("overview").asText(""));
      String firstImage = item.path("firstimage").asText("");
      String zipcode = item.path("zipcode").asText("");
      
      StringBuilder sb = new StringBuilder();
      
      // 제목 + 타입 뱃지
      sb.append("📌 ").append(title);
      String typeBadge = getContentTypeBadge(contentTypeId);
      if (!typeBadge.isEmpty()) {
        sb.append(" ").append(typeBadge);
      }
      sb.append("\n\n");
      
      // 설명 (DummySearchService처럼 500자 제한)
      if (!overview.isEmpty()) {
        String desc = overview;
        if (desc.length() > 500) {
          desc = desc.substring(0, 500) + "...";
        }
        sb.append("📝 소개\n").append(desc).append("\n\n");
      }
      
      // 주소
      if (!addr1.isEmpty()) {
        sb.append("📍 주소\n").append(addr1);
        if (!addr2.isEmpty()) {
          sb.append(" ").append(addr2);
        }
        if (!zipcode.isEmpty()) {
          sb.append(" (").append(zipcode).append(")");
        }
        sb.append("\n\n");
      }
      
      // 연락처
      if (!tel.isEmpty()) {
        sb.append("📞 연락처\n").append(tel).append("\n\n");
      }
      
      // 홈페이지
      if (!homepage.isEmpty()) {
        sb.append("🌐 홈페이지\n").append(homepage).append("\n\n");
      }
      
      // 이미지
      if (!firstImage.isEmpty()) {
        sb.append("🖼️  사진\n").append(firstImage).append("\n\n");
      }
      
      // 추가 정보 안내
      sb.append("💡 추가 정보가 필요하면 위 연락처나 홈페이지를 확인하세요.");
      
      log.info("✅ 상세 조회 완료");
      return sb.toString();
      
    } catch (Exception e) {
      log.error("❌ 상세 조회 실패", e);
      return String.format("상세 정보를 조회할 수 없습니다: %s", e.getMessage());
    }
  }
  
  // ##### 헬퍼 메서드 #####
  
  /**
   * 키워드에서 콘텐츠 타입을 자동 감지
   * 
   * @param query 검색 키워드
   * @return contentTypeId (감지 안되면 null)
   */
  private String detectContentType(String query) {
    String lowerQuery = query.toLowerCase();
    
    // 숙박 관련 키워드
    if (lowerQuery.contains("숙소") || lowerQuery.contains("호텔") || 
        lowerQuery.contains("펜션") || lowerQuery.contains("모텔") ||
        lowerQuery.contains("게스트하우스") || lowerQuery.contains("리조트") ||
        lowerQuery.contains("숙박") || lowerQuery.contains("accommodation")) {
      return TYPE_ACCOMMODATION;
    }
    
    // 음식점 관련 키워드
    if (lowerQuery.contains("맛집") || lowerQuery.contains("음식점") || 
        lowerQuery.contains("식당") || lowerQuery.contains("레스토랑") ||
        lowerQuery.contains("카페") || lowerQuery.contains("restaurant")) {
      return TYPE_RESTAURANT;
    }
    
    // 관광지 관련 키워드
    if (lowerQuery.contains("관광지") || lowerQuery.contains("여행지") || 
        lowerQuery.contains("명소") || lowerQuery.contains("볼거리") ||
        lowerQuery.contains("attraction")) {
      return TYPE_ATTRACTION;
    }
    
    // 문화시설 관련 키워드
    if (lowerQuery.contains("박물관") || lowerQuery.contains("미술관") || 
        lowerQuery.contains("전시관") || lowerQuery.contains("문화시설")) {
      return TYPE_CULTURE;
    }
    
    // 축제 관련 키워드
    if (lowerQuery.contains("축제") || lowerQuery.contains("행사") ||
        lowerQuery.contains("festival")) {
      return TYPE_FESTIVAL;
    }
    
    // 쇼핑 관련 키워드
    if (lowerQuery.contains("쇼핑") || lowerQuery.contains("시장") ||
        lowerQuery.contains("면세점") || lowerQuery.contains("shopping")) {
      return TYPE_SHOPPING;
    }
    
    // 레포츠 관련 키워드
    if (lowerQuery.contains("레포츠") || lowerQuery.contains("액티비티") ||
        lowerQuery.contains("스키") || lowerQuery.contains("sports")) {
      return TYPE_LEISURE;
    }
    
    // 감지 안됨
    return null;
  }
  
  /**
   * 검색어에서 지역코드(areaCode)를 감지
   * 한국관광공사 API 지역코드 기준
   */
  private String detectAreaCode(String query) {
    if (query.contains("서울")) return "1";
    if (query.contains("인천")) return "2";
    if (query.contains("대전")) return "3";
    if (query.contains("대구")) return "4";
    if (query.contains("광주")) return "5";
    if (query.contains("부산")) return "6";
    if (query.contains("울산")) return "7";
    if (query.contains("세종")) return "8";
    if (query.contains("경기")) return "31";
    if (query.contains("강원")) return "32";
    if (query.contains("충북") || query.contains("충청북")) return "33";
    if (query.contains("충남") || query.contains("충청남")) return "34";
    if (query.contains("경북") || query.contains("경상북")) return "35";
    if (query.contains("경남") || query.contains("경상남")) return "36";
    if (query.contains("전북") || query.contains("전라북")) return "37";
    if (query.contains("전남") || query.contains("전라남")) return "38";
    if (query.contains("제주")) return "39";
    return null;  // 지역 감지 안됨
  }
  
  /**
   * 콘텐츠 타입 ID를 이모지 뱃지로 변환
   */
  private String getContentTypeBadge(String contentTypeId) {
    return switch (contentTypeId) {
      case TYPE_ATTRACTION -> "🏞️ 관광지";
      case TYPE_CULTURE -> "🎭 문화시설";
      case TYPE_FESTIVAL -> "🎉 축제/행사";
      case TYPE_COURSE -> "🚶 여행코스";
      case TYPE_LEISURE -> "🏃 레포츠";
      case TYPE_ACCOMMODATION -> "🏨 숙박";
      case TYPE_SHOPPING -> "🛍️ 쇼핑";
      case TYPE_RESTAURANT -> "🍽️ 음식점";
      default -> "";
    };
  }
  
  /**
   * 검색어에서 카테고리 단어(관광지/맛집 등)를 제거
   * TourAPI 키워드 검색은 제목 매칭이라 카테고리 단어가 포함되면 결과가 0건이 되기 쉬움
   */
  private String stripCategoryWords(String query) {
    String stripped = query
        .replaceAll("관광지|여행지|명소|볼거리|맛집|음식점|식당|레스토랑|카페|박물관|미술관|전시관|문화시설|축제|행사|쇼핑|레포츠|액티비티|추천", "")
        .replaceAll("\\s+", " ")
        .trim();
    // 전부 제거되어 빈 문자열이면 원본 사용
    return stripped.isEmpty() ? query : stripped;
  }

  /**
   * 키워드를 URL 인코딩
   */
  private String encodeKeyword(String keyword) {
    try {
      return java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.warn("⚠️  키워드 인코딩 실패, 원본 사용: {}", keyword);
      return keyword;
    }
  }
  
  /**
   * API 호출 (공통 로직)
   * 
   * @param path API 경로 (예: "/searchKeyword2")
   * @param params 파라미터 (key1, value1, key2, value2, ...)
   * @return API 응답 (JSON 문자열)
   */
  private String callApi(String path, String... params) {
    // URL 생성
    StringBuilder url = new StringBuilder(path);
    url.append("?serviceKey=").append(apiKey);
    url.append("&MobileOS=").append(MOBILE_OS);
    url.append("&MobileApp=").append(MOBILE_APP);
    url.append("&_type=").append(RESPONSE_TYPE);
    
    // 추가 파라미터
    StringBuilder paramLog = new StringBuilder();
    for (int i = 0; i < params.length; i += 2) {
      if (i + 1 < params.length) {
        url.append("&").append(params[i]).append("=").append(params[i + 1]);
        paramLog.append(params[i]).append("=").append(params[i + 1]).append(", ");
      }
    }
    
    log.info("🌐 API 호출: {} ({})", path, paramLog.toString());
    log.debug("   전체 URL: {}{}", apiEndpoint, url);
    
    // WebClient로 API 호출
    return webClient.get()
        .uri(url.toString())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
  
  /**
   * HTML 태그 제거 및 텍스트 정리
   * 
   * @param text 원본 텍스트
   * @return 정리된 텍스트
   */
  private String cleanText(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    
    return text
        .replaceAll("<[^>]*>", "")        // HTML 태그 제거
        .replaceAll("&nbsp;", " ")        // &nbsp; → 공백
        .replaceAll("&lt;", "<")          // &lt; → <
        .replaceAll("&gt;", ">")          // &gt; → >
        .replaceAll("&amp;", "&")         // &amp; → &
        .replaceAll("&quot;", "\"")       // &quot; → "
        .replaceAll("\\s+", " ")          // 연속된 공백 → 단일 공백
        .trim();
  }
}
