# Java 21 JDK 기반 이미지 사용
FROM eclipse-temurin:21-jdk-alpine

# JAR 파일 복사 (빌드 산출물)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 포트 오픈 (application.yml의 server.port와 맞추기)
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["java","-jar","/app.jar"] 