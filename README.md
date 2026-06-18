# HireSync - 채용 정보 서비스 (Job Info Service)

HireSync는 AI(OpenAI/LLM)를 활용하여 다양한 소스(PDF, 웹 페이지)로부터 채용 공고 데이터를 자동으로 추출하고 관리하는 서비스입니다.

## 🚀 주요 기능
- **AI 데이터 추출**: PDF 공고 파일 및 URL(사람인 등)에서 채용 정보를 자동 파싱
- **채용 공고 관리**: 추출된 데이터의 검토, 승인 및 목록 제공
- **자동화된 워크플로우**: 구직자용 대시보드 및 관리자용 관리 도구 제공

## 🛠 기술 스택
### Backend
- **Java 21** / **Spring Boot 3.5.0**
- **Spring AI**: OpenAI API 기반 데이터 추출
- **Spring Data JPA** / **H2 Database** (개발/테스트용)
- **PDFBox**: PDF 텍스트 추출
- **JSoup**: 웹 스크래핑 및 HTML 파싱
- **SpringDoc OpenAPI (Swagger)**: API 문서화

### Frontend
- **React 19**
- **React Router 7**
- **Axios**: API 통신

## 📋 Prerequisites
- **Java**: JDK 21
- **Node.js**: v18+ 및 npm 9+
- **OpenAI API Key**: (또는 호환되는 LLM 서버 API 키)

## 🏃 실행 방법

### Backend 실행
1. `backend/src/main/resources/application.properties` 설정 확인
   - `spring.ai.openai.api-key` 설정 필요
2. 서버 실행:
   ```bash
   ./gradlew :backend:bootRun
   ```
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:hiresync` (또는 `jdbc:h2:file:./data/hiresync`)
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

### Frontend 실행
1. `frontend` 디렉토리로 이동하여 필요한 패키지를 설치합니다.
   ```bash
   cd frontend
   npm install
   ```
2. 환경 변수 설정 (`frontend/.env` 파일 생성 또는 확인):
   - 백엔드 API 주소를 설정합니다. (기본 포트: 8080)
   ```env
   REACT_APP_BACKEND=http://localhost:8080
   ```
3. 개발 서버를 실행합니다:
   ```bash
   npm start
   ```
- **프론트엔드 접속 주소**: http://localhost:3000

## 👥 테스트용 계정 정보

권한 및 역할에 따라 접근할 수 있는 페이지와 기능이 제한됩니다.

| 역할 (Role) | 이메일 (ID) | 비밀번호 | 리다이렉션 경로 및 제공 기능 |
| :--- | :--- | :--- | :--- |
| **관리자 (ADMIN)** | `admin@example.com` | `1234` | **관리자 대시보드 (`/admin/dashboard`)** 로 즉시 이동<br>· 전체 메뉴 활성화 (Dashboard, Job Listings, User Management, Settings) |
| **일반 사용자 (GENERAL)** | `tester@example.com` | `1234!` | **채용공고 목록 (`/`)** 으로 즉시 이동<br>· 일반 사용자용 메뉴 활성화 (Job Listings만 노출, 관리자 페이지 접근 자동 차단) |

## 🔍 프로젝트 구조
- `backend/`: Spring Boot 프로젝트
- `frontend/`: React 프로젝트
- `specs/`: 프로젝트 설계 문서 및 태스크 관리
- `docs/`: 비즈니스 요구사항(PRD) 문서

## 🧪 테스트 실행
### API E2E 테스트 (Newman)
```bash
# 서버 실행 중인 상태에서
newman run test-12.json
```

### 브라우저 시나리오 E2E 테스트 (Playwright)
```bash
# 전체 시나리오 테스트 실행
npx playwright test

# 특정 시나리오 테스트 실행 (예: 일반 사용자 권한 검사)
npx playwright test tests/test-13.ts
npx playwright test tests/test-14.ts

# 테스트 결과 리포트 확인
npx playwright show-report
```
