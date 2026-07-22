# 코드 목표
02.simple-multiagent 와 동일한 위치의 01.simple-agent의 Agent 와 tool을 이용해서 
Multi-Agent로 확장하는 코드를 작성한다.
이 코드는 01.simple-agent를 이용해서 개별 agent에 대한 이해와 개발 경험을 습득하고,
이것을 multi-agent 즉 orchestration agent를 통해 여러 개의 복합 에이전트를 활용하는 방법을 학습하기 위한 샘플 코드이다.

# 작성 방법
Multi-Agent로 확장하기 위한 방법과 가이드를 README.md에 작성해라
README.md에는 어떤 멀티 Agent인지도 설명해라
기존 simple-agent 코드의 변경을 최소화 하고 multi-agent로 갈경우 orhestration 중심으로 작성한다.
이러한 과정을 통해 기존 simple-agent 대비 어떤 것이 변경되었는지를 명확하게 이해하도록 한다.  


# 규칙
이미지 아이콘을 사용하지 않는다.
코드는 복잡하지 않고 교육을 위한 것으로 단순화하고 예외처리 및 로그를 많이 넣지 않는다. 
단, 학습 과정에 대한 흐름을 표시하는 로그는 넣을 수 있다.
커멘트가 필요한 경우 한줄짜리 간단하게 넣는다. 
spring AI 를 사용하고 maven 기준으로 작성한다. 
tymeleaf로 간한하게 agent 별 실행 과 결과를 볼 수 있는 UI를 제공한다. (화려하지 않고 심플하지만 깔끔하게)
