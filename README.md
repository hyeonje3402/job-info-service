# HireSync — AI 채용 정보 관리 서비스

PDF와 채용 공고 URL에서 핵심 정보를 추출하고, 공고·사용자·운영 설정을 관리하는 웹 서비스입니다.

## 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 주요 기능 | PDF/URL 공고 분석, 채용 공고 관리, 역할별 권한, 사용자·설정 관리 |
| Frontend | React 19, React Router 7, Axios, 반응형 CSS |
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring AI |
| Database | 기본 H2 인메모리 구성, PostgreSQL JDBC 드라이버 포함 |
| 문서 처리 | Apache PDFBox, Tika, Jsoup, Selenium |
| 테스트 | JUnit 5/MockMvc, Playwright, Newman 컬렉션 |
| CI | GitHub Actions Playwright 테스트 및 GitHub Classroom Markdown 검사 |
| CD/클라우드 | 저장소에서 자동 배포·클라우드 호스팅 설정은 확인되지 않음 |

## 전체 아키텍처

~~~mermaid
flowchart LR
    U[사용자·관리자] --> UI[React 웹 앱]
    UI --> API[Spring Boot REST API]
    API --> SEC[Spring Security 세션·역할 권한]
    API --> DB[(H2 데이터베이스)]
    PDF[채용 PDF] --> EXT[PDFBox/Tika 텍스트 추출]
    URL[채용 페이지 URL] --> SCRAPE[Jsoup/Selenium 수집]
    EXT --> LLM[Spring AI · OpenAI 호환 API]
    SCRAPE --> LLM
    LLM --> API
    API --> UI
~~~

## 문서

- [시스템 아키텍처](docs/architecture/README.md)
- [개발 환경·실행·테스트](docs/development/README.md)
- [CI·배포 현황](docs/operations/README.md)
- [로드맵 제안](docs/roadmap/README.md)

## 빠른 실행

백엔드와 프런트엔드를 각각 실행합니다.

~~~bash
./gradlew :backend:bootRun
~~~

~~~bash
cd frontend
npm install
npm start
~~~

AI 분석을 사용하려면 OpenAI 호환 API 키를 환경 변수로 설정해야 합니다. 로컬 사용자 계정과 설정은 [개발 문서](docs/development/README.md)를 확인하세요.

## 백엔드 테스트

저장소 루트에서 JUnit/MockMvc 백엔드 테스트를 실행합니다.

~~~bash
./gradlew :backend:test
~~~

테스트 전용 설정과 모킹된 HTTP 클라이언트를 사용하므로 실제 AI API 키나 외부 AI 호출 없이 테스트할 수 있습니다. 브라우저 E2E와 프런트엔드 테스트 명령은 [개발 문서](docs/development/README.md)를 참고하세요.

> 이 저장소는 개발 프로젝트입니다. 운영 배포 설정은 포함되어 있지 않습니다. 공개 저장소 README에는 테스트 계정 비밀번호나 API 키를 기록하지 마세요.
