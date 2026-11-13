# 설치 및 개발

이 안내는 저장소의 Gradle·npm 설정을 기준으로 합니다. 배포 환경 정보는 [운영 문서](../operations/README.md)를 참고하세요.

## 요구 도구

- Java 21 (Gradle Wrapper 사용)
- Node.js 및 npm (버전 범위는 프로젝트 설정에서 고정하지 않음)
- AI 기반 PDF 분석을 사용할 경우 `OPENAI_API_KEY`와 호환 API 서비스 접근 정보

## 환경 변수

저장소 루트의 `.env.example`과 애플리케이션 설정을 확인해 로컬 환경을 준비합니다. 비밀값이 든 `.env` 파일은 커밋하지 마세요.

- `OPENAI_API_KEY`: 백엔드 AI 연동
- `MAIL_USERNAME`, `MAIL_PASSWORD`: SMTP 메일 기능에 필요할 때 사용
- `REACT_APP_BACKEND`: 프런트엔드 백엔드 주소 (예: `http://localhost:8080`)

백엔드 기본 DB는 인메모리 H2이며 포트는 `8080`입니다. AI 서버 주소와 모델 이름은 `backend/src/main/resources/application.properties`에서 확인하세요.

## 백엔드 실행

저장소 루트에서 실행합니다.

```bash
./gradlew :backend:bootRun
```

Windows에서는 `gradlew.bat :backend:bootRun`을 사용할 수 있습니다. Swagger UI 기본 경로는 `http://localhost:8080/swagger-ui/index.html`입니다.

## 프런트엔드 실행

```bash
cd frontend
npm ci
npm start
```

개발 서버 기본 주소는 `http://localhost:3000`입니다. 백엔드 주소가 기본값과 다르면 `REACT_APP_BACKEND`를 설정합니다.

## 테스트 명령

```bash
./gradlew :backend:test
cd frontend && npm test
npx playwright test
```

Playwright 브라우저 설치가 필요하면 `npx playwright install`을 실행합니다.
