# ReAct (Reasoning + Acting) 테스트 질문 모음

## 1. 단일 정보 요청 (개별 Tool 호출)

### 시간 정보
```
지금 몇 시야?
현재 시간 알려줘
```

### 날씨 정보
```
오늘 날씨 어때?
지금 날씨 알려줘
밖에 춥니?
```

### 위치 정보
```
지금 어디야?
현재 위치가 어디야?
여기가 어디지?
```

### 상황 정보
```
지금 상황이 어때?
현재 현황 알려줘
주변 상황 설명해줘
```

---

## 2. 복합 정보 요청 (getAllInfo Tool 호출)

```
지금 모든 정보 알려줘
현재 시간, 날씨, 위치 다 알려줘
지금 상황 전체적으로 설명해줘
외출하기 좋은 날씨야? 지금 상황 전체를 고려해서 알려줘
```

---

## 3. ReAct 추론 과정을 확인할 수 있는 질문

```
외출하려고 하는데 어떤 옷을 입어야 할까?
-> Tool: getWeatherInfo 호출 예상
-> Thought: 날씨 정보를 확인해야 함
-> Action: getWeatherInfo 호출
-> Observation: 5°C, 맑음, 겨울 날씨
-> Answer: 두꺼운 외투 착용 권장

지금 점심 먹으러 가기 좋은 시간이야?
-> Tool: getCurrentTime, getStatusInfo 호출 예상
-> Thought: 현재 시간과 주변 상황 확인 필요
-> Action: getCurrentTime, getStatusInfo 호출
-> Observation: 오전 11시 10분, 식당 점심 준비 중
-> Answer: 조금 이른 시간이지만 가능

북수원에서 지금 산책하기 좋은 환경이야?
-> Tool: getAllInfo 호출 예상
-> Thought: 날씨, 시간, 위치, 상황 모두 확인 필요
-> Action: getAllInfo 호출
-> Observation: 5°C 맑음, 오전 11시, 북수원, 교통량 보통
-> Answer: 날씨는 맑지만 춥고, 시간대는 괜찮음. 따뜻한 옷 입고 산책 가능
```

---

## 4. 추천 테스트 순서

### 단계 1: 기본 Tool 동작 확인
```bash
./call.sh /react/chat "지금 몇 시야?"
./call.sh /react/chat "오늘 날씨 어때?"
```

### 단계 2: 복합 Tool 동작 확인
```bash
./call.sh /react/chat "지금 모든 정보 알려줘"
```

### 단계 3: ReAct 추론 과정 확인
```bash
./call.sh /react/chat "외출하려고 하는데 어떤 옷을 입어야 할까?"
./call.sh /react/chat "북수원에서 지금 산책하기 좋은 환경이야?"
```

---

## 5. 기대 동작

각 질문에 대해 다음과 같은 형식의 답변이 나와야 합니다:

```
Thought: [질문 분석 및 필요 정보 판단]
Action: [Tool 선택 및 호출]
Observation: [Tool 실행 결과]
Answer: [최종 답변]
```

예시:
```
Thought: 사용자가 날씨를 물어보고 있으므로 getWeatherInfo 도구를 사용해야 합니다.
Action: getWeatherInfo 호출
Observation: 11월 겨울 날씨, 기온 5°C, 맑음
Answer: 현재 날씨는 맑지만 기온이 5°C로 낮습니다. 겨울 날씨이므로 따뜻한 외투를 입고 외출하시는 것이 좋습니다.
```
