# spring-ai

## training-code
실습을 위한 베이스 코드가 있음

실습해야 할 코드 내용은 다음과 같이 코드 내부에 존재하며 이것을 다음과 같이 변경해야 한다
```
        // ----------- 여기는 실습 코드 부분입니다 ------------------------------------
       // TODO: 실습 1 - ChatClient를 사용하여 AI 응답 생성
       // 1. this.chatClient.prompt()로 프롬프트 시작
       // 2. .system()으로 시스템 메시지 설정 (예: "SKALA Cloud 기반 교육을 지원합니다")
       // 3. .user()로 사용자 입력 전달
       // 4. .call().content()로 응답 받기
       String response = null; // 여기에 AI 응답을 저장하세요
      
       // TODO: 실습 2 - 응답 Map 생성 및 반환
       // 1. HashMap 생성
       // 2. "message" 키에 AI 응답 저장
       // 3. "conversationId" 키에 conversationId 저장 (null이면 httpSession.getId() 사용)
       // 4. ResponseEntity.ok()로 반환
```

코드 실습 코드는 다음과 같이 추가한다.
```java
        // AI 응답 생성
        String response = this.chatClient.prompt()
            .system("SKALA Cloud 기반 교육을 지원합니다")
            .user(userInput)
            .call()
            .content();
        
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", response);
        responseMap.put("conversationId", conversationId != null ? conversationId :  httpSession.getId());
        
        return ResponseEntity.ok(responseMap);
```


## answer-code
실습 답이 있음 이코드는 최종적으로 맞춰보기 위한 코드로 사용한다