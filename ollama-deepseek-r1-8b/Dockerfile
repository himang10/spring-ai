## Ollama 베이스 이미지에 deepseek-r1:8b 모델을 미리 내려받아 포함시키는 이미지
##
## - ollama/ollama 이미지를 기반으로, 빌드 시점에 ollama serve를 잠시 띄워
##   지정된 모델을 pull한 뒤 그대로 이미지 레이어에 포함시킨다.
## - 컨테이너 최초 기동 시 모델을 매번 다시 받을 필요가 없어 기동이 빠르다.

FROM ollama/ollama:latest

# 빌드 시 --build-arg OLLAMA_MODEL=... 로 변경 가능 (기본값: deepseek-r1:8b)
ARG OLLAMA_MODEL=deepseek-r1:8b
ENV OLLAMA_MODEL=${OLLAMA_MODEL}
ENV OLLAMA_HOST=0.0.0.0

# 빌드 시점에 ollama 서버를 백그라운드로 띄운 뒤 모델을 pull하고 종료한다.
RUN (ollama serve &) && \
    sleep 5 && \
    ollama pull "${OLLAMA_MODEL}" && \
    ollama list && \
    (pkill ollama || true)

EXPOSE 11434

ENTRYPOINT ["ollama", "serve"]
