# 1단계: 빌드 스테이지
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사 (멀티 프로젝트 구조 대응)
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY backend/build.gradle backend/

# 의존성 캐싱을 위해 빌드 파일만 복사 후 의존성 다운로드
RUN ./gradlew :backend:dependencies --no-daemon || true

# 백엔드 소스 코드 복사
COPY backend/src backend/src

# 테스트를 제외하고 백엔드 빌드
RUN ./gradlew :backend:bootJar -x test --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일 복사
COPY --from=build /app/backend/build/libs/*.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
