# Orchestrator-Worker Pattern - Prompt 흐름 문서

## 전체 워크플로우 개요

```
User Request
    ↓
[1] Orchestrator Analysis (작업 분석 및 분해)
    ↓
[2] Worker Processing (병렬 처리)
    ↓
[3] Final Synthesis (최종 종합)
    ↓
User Response
```

---

## [1단계] Orchestrator: 작업 분석 및 분해

### 📨 User → Orchestrator

**사용자 입력 예시:**
```
"보안 모범 사례를 포함한 포괄적인 사용자 인증 시스템을 구축해라"
```

### 🤖 Orchestrator Prompt (to OpenAI)

```
당신은 작업을 분석하고 하위 작업으로 분해하는 오케스트레이터 에이전트입니다.

다음 작업이 주어졌습니다: {사용자_입력}

작업을 분석하여:
1. 수행해야 할 내용에 대한 간략한 분석을 제공하세요
2. 서로 다른 전문가가 처리할 수 있는 구체적인 하위 작업으로 분해하세요
3. 각 하위 작업에 대해 다음을 지정하세요:
   - 수행해야 할 작업에 대한 명확한 설명
   - 필요한 전문가 유형 (예: technical, documentation, testing, design)

다음 구조의 JSON 형식으로 응답하세요:
{
  analysis: 작업에 대한 간략한 분석,
  subTasks: [
    {
      description: 하위 작업에 대한 상세 설명,
      type: 전문가 유형
    }
  ]
}
```

### 📥 OpenAI → Orchestrator Response

```json
{
  "analysis": "이 작업은 패스워드 해싱, 세션 관리, 다중 인증 지원을 포함한 업계 모범 사례를 적용한 안전한 인증 시스템 구현이 필요합니다.",
  "subTasks": [
    {
      "description": "bcrypt 또는 Argon2를 사용하여 적절한 솔트 생성 및 저장과 함께 안전한 패스워드 해싱 구현",
      "type": "technical"
    },
    {
      "description": "리프레시 토큰 메커니즘을 포함한 JWT 기반 세션 관리 설계 및 구현",
      "type": "technical"
    },
    {
      "description": "로그인, 로그아웃, 회원가입, 토큰 갱신을 포함한 인증 엔드포인트에 대한 포괄적인 API 문서 작성",
      "type": "documentation"
    },
    {
      "description": "엣지 케이스와 보안 취약점을 포함한 인증 플로우에 대한 단위 및 통합 테스트 작성",
      "type": "testing"
    }
  ]
}
```

---

## [2단계] Worker: 병렬 서브작업 처리

각 서브작업은 전문 분야별 Worker가 **병렬로** 처리합니다 (Virtual Threads 사용).

### Worker Type 1: Technical Worker

#### 📨 Orchestrator → Technical Worker

**SubTask #1:**
```
Description: "bcrypt 또는 Argon2를 사용하여 적절한 솔트 생성 및 저장과 함께 안전한 패스워드 해싱을 구현해라"
Type: "technical"
```

#### 🤖 Technical Worker Prompt (to OpenAI)

```
당신은 기술 전문가입니다.
당신의 작업: bcrypt 또는 Argon2를 사용하여 적절한 솔트 생성 및 저장과 함께 안전한 패스워드 해싱을 구현해라

다음을 포함한 상세한 기술 솔루션을 제공하세요:
- 구현 방법
- 기술적 고려사항
- 해당하는 경우 코드 예제
```

#### 📥 OpenAI → Technical Worker Response

```
안전한 패스워드 해싱 기술 구현:

1. 구현 방법:
   - 최적의 보안/성능 균형을 위해 cost factor 12-14의 BCrypt 사용
   - 패스워드당 고유한 솔트를 자동으로 생성 (BCrypt가 내부적으로 처리)
   - 데이터베이스에 60자 이상의 문자 제한으로 해시된 패스워드 저장

2. 기술적 고려사항:
   - BCrypt는 무차별 대입 공격을 방지하기 위해 의도적으로 느리게 설계됨
   - Cost factor는 하드웨어 성능 향상에 따라 시간이 지나면서 증가 가능
   - 평문 패스워드는 절대 저장하지 않음
   - SQL 인젝션 방지를 위해 Prepared Statement 사용

3. 코드 예제 (Spring Security):

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    
    public void registerUser(String username, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        // Save to database: username + hashedPassword
    }
    
    public boolean authenticate(String username, String password) {
        String storedHash = getUserHashFromDB(username);
        return passwordEncoder.matches(password, storedHash);
    }
}
```

4. 보안 모범 사례:
   - 최소 패스워드 길이: 8자
   - 복잡도 요구 (대문자, 소문자, 숫자, 특수문자)
   - 로그인 실패 시 계정 잠금 구현
   - 모든 인증 엔드포인트에 HTTPS 사용
```

---

#### 📨 Orchestrator → Technical Worker

**SubTask #2:**
```
Description: "리프레시 토큰 메커니즘을 포함한 JWT 기반 세션 관리 설계 및 구현"
Type: "technical"
```

#### 🤖 Technical Worker Prompt (to OpenAI)

```
당신은 기술 전문가입니다.
당신의 작업: 리프레시 토큰 메커니즘을 포함한 JWT 기반 세션 관리 설계 및 구현

다음을 포함한 상세한 기술 솔루션을 제공하세요:
- 구현 방법
- 기술적 고려사항
- 해당하는 경우 코드 예제
```

#### 📥 OpenAI → Technical Worker Response

```
JWT 기반 세션 관리 기술 구현:

1. 구현 방법:
   - Access Token: 짧은 수명(15분), 사용자 인증 및 권한 정보 포함
   - Refresh Token: 긴 수명(7일), Access Token 갱신 전용
   - 토큰 페어 전략: 로그인 시 두 토큰을 함께 발급
   - Refresh Token Rotation: 사용 시마다 새로운 Refresh Token 발급

2. 기술적 고려사항:
   - JWT 서명 알고리즘: HS256 또는 RS256 사용
   - 토큰 페이로드에 민감한 정보(패스워드) 포함 금지
   - Refresh Token은 서버 측 블랙리스트 또는 화이트리스트로 관리
   - 토큰 만료 시간은 보안과 사용성의 균형 고려

3. 코드 예제 (Spring Boot):

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity = 900000; // 15분
    
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity = 604800000; // 7일
    
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}

@Service
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    
    public TokenResponse login(String username, String password) {
        // 사용자 인증 로직
        UserDetails user = authenticateUser(username, password);
        
        // Access Token 및 Refresh Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        // Refresh Token을 데이터베이스에 저장 (화이트리스트 관리)
        saveRefreshToken(username, refreshToken);
        
        return new TokenResponse(accessToken, refreshToken, "Bearer", 900);
    }
    
    public TokenResponse refresh(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        
        // 데이터베이스에 저장된 토큰인지 확인 (화이트리스트 검증)
        if (!refreshTokenRepository.exists(refreshToken)) {
            throw new UnauthorizedException("Refresh token not found");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserDetails user = loadUserByUsername(username);
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        
        // Refresh Token Rotation: 새로운 Refresh Token 생성
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        // 기존 Refresh Token 삭제 및 새 토큰 저장
        refreshTokenRepository.delete(refreshToken);
        saveRefreshToken(username, newRefreshToken);
        
        return new TokenResponse(newAccessToken, newRefreshToken, "Bearer", 900);
    }
    
    public void logout(String refreshToken) {
        // Refresh Token을 블랙리스트에 추가 또는 화이트리스트에서 제거
        refreshTokenRepository.delete(refreshToken);
    }
}
```

4. 보안 모범 사례:
   - Access Token은 클라이언트 메모리(state)에만 저장
   - Refresh Token은 httpOnly, secure 쿠키에 저장
   - CSRF 공격 방지를 위한 추가 보안 헤더 설정
   - 토큰 탈취 감지: 동일 Refresh Token 재사용 시 모든 세션 무효화
   - 로그아웃 시 Refresh Token 즉시 무효화

5. 토큰 갱신 플로우:
   ```
   1. 클라이언트가 API 요청 시 Access Token 포함
   2. 서버가 401 Unauthorized 응답 (토큰 만료)
   3. 클라이언트가 자동으로 /api/auth/refresh 호출
   4. Refresh Token으로 새 Access Token 및 Refresh Token 획득
   5. 원래 요청을 새 Access Token으로 재시도
   ```
```

---

### Worker Type 2: Documentation Worker

#### 📨 Orchestrator → Documentation Worker

**SubTask #3:**
```
Description: "로그인, 로그아웃, 회원가입, 토큰 갱신을 포함한 인증 엔드포인트에 대한 포괄적인 API 문서 작성"
Type: "documentation"
```

#### 🤖 Documentation Worker Prompt (to OpenAI)

```
당신은 문서화 전문가입니다.
당신의 작업: 로그인, 로그아웃, 회원가입, 토큰 갱신을 포함한 인증 엔드포인트에 대한 포괄적인 API 문서 작성

다음을 포함한 포괄적인 문서를 제공하세요:
- 명확한 설명
- 사용 예제
- 모범 사례
```

#### 📥 OpenAI → Documentation Worker Response

```
# 인증 API 문서

## 엔드포인트 개요

### 1. POST /api/auth/register
새 사용자 계정을 등록합니다.

**요청 본문:**
```json
{
  "username": "문자열 (3-50자)",
  "email": "문자열 (유효한 이메일)",
  "password": "문자열 (최소 8자)"
}
```

**응답 (201 Created):**
```json
{
  "userId": "uuid",
  "username": "문자열",
  "email": "문자열",
  "createdAt": "ISO8601 타임스탬프"
}
```

**에러 응답:**
- 400 Bad Request: 잘못된 입력 데이터
- 409 Conflict: 사용자명 또는 이메일이 이미 존재함

---

### 2. POST /api/auth/login
사용자를 인증하고 토큰을 받습니다.

**요청 본문:**
```json
{
  "username": "문자열",
  "password": "문자열"
}
```

**응답 (200 OK):**
```json
{
  "accessToken": "JWT 토큰 (15분 후 만료)",
  "refreshToken": "JWT 토큰 (7일 후 만료)",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**에러 응답:**
- 401 Unauthorized: 잘못된 인증 정보
- 423 Locked: 로그인 실패로 인한 계정 잠금

---

### 모범 사례:

1. **토큰 저장:**
   - Access 토큰은 메모리에 저장 (state/context)
   - Refresh 토큰은 httpOnly secure 쿠키에 저장
   - localStorage에는 절대 토큰을 저장하지 않음

2. **토큰 사용:**
   ```
   Authorization: Bearer {accessToken}
   ```

3. **토큰 갱신 흐름:**
   - Access 토큰이 만료되면 (401), 자동으로 /api/auth/refresh 호출
   - Refresh 토큰을 사용하여 새 access 토큰 획득
   - 갱신 실패 시, 로그인 페이지로 리다이렉트
```

---

### Worker Type 3: Testing Worker

#### 📨 Orchestrator → Testing Worker

**SubTask #4:**
```
Description: "엣지 케이스와 보안 취약점을 포함한 인증 플로우에 대한 단위 및 통합 테스트 작성"
Type: "testing"
```

#### 🤖 Testing Worker Prompt (to OpenAI)

```
당신은 테스트 전문가입니다.
당신의 작업: 엣지 케이스와 보안 취약점을 포함한 인증 플로우에 대한 단위 및 통합 테스트 작성

다음을 포함한 테스트 권장사항을 제공하세요:
- 테스트 시나리오
- 테스트 케이스
- 예상 결과
```

#### 📥 OpenAI → Testing Worker Response

```
# 인증 테스트 전략

## 테스트 시나리오

### 1. 회원가입 테스트

**테스트 케이스 1.1: 유효한 회원가입**
```java
@Test
public void testRegisterUser_ValidData_Success() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "john_doe", 
        "john@example.com", 
        "SecurePass123!"
    );
    
    // When
    ResponseEntity<UserResponse> response = authController.register(request);
    
    // Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody().getUserId());
    verify(passwordEncoder).encode("SecurePass123!");
}
```

**테스트 케이스 1.2: 중복 사용자명**
```java
@Test
public void testRegisterUser_DuplicateUsername_Conflict() {
    // Given
    when(userRepository.existsByUsername("john_doe")).thenReturn(true);
    
    // When & Then
    assertThrows(ConflictException.class, () -> 
        userService.register("john_doe", "email@test.com", "password")
    );
}
```

### 2. 인증 테스트

**테스트 케이스 2.1: 성공적인 로그인**
```java
@Test
public void testLogin_ValidCredentials_ReturnsTokens() {
    // Given
    String username = "john_doe";
    String password = "SecurePass123!";
    String hashedPassword = passwordEncoder.encode(password);
    User user = new User(username, hashedPassword);
    
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access.token.here");
    when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh.token.here");
    
    // When
    TokenResponse response = authService.login(username, password);
    
    // Then
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    assertEquals("Bearer", response.getTokenType());
}
```

**테스트 케이스 2.2: 잘못된 패스워드**
```java
@Test
public void testLogin_InvalidPassword_UnauthorizedException() {
    // Given
    when(userRepository.findByUsername("john_doe"))
        .thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong_password", user.getPassword()))
        .thenReturn(false);
    
    // When & Then
    assertThrows(UnauthorizedException.class, () ->
        authService.login("john_doe", "wrong_password")
    );
}
```

### 3. 보안 취약점 테스트

**테스트 케이스 3.1: SQL 인젝션 방어**
```java
@Test
public void testLogin_SqlInjectionAttempt_SafelyHandled() {
    String maliciousUsername = "admin' OR '1'='1";
    
    // Should not bypass authentication
    assertThrows(UnauthorizedException.class, () ->
        authService.login(maliciousUsername, "any_password")
    );
}
```

**테스트 케이스 3.2: 무차별 대입 공격 방어**
```java
@Test
public void testLogin_MultipleFailedAttempts_AccountLocked() {
    // Given
    String username = "john_doe";
    
    // When - 5 failed login attempts
    for (int i = 0; i < 5; i++) {
        try {
            authService.login(username, "wrong_password");
        } catch (UnauthorizedException e) {
            // Expected
        }
    }
    
    // Then - 6th attempt should result in locked account
    assertThrows(AccountLockedException.class, () ->
        authService.login(username, "correct_password")
    );
}
```

**테스트 케이스 3.3: JWT 토큰 만료**
```java
@Test
public void testAccessToken_Expired_UnauthorizedException() {
    // Given
    String expiredToken = generateExpiredToken();
    
    // When & Then
    assertThrows(UnauthorizedException.class, () ->
        jwtTokenProvider.validateToken(expiredToken)
    );
}
```

### 4. 통합 테스트

**테스트 케이스 4.1: 전체 인증 플로우**
```java
@Test
@SpringBootTest
@AutoConfigureMockMvc
public void testFullAuthenticationFlow() {
    // 1. Register
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registerRequestJson))
        .andExpect(status().isCreated());
    
    // 2. Login
    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginRequestJson))
        .andExpect(status().isOk())
        .andReturn();
    
    String accessToken = extractAccessToken(loginResult);
    
    // 3. Access Protected Resource
    mockMvc.perform(get("/api/user/profile")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
}
```

## 예상 테스트 커버리지

- 단위 테스트: 80% 이상 코드 커버리지
- 통합 테스트: 모든 주요 경로
- 보안 테스트: 모든 OWASP Top 10 취약점
- 성능 테스트: 로그인은 500ms 이내 완료
```

---

## [3단계] Final Synthesis: 최종 결과 종합

### 🔄 Advisor Pattern의 경우

Advisor가 모든 Worker 결과를 수집한 후 LLM에게 최종 종합을 요청합니다.

#### 📨 Advisor → OpenAI (Final Synthesis)

```
원본 요청: 보안 모범 사례를 포함한 포괄적인 사용자 인증 시스템을 구축해라

작업 분석: 이 작업은 패스워드 해싱, 세션 관리, 다중 인증 지원을 포함한 업계 모범 사례를 적용한 안전한 인증 시스템 구현이 필요합니다.

전문 워커 결과:

1. [technical] bcrypt 또는 Argon2를 사용하여 적절한 솔트 생성 및 저장과 함께 안전한 패스워드 해싱 구현
결과: {Technical Worker의 전체 응답}

2. [technical] 리프레시 토큰 메커니즘을 포함한 JWT 기반 세션 관리 설계 및 구현
결과: {Technical Worker의 전체 응답}

3. [documentation] 로그인, 로그아웃, 회원가입, 토큰 갱신을 포함한 인증 엔드포인트에 대한 포괄적인 API 문서 작성
결과: {Documentation Worker의 전체 응답}

4. [testing] 엣지 케이스와 보안 취약점을 포함한 인증 플로우에 대한 단위 및 통합 테스트 작성
결과: {Testing Worker의 전체 응답}

이러한 전문 결과들을 종합하여 포괄적인 최종 응답을 작성해주세요.
```

#### 📥 OpenAI → User (Final Response)

```
# 포괄적인 사용자 인증 시스템

분석 및 전문 워커의 기여를 바탕으로, 안전한 사용자 인증 시스템 구현을 위한 완전한 가이드입니다:

## 아키텍처 개요

인증 시스템은 네 가지 주요 구성 요소로 이루어져 있습니다:
1. 패스워드 보안 계층 (BCrypt 해싱)
2. 토큰 관리 시스템 (리프레시 토큰을 포함한 JWT)
3. API 인터페이스 (RESTful 엔드포인트)
4. 테스트 및 검증 도구

## 1. 패스워드 보안 구현

[Technical Worker의 구현 내용 요약]

## 2. JWT 세션 관리

[Technical Worker의 JWT 구현 내용 요약]

## 3. API 엔드포인트

[Documentation Worker의 API 문서 요약]

## 4. 테스트 전략

[Testing Worker의 테스트 전략 요약]

## 보안 체크리스트

✅ BCrypt를 사용한 패스워드 해싱 (cost factor 12)
✅ 만료 시간이 있는 JWT 토큰
✅ 리프레시 토큰 순환
✅ 로그인 실패 시 계정 잠금
✅ SQL 인젝션 방어
✅ HTTPS 강제 적용
✅ 포괄적인 테스트 커버리지

## 구현 단계

1. Spring Security 설정 구성
2. PasswordEncoder 빈 구현
3. 인증 엔드포인트 생성
4. JWT 토큰 프로바이더 구현
5. 보안 필터 추가
6. 포괄적인 테스트 작성
7. 모든 API 문서화

이 아키텍처는 우수한 개발자 경험과 시스템 성능을 유지하면서 엔터프라이즈급 보안을 제공합니다.
```

---

## 요청/응답 흐름 다이어그램

```
┌─────────────┐
│    User     │
└──────┬──────┘
       │
       │ POST /api/advisor/process
       │ {"task": "Create authentication system"}
       │
       ▼
┌──────────────────────────────────────────┐
│        ChatClient (Advisor Chain)        │
│  ┌────────────────────────────────────┐  │
│  │  OrchestratorWorkersAdvisor        │  │
│  └────────────────────────────────────┘  │
└──────┬───────────────────────────────────┘
       │
       │ [1] Orchestrator Analysis
       ▼
┌──────────────────────┐
│   Orchestrator       │──────► OpenAI API
│   (analyze task)     │◄────── (JSON response with subtasks)
└──────┬───────────────┘
       │
       │ SubTasks: [
       │   {type: "technical", desc: "..."},
       │   {type: "documentation", desc: "..."},
       │   {type: "testing", desc: "..."}
       │ ]
       │
       │ [2] Parallel Worker Processing
       ▼
┌─────────────────────────────────────────────────┐
│              Worker Pool                        │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────┐ │
│  │ Technical    │ │Documentation │ │ Testing │ │
│  │ Worker #1    │ │ Worker #2    │ │ Worker  │ │
│  └──────┬───────┘ └──────┬───────┘ └────┬────┘ │
│         │                │               │      │
│         ▼                ▼               ▼      │
│    ┌────────┐      ┌────────┐      ┌────────┐  │
│    │ OpenAI │      │ OpenAI │      │ OpenAI │  │
│    └────┬───┘      └────┬───┘      └────┬───┘  │
│         │                │               │      │
│         ▼                ▼               ▼      │
│    [Result 1]       [Result 2]      [Result 3] │
└─────────┬────────────────┬───────────────┬─────┘
          │                │               │
          └────────┬───────┴───────┬───────┘
                   │               │
       [3] Combine Results         │
                   ▼               │
          ┌────────────────┐       │
          │ buildEnhanced  │       │
          │    Prompt      │       │
          └────────┬───────┘       │
                   │               │
                   ▼               │
          ┌────────────────┐       │
          │   OpenAI API   │       │
          │  (Synthesize)  │       │
          └────────┬───────┘       │
                   │               │
                   ▼               │
          ┌────────────────┐       │
          │ Final Response │       │
          └────────┬───────┘       │
                   │               │
                   ▼               ▼
              ┌─────────────────────┐
              │    User Response    │
              └─────────────────────┘
```

---

## OpenAI API 호출 횟수

각 요청당 총 **5회**의 OpenAI API 호출이 발생합니다:

1. **1회**: Orchestrator 분석 (작업 분해)
2. **3회**: Worker들의 병렬 처리 (서브작업 처리)
   - Technical Worker × 2
   - Documentation Worker × 1
   - Testing Worker × 1
3. **1회**: Final Synthesis (최종 종합)

---

## 성능 최적화

### Virtual Threads 사용
```java
Executor executor = Executors.newVirtualThreadPerTaskExecutor();
```

Worker들이 병렬로 실행되므로:
- **순차 처리 시간**: ~20초 (각 Worker 5초 × 4개)
- **병렬 처리 시간**: ~5초 (가장 느린 Worker 기준)

약 **4배 성능 향상** 달성!

---

## 프롬프트 커스터마이징 가이드

### Orchestrator 프롬프트 수정
위치: `Orchestrator.java`

```java
String prompt = """
    당신은 작업을 분석하고 하위 작업으로 분해하는 오케스트레이터 에이전트입니다...
    
    [여기에 원하는 분석 방식 추가]
    
    다음 측면을 고려하세요:
    - 보안 요구사항
    - 확장성 고려사항
    - 성능 최적화
    """.formatted(taskDescription);
```

### Worker 프롬프트 추가
위치: `Worker.java`

```java
private static final String DESIGN_WORKER_PROMPT = """
    당신은 UX/UI 디자인 전문가입니다.
    당신의 작업: {task}
    
    다음을 포함한 디자인 권장사항을 제공하세요:
    - 사용자 경험 고려사항
    - 인터페이스 목업
    - 접근성 가이드라인
    """;

private String getPromptForType(String type) {
    return switch (type.toLowerCase()) {
        case "technical" -> TECHNICAL_WORKER_PROMPT;
        case "documentation" -> DOCUMENTATION_WORKER_PROMPT;
        case "testing" -> TESTING_WORKER_PROMPT;
        case "design" -> DESIGN_WORKER_PROMPT;  // 추가
        default -> DEFAULT_WORKER_PROMPT;
    };
}
```

---

## 실제 사용 예시

```bash
curl -X POST http://localhost:8080/api/advisor/process \
  -H "Content-Type: application/json" \
  -d '{
    "task": "Create a comprehensive user authentication system with security best practices"
  }'
```

이 요청은 위에서 설명한 전체 프롬프트 흐름을 거쳐 최종 응답을 생성합니다.
