# HireSync - AI 기반 채용 정보 관리 서비스 (Job Info Service)

HireSync는 AI(OpenAI/LLM)를 활용하여 다양한 소스(PDF 문서, 채용 사이트 URL 등)로부터 채용 공고 데이터를 자동으로 추출하고 통합 관리하는 웹 서비스입니다.

---

## 🚀 주요 기능

- **AI 채용 공고 자동 추출**
  - **PDF 분석**: 채용 공고 PDF 파일에서 텍스트를 추출하고 LLM으로 핵심 항목(기업명, 직무, 근무지, 자격요건, 마감일 등)을 구조화
  - **웹 URL 스크래핑**: 채용 사이트(사람인 등) URL에서 본문 데이터를 수집 및 AI 자동 파싱
- **채용 공고 통합 관리**
  - 공고 목록 조회(페이징, 키워드 검색, 최신 등록순/마감일 임박순 정렬)
  - 공고 상태 실시간 변경 (`PENDING`, `APPROVED`, `REJECTED`, `CLOSED`) 및 상세 조회·수정·삭제
- **역할 기반 접근 제어 (RBAC) 및 세션 인증**
  - `JSESSIONID` 쿠키 기반 세션 인증 및 권한별 메뉴·라우트 접근 제어 (`ADMIN` / `GENERAL`)
  - 비인가 접근 시 로그인 화면(`/login?error=unauthorized`) 자동 리다이렉트
- **관리자 대시보드 및 시스템 관리**
  - 채용 공고 현황 요약 대시보드 (`/admin/dashboard`)
  - 사용자 계정 조회·생성·수정·삭제 관리 (`/users-management`)
  - AI 모델/프롬프트 설정 및 API 토큰 사용량 로그 모니터링 (`/settings`)
- **반응형 UI 디자인**
  - 데스크톱 사이드바 및 모바일 상/하단 네비게이션 바 지원

---

## 🛠 기술 스택

### Backend
- **Java 21** / **Spring Boot 3.5.x**
- **Spring Security**: 세션(`JSESSIONID`) 기반 인증 및 권한 관리
- **Spring AI**: OpenAI 호환 LLM API 기반 채용 정보 추출
- **Spring Data JPA** / **H2 Database**: 데이터 영속성 관리 (In-Memory / File 모드 지원)
- **Apache PDFBox 3.0.x**: PDF 문서 텍스트 추출
- **JSoup 1.17.x**: 웹 페이지 크롤링 및 HTML 파싱
- **SpringDoc OpenAPI 2.x (Swagger)**: REST API 문서 자동화

### Frontend
- **React 19** / **React Router 7**
- **Axios**: 쿠키/세션 연동(`withCredentials`) 및 인터셉터 기반 API 통신
- **Vanilla CSS**: 디자인 시스템(`DESIGN.md`) 기반 반응형 스타일링

### Testing
- **JUnit 5 & MockMvc**: 백엔드 단위/통합 테스트
- **Playwright**: 브라우저 E2E 시나리오 테스트 (`tests/test-1.ts` ~ `tests/test-14.ts`)
- **Newman (Postman CLI)**: API E2E 컬렉션 테스트

---

## 📋 사전 요구사항 (Prerequisites)

- **Java**: JDK 21 이상
- **Node.js**: v18 이상 및 npm 9 이상
- **OpenAI API Key**: OpenAI API 또는 호환 LLM 서버 API 키

---

## 🏃 실행 방법

### 1. Backend 실행
1. `backend/src/main/resources/application.properties` 설정을 확인합니다.
   - `spring.ai.openai.api-key` 또는 환경 변수 설정 필요
2. 백엔드 서버를 실행합니다:
   ```bash
   ./gradlew :backend:bootRun
   ```
- **백엔드 기본 주소**: `http://localhost:8080`
- **H2 Console**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:hiresync` (또는 설정에 따라 `jdbc:h2:file:./data/hiresync`)
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

### 2. Frontend 실행
1. `frontend` 디렉토리로 이동하여 의존성 패키지를 설치합니다:
   ```bash
   cd frontend
   npm install
   ```
2. 환경 변수 설정 (`frontend/.env` 파일 생성 또는 확인):
   ```env
   REACT_APP_BACKEND=http://localhost:8080
   ```
3. 프론트엔드 개발 서버를 실행합니다:
   ```bash
   npm start
   ```
- **프론트엔드 접속 주소**: `http://localhost:3000`

---

## 👥 초기 테스트 계정 정보

서버 구동 시 `DataInitializer`를 통해 아래 기본 계정과 샘플 채용 공고(`SK키파운드리`)가 자동 생성됩니다.

| 역할 (Role) | 이름 | 이메일 (ID) | 비밀번호 | 로그인 후 이동 경로 및 권한 |
| :--- | :--- | :--- | :--- | :--- |
| **관리자 (`ADMIN`)** | Admin User | `admin@example.com` | `1234` | **관리자 대시보드 (`/admin/dashboard`)** 즉시 이동<br>· 전체 메뉴 활성화 (대시보드, 채용공고 관리, 사용자 관리, 시스템 설정) |
| **일반 사용자 (`GENERAL`)** | 일반사용자 | `tester@example.com` | `1234!` | **채용공고 목록 (`/`)** 즉시 이동<br>· 일반 사용자용 메뉴 활성화 (채용공고 목록만 노출, 관리자 경로 접근 차단) |
| **일반 사용자 (`GENERAL`)** | General User | `newuser@example.com` | `1234` | **채용공고 목록 (`/`)** 즉시 이동 |
| **일반 사용자 (`GENERAL`)** | 김지훈 | `jihoon.kim@example.com` | `1234` | **채용공고 목록 (`/`)** 즉시 이동 |

---

## 🔍 프로젝트 구조

```text
.
├── backend/                  # Spring Boot 백엔드 애플리케이션
│   └── src/
│       ├── main/java/com/hiresync/
│       │   ├── config/       # 초기 데이터(DataInitializer), CORS/App 설정
│       │   ├── controllers/  # REST API 컨트롤러 (Auth, JobPosting, PdfAnalysis, JobImport, User, ApiUsageLog)
│       │   ├── extraction/   # OpenAI 기반 채용 데이터 추출 엔진
│       │   ├── models/       # JPA 엔티티 (User, JobPosting, ApiUsageLog 등)
│       │   ├── repositories/ # Spring Data JPA 리포지토리
│       │   ├── security/     # Spring Security 및 세션 인증 설정
│       │   └── services/     # 비즈니스 로직 (PDF 분석, URL 스크래핑, 사람인 크롤러, 사용자 관리)
│       └── test/             # 백엔드 JUnit 단위/통합 테스트
├── frontend/                 # React 19 프론트엔드 애플리케이션
│   └── src/
│       ├── components/       # 공용 컴포넌트 (반응형 Sidebar 등)
│       ├── pages/            # 주요 페이지 (로그인, 회원가입, 채용공고 목록/등록/수정/상세, 시스템 설정, 관리자 페이지)
│       └── services/         # Axios 인스턴스 및 API 호출 모듈
├── tests/                    # Playwright 브라우저 E2E 시나리오 테스트 (test-1.ts ~ test-14.ts 등)
├── specs/                    # 기능 명세서(Spec), API 계약(Contract), 태스크 문서
├── docs/                     # 제품 요구사항 정의서(PRD)
└── DESIGN.md                 # UI/UX 디자인 시스템 가이드라인
```

---

## 🧪 테스트 실행

### Backend 단위/통합 테스트 (Gradle)
```bash
./gradlew :backend:test
```

### API E2E 테스트 (Newman)
```bash
# 서버가 실행 중인 상태에서 수행
newman run test-12.json
```

### 브라우저 시나리오 E2E 테스트 (Playwright)
```bash
# 전체 시나리오 테스트 실행
npx playwright test

# 개별 시나리오 테스트 실행 예시 (test-1.ts ~ test-14.ts)
npx playwright test tests/test-12.ts
npx playwright test tests/test-13.ts
npx playwright test tests/test-14.ts

# 테스트 결과 리포트 확인
npx playwright show-report
```
