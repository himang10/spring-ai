package com.example.demo.agent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.dto.Accommodation;
import com.example.demo.dto.Attraction;
import com.example.demo.dto.Plan;
import com.example.demo.dto.PlanState;
import com.example.demo.dto.Requirements;
import com.example.demo.dto.Restaurant;

// Travel Orchestrator - 여행 멀티 에이전트 조율자
// Tool 기반 아키텍처로 LLM이 자동으로 필요한 Agent 선택
// ToolContext를 활용하여 ThreadLocal 없이 emitter를 @Tool 메서드에 전달
@Component
public class TravelOrchestrator {
  // ----------------------------------------------------------------------------------------
  // ChatClient 생성을 위한 빌더
  private final ChatClient.Builder chatClientBuilder;

  // 대화의 문맥(Context)을 저장하고 관리하는 메모리
  private final ChatMemory chatMemory;

  // 협업을 수행할 하위 전문가 에이전트들
  private final Exam03AttractionAgent attractionAgent; // 관광지 검색 에이전트
  private final Exam04RestaurantAgent restaurantAgent; // 맛집 추천 에이전트
  private final Exam05AccommodationAgent accommodationAgent; // 숙소 정보 에이전트
  private final PlanAgent planAgent; // 일정 통합 계획 에이전트
  private final BudgetAgent budgetAgent; // 예산 검증 에이전트

  // ----------------------------------------------------------------------------------------
  // 생성자: ChatClient와 협업할 전문가 에이전트들을 주입받아 초기화
  public TravelOrchestrator(
      ChatClient.Builder chatClientBuilder,
      ChatMemory chatMemory,
      Exam03AttractionAgent attractionAgent,
      Exam04RestaurantAgent restaurantAgent,
      Exam05AccommodationAgent accommodationAgent,
      PlanAgent planAgent,
      BudgetAgent budgetAgent) {

    this.chatClientBuilder = chatClientBuilder;
    this.chatMemory = chatMemory;
    this.attractionAgent = attractionAgent;
    this.restaurantAgent = restaurantAgent;
    this.accommodationAgent = accommodationAgent;
    this.planAgent = planAgent;
    this.budgetAgent = budgetAgent;
  }

  // ----------------------------------------------------------------------------------------
  // Agent 실행
  public String execute(String userQuery, String conversationId, SseEmitter emitter) {
    ChatClient chatClient = chatClientBuilder.build();

    String systemMessage = """
          당신은 여행 계획 전문 오케스트레이터(조율자)입니다.
          사용자의 질문을 분석하여 적절한 전문 에이전트에게 작업을 위임합니다.

          ## 사용 가능한 도구 (Agent):
          1. callAttractionAgent - 관광지/명소 추천
              예: "서울 명소", "부산 관광지", "제주도 가볼만한 곳"
          2. callRestaurantAgent - 맛집 추천
              예: "강남 맛집", "제주도 흑돼지", "부산 해물 맛집"
          3. callAccommodationAgent - 숙소 추천
              예: "서울 호텔", "제주도 펜션", "가성비 숙소"
          4. callMultiAgentForTravelPlan - 전체 여행 일정 생성
              예: "제주도 2박3일 100만원 예산으로 계획 짜줘"

          ## 응답 규칙:
          - 단일 정보(관광지/맛집/숙소)만 필요하면 해당 도구 하나만 호출
          - "N박N일 계획" 요청 시 callMultiAgentForTravelPlan 호출
          - 도구 호출이 필요 없는 일반 질문은 직접 답변

          당신의 주 역할은 적절한 도구(전문가 에이전트)를 호출하는 것입니다.
        """;

    // emitter를 ToolContext로 전달 → @Tool 메서드에서 직접 꺼내 사용
    String response = chatClient.prompt()
        .system(systemMessage)
        .user(userQuery)
        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .tools(this)
        .toolContext(Map.of("emitter", emitter))
        .call()
        .content();

    return response;
  }

  // ----------------------------------------------------------------------------------------
  // SSE 이벤트 전송 (emitter를 파라미터로 직접 받음)
  private void sendSseEvent(SseEmitter emitter, String agentName, String status, String message) {
    try {
      if (emitter != null) {
        emitter.send(SseEmitter.event().name("agent").data(Map.of(
            "agent", agentName,
            "status", status,
            "message", message)));
      }
    } catch (Exception e) {
      // 전송 실패 시 무시하여 주 로직의 흐름을 방해하지 않음
    }
  }

  // ----------------------------------------------------------------------------------------
  // ToolContext에서 SseEmitter 추출 헬퍼
  private SseEmitter getEmitter(ToolContext toolContext) {
    return (SseEmitter) toolContext.getContext().get("emitter");
  }

  // ----------------------------------------------------------------------------------------
  // Tool: 관광지 정보 조회
  @Tool(description = """
        관광지 정보를 조회합니다.
        사용자가 관광지, 명소, 가볼만한 곳에 대해 물어볼 때 이 도구를 사용하세요.
        예: "제주도 명소", "제주도 관광지", "제주도 가볼만한 곳"
      """, returnDirect = true)
  public List<Attraction> callAttractionAgent(String query, ToolContext toolContext) {
    SseEmitter emitter = getEmitter(toolContext);
    sendSseEvent(emitter, "AttractionAgent", "running", "관광지 정보 검색 중...");
    List<Attraction> attractions = attractionAgent.execute(query);
    sendSseEvent(emitter, "AttractionAgent", "complete", "관광지 정보 검색 완료");
    return attractions;
  }

  // ----------------------------------------------------------------------------------------
  // Tool: 맛집 정보 조회
  @Tool(description = """
        맛집 정보를 조회합니다.
        사용자가 음식, 맛집, 레스토랑에 대해 물어볼 때 이 도구를 사용하세요.
        예: "제주도 맛집", "제주도 흑돼지 맛집", "제주도 해물 맛집"
      """, returnDirect = true)
  public List<Restaurant> callRestaurantAgent(String query, ToolContext toolContext) {
    SseEmitter emitter = getEmitter(toolContext);
    sendSseEvent(emitter, "RestaurantAgent", "running", "맛집 정보 검색 중...");
    List<Restaurant> restaurants = restaurantAgent.execute(query);
    sendSseEvent(emitter, "RestaurantAgent", "complete", "맛집 정보 검색 완료");
    return restaurants;
  }

  // ----------------------------------------------------------------------------------------
  // Tool: 숙소 정보 조회
  @Tool(description = """
        숙소 정보를 조회합니다.
        사용자가 호텔, 숙소, 펜션, 리조트에 대해 물어볼 때 이 도구를 사용하세요.
        예: "제주도 호텔", "제주도 펜션", "제주도 가성비 숙소"
      """, returnDirect = true)
  public List<Accommodation> callAccommodationAgent(String query, ToolContext toolContext) {
    SseEmitter emitter = getEmitter(toolContext);
    sendSseEvent(emitter, "AccommodationAgent", "running", "숙소 정보 검색 중...");
    List<Accommodation> accommodations = accommodationAgent.execute(query);
    sendSseEvent(emitter, "AccommodationAgent", "complete", "숙소 정보 검색 완료");
    return accommodations;
  }

  // ----------------------------------------------------------------------------------------
  // Tool: 여행 일정 생성
  @Tool(description = """
          전체 여행 일정을 생성합니다.
          사용자가 "N박N일 계획 짜줘", "여행 일정 만들어줘"라고 요청할 때 사용하세요.
          예: "제주도 2박3일 여행 계획 짜줘", "제주도 3박4일 여행 일정 만들어줘"
      """, returnDirect = true)
  public Plan callMultiAgentForTravelPlan(String query, ToolContext toolContext) {
    SseEmitter emitter = getEmitter(toolContext);

    // 사용자 질문에서 요구사항을 알아내고 상태 객체 생성 (요구 분석 단계)
    PlanState state = parseUserQuery(query);

    // 관광지/맛집/숙소 에이전트를 병렬로 실행 (정보 수집 단계)
    // emitter를 파라미터로 직접 전달 → 각 자식 스레드가 클로저로 캡처
    collectTravelInfoInParallel(state, emitter);

    // 여행 일정을 생성하는 에이전트 실행 (여행 일정 초기 계획 단계)
    sendSseEvent(emitter, "PlanAgent", "running", "초기 여행 일정 생성 중...");
    planAgent.execute(state);
    sendSseEvent(emitter, "PlanAgent", "complete", "초기 여행 일정 생성 완료");

    // 예산을 분석하는 에이전트 실행 (검증 단계)
    sendSseEvent(emitter, "BudgetAgent", "running", "예산 분석 중...");
    budgetAgent.execute(state);
    sendSseEvent(emitter, "BudgetAgent", "complete", state.getBudgetAnalysis().getMessage());

    // 예산 초과 시에만 재계획 수행 (피드백 루프)
    if (state.getBudgetAnalysis().isExceeded()) {
      sendSseEvent(emitter, "PlanAgent", "warning", "예산 초과 - 재계획 필요");
      replanWithAdjustedBudget(state, emitter);
    }
    // 최종 plan 반환
    return state.getPlan();
  }

  // ----------------------------------------------------------------------------------------
  // 사용자 질문 파싱 메서드
  private PlanState parseUserQuery(String userQuery) {
    PlanState state = new PlanState();
    state.setUserQuery(userQuery);

    // LLM을 사용하여 구조화된 정보 추출
    String userMessage = String.format("""
        다음 사용자 질문에서 여행 정보를 추출하여 JSON 형식으로 반환하세요.

        사용자 질문: "%s"

        추출할 정보:
        - destination: 여행지, 예) 제주도
        - days: 여행 전체 일수, 예) 3, 4
        - maxBudget: 예산 (숫자만, 원 단위), 예) 200000, 1000000

        추출 규칙:
        - destination은 "제주도"로 고정값
        - days는 "N박N일"에서 마지막 숫자 추출 (예: "2박3일" → 3)
        - maxBudget은 원 단위 숫자로 변환:
          * "20만원" → 200000 (20 곱하기 10000)
          * "100만원" → 1000000 (100 곱하기 10000)
          * "50만원" → 500000 (50 곱하기 10000)
          * "200만원" → 2000000 (200 곱하기 10000)

        예시 변환:
        - "제주도 2박3일 20만원" → {"destination": "제주도", "days": 3, "maxBudget": 200000}
        - "제주도 3박4일 100만원" → {"destination": "제주도", "days": 4, "maxBudget": 1000000}
        - "제주도 1박2일 50만원" → {"destination": "제주도", "days": 2, "maxBudget": 500000}

        **중요**: "X만원"에서 X는 만원 단위 숫자입니다. "20만원"은 20×10000=200000원입니다.

        JSON만 반환하고 다른 설명은 추가하지 마세요.
        """, userQuery);

    ChatClient chatClient = chatClientBuilder.build();
    Requirements requirements = chatClient.prompt()
        .user(userMessage)
        .call()
        .entity(Requirements.class);
    state.setDestination(requirements.getDestination());
    state.setDays(requirements.getDays());
    state.setMaxBudget(requirements.getMaxBudget());

    return state;
  }

  // ----------------------------------------------------------------------------------------
  // 병렬로 여행 정보 수집 (관광지/맛집/숙소)
  // emitter를 파라미터로 받아 람다 클로저로 캡처 → ThreadLocal 불필요
  private void collectTravelInfoInParallel(PlanState state, SseEmitter emitter) {
    // 관광지 검색
    CompletableFuture<Void> attractionTask = CompletableFuture.runAsync(() -> {
      sendSseEvent(emitter, "AttractionAgent", "running", "관광지 검색 중...");
      attractionAgent.execute(state);
      sendSseEvent(emitter, "AttractionAgent", "complete", "관광지 검색 완료");
    });

    // 맛집 검색
    CompletableFuture<Void> restaurantTask = CompletableFuture.runAsync(() -> {
      sendSseEvent(emitter, "RestaurantAgent", "running", "맛집 검색 중...");
      restaurantAgent.execute(state);
      sendSseEvent(emitter, "RestaurantAgent", "complete", "맛집 검색 완료");
    });

    // 숙소 검색
    CompletableFuture<Void> accommodationTask = CompletableFuture.runAsync(() -> {
      sendSseEvent(emitter, "AccommodationAgent", "running", "숙소 검색 중...");
      accommodationAgent.execute(state);
      sendSseEvent(emitter, "AccommodationAgent", "complete", "숙소 검색 완료");
    });

    // 모든 병렬 작업 완료 대기
    CompletableFuture.allOf(attractionTask, restaurantTask, accommodationTask).join();
  }

  // ----------------------------------------------------------------------------------------
  // 예산 초과 시 여행 계획 재작성
  private void replanWithAdjustedBudget(PlanState state, SseEmitter emitter) {
    sendSseEvent(emitter, "PlanAgent", "warning", "더 저렴한 옵션으로 재계획 중...");

    // 재계획 정보 설정
    state.setReplan(true);
    state.setPreviousTotalCost(state.getPlan().getTotalCost());

    // 저렴한 옵션으로 관광지 재검색
    CompletableFuture<Void> attractionReSearch = CompletableFuture.runAsync(() -> {
      try {
        sendSseEvent(emitter, "AttractionAgent", "running", "관광지 재검색 중...");
        attractionAgent.execute(state);
        sendSseEvent(emitter, "AttractionAgent", "complete", "관광지 재검색 완료");
      } catch (Exception e) {
        // 예외 발생 시 무시
      }
    });

    CompletableFuture<Void> accommodationReSearch = CompletableFuture.runAsync(() -> {
      try {
        sendSseEvent(emitter, "AccommodationAgent", "running", "숙소 재검색 중...");
        accommodationAgent.execute(state);
        sendSseEvent(emitter, "AccommodationAgent", "complete", "숙소 재검색 완료");
      } catch (Exception e) {
        // 예외 발생 시 무시
      }
    });

    CompletableFuture<Void> restaurantReSearch = CompletableFuture.runAsync(() -> {
      try {
        sendSseEvent(emitter, "RestaurantAgent", "running", "맛집 재검색 중...");
        restaurantAgent.execute(state);
        sendSseEvent(emitter, "RestaurantAgent", "complete", "맛집 재검색 완료");
      } catch (Exception e) {
        // 예외 발생 시 무시
      }
    });

    // 병렬 처리가 모두 완료될 때까지 블로킹
    CompletableFuture.allOf(attractionReSearch, restaurantReSearch, accommodationReSearch).join();

    // 여행 일정 재계획
    sendSseEvent(emitter, "PlanAgent", "running", "일정 재계획 중...");
    planAgent.execute(state);
    sendSseEvent(emitter, "PlanAgent", "complete", "일정 재계획 완료");

    // 예산 재검증
    sendSseEvent(emitter, "BudgetAgent", "running", "일정 재계획 예산 검증");
    budgetAgent.execute(state);
    sendSseEvent(emitter, "BudgetAgent", "complete", state.getBudgetAnalysis().getMessage());
  }
}