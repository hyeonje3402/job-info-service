# 운영·CI/CD·배포

## 런타임 설정

저장소에서 확인한 기본값은 개발용입니다. 운영 환경 설정과 비밀 정보는 실행 환경에서 별도로 제공해야 합니다.

- 백엔드 포트: `8080`
- 기본 DB: 인메모리 H2 (`jdbc:h2:mem:hiresync`)
- AI 연동: `OPENAI_API_KEY`; API 주소와 모델은 `application.properties`에서 설정
- SMTP: 메일 기능에서 `MAIL_USERNAME`, `MAIL_PASSWORD` 사용
- 프런트엔드 API 주소: 필요하면 `REACT_APP_BACKEND`로 설정

`.env`, API 키, 계정 비밀번호는 Git에 올리지 마세요. PostgreSQL 운영 전환 설정과 마이그레이션 절차는 확인되지 않았습니다.

## CI 자동화

`.github/workflows/`에 다음 GitHub Actions 워크플로가 있습니다.

- `classroom.yml`: `push`, `repository_dispatch`에서 Markdown 형식 검사 스크립트를 실행합니다.
- `playwright.yml`: `main` 또는 `master` 대상 `push`와 `pull_request`에서 Node 설치, `npm ci`, Playwright 브라우저 설치·테스트, 리포트 보관을 수행합니다.

백엔드 테스트를 실행하는 GitHub Actions 워크플로는 확인되지 않았습니다. 실제 CI 실행 결과는 보장하지 않습니다.

## 컨테이너 및 배포 상태

`backend/Dockerfile`은 Java 21 기반 이미지에서 `:backend:bootJar -x test`로 백엔드 JAR를 만들고 Java 21 런타임 이미지에서 포트 `8080`으로 실행하도록 작성되어 있습니다. 이는 컨테이너 빌드 정의이며 배포의 증거는 아닙니다.

배포 워크플로, 대상 인프라, 운영 URL, 배포 절차는 저장소에서 **확인되지 않았습니다**. 실제 배포 상태는 **확인 필요**입니다.

## 운영 전 확인 항목

- 운영 DB, 스키마 변경, 백업·복구 절차
- 비밀 값 주입과 교체 방법
- AI API 접근성·사용량·오류 모니터링
- HTTPS, 도메인, CORS 허용 목록과 세션 쿠키 설정
- 이미지 저장소 및 배포 자동화
