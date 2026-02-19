FROM eclipse-temurin:21-jre-jammy

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일과 OpenTelemetry Agent를 컨테이너로 복사
COPY target/spring-ai-tools-0.0.1-SNAPSHOT.jar app.jar
#COPY target/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# 포트 노출
EXPOSE 8080

# OpenTelemetry Java Agent와 함께 애플리케이션 실행
# ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]
