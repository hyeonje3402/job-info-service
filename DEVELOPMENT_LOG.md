# DEVELOPMENT_LOG

## 2026-11-26 — 공개 문서와 빌드 경로 점검

- 목적: 공개 README의 실행·테스트 안내와 현재 저장소 구성을 대조했다.
- 확인: \`main\`과 \`develop\`은 같은 기준 커밋이며, 기존 \`changelog.md\`의 1.0.1·1.0.2 기록을 보존한다.
- 검증: Java 21 환경에서 Gradle 8.4로 \`:backend:test\` 실행이 성공했다. 저장소의 Gradle Wrapper JAR가 없어 README의 \`./gradlew\` 명령은 이 clone에서 실행할 수 없었다. 성공한 검증은 직접 설치된 Gradle 실행에 한정된다.
- 변경: README와 changelog의 실행 전제·검증 범위를 명확히 했다. 소스 및 설정은 변경하지 않았다.
- 미검증: 프런트엔드 실행, Playwright E2E, 운영 배포는 이 작업에서 검증하지 않았다.
