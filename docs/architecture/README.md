# HireSync 시스템 아키텍처

[메인 README](../../README.md)

## 요청 흐름

1. React 화면에서 사용자가 PDF를 올리거나 채용 공고 URL을 입력합니다.
2. Spring Boot API가 세션 인증과 역할 권한을 확인합니다.
3. PDFBox/Tika 또는 Jsoup·Selenium을 이용해 문서 텍스트를 추출합니다.
4. Spring AI를 통해 OpenAI 호환 LLM에 구조화 추출을 요청합니다.
5. JPA 서비스 계층이 공고·사용자·사용량 관련 데이터를 관리하고 React에 응답합니다.

## 주요 구성

| 구성 | 구현 |
|---|---|
| Frontend | React 19, React Router 7, Axios, Vanilla CSS |
| API | Spring Boot 3.5, Spring MVC, Validation |
| 인증 | Spring Security 및 JSESSIONID 세션 |
| LLM | Spring AI OpenAI starter, OpenAI 호환 API 설정 |
| 문서·웹 수집 | Apache PDFBox, Spring AI Tika reader, Jsoup, Selenium/WebDriverManager |
| ORM·DB | Spring Data JPA, 기본 H2 설정, PostgreSQL 드라이버 포함 |
| API 문서 | SpringDoc OpenAPI |
| 테스트 | JUnit 5, Spring Security Test, MockMvc, Playwright |

## 데이터와 DB 경계

현재 application.properties는 H2 인메모리 데이터베이스를 기본으로 설정합니다. 인메모리 DB는 서버 프로세스가 종료되면 데이터가 유지되지 않습니다. Gradle에 PostgreSQL 드라이버가 포함되어 있지만, 이것만으로 운영 PostgreSQL 연결이나 마이그레이션이 구성된 것은 아닙니다.

공고·사용자·AI 사용 로그는 JPA 모델과 저장소 계층으로 처리합니다. 구체적인 엔티티와 테이블 정의는 backend/src/main/java 및 backend/src/main/resources/schema.sql을 확인하세요.

## 외부 연동 경계

LLM 호출에는 설정된 OpenAI 호환 API가 필요합니다. 채용 사이트 스크래핑은 페이지 구조와 접근 정책 변경에 영향을 받으므로, 사이트별 허용 범위와 이용 조건을 확인해야 합니다.

이 저장소에서 배포 클라우드, 객체 저장소, 프로덕션 데이터베이스 서버의 연결 설정은 확인되지 않았습니다.