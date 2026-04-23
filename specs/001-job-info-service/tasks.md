# Tasks: HireSync 채용 정보 서비스

**Input**: Design documents from `/specs/001-job-info-service/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md

---

## Phase 1: Setup (Build System Migration)

**Purpose**: Maven에서 Gradle 8+ 및 Java 21 환경으로 전환 (PDFBox 제거)

- [X] T001 Initialize Gradle 8+ wrapper and structure in repository root
- [X] T002 Migrate `pom.xml` dependencies to `build.gradle` (Spring Boot 3.5.x, Java 21) 및 PDFBox 의존성 제거
- [X] T003 Configure Java 21 toolchain and Gradle build properties in `backend/build.gradle`
- [X] T004 [P] Setup React 19 project structure in `frontend/` and update `package.json`
- [X] T005 [P] Configure ESLint and Prettier for Java and JavaScript in project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 모든 사용자 스토리에 필요한 핵심 인프라 구축

- [X] T006 Setup PostgreSQL database schema for `JobPosting` and `ExtractionLog` in `backend/src/main/resources/schema.sql`
- [X] T007 Implement Base Entity and JPA repositories in `backend/src/main/java/com/hiresync/repositories/`
- [X] T008 [P] Configure Spring Security with JWT (Java 21 compatible) in `backend/src/main/java/com/hiresync/security/`
- [X] T009 [P] Setup Spring AI with OpenAI API key and basic chat client configuration in `backend/src/main/resources/application.properties`
- [X] T010 Configure SpringDoc OpenAPI 2.x for API documentation (Swagger UI) in `backend/src/main/java/com/hiresync/config/`
- [X] T011 Setup global exception handling and logging infrastructure in `backend/src/main/java/com/hiresync/exception/`

**Checkpoint**: 인프라 준비 완료 - 각 사용자 스토리별 병렬 개발 가능

---

## Phase 3: User Story 1 - 채용 정보 조회 (Priority: P1) 🎯 MVP

**Goal**: 구직자가 채용 공고 목록을 조회하고 상세 내용을 확인할 수 있음

**Independent Test**: `backend/src/test/`에서 목록 조회 및 상세 조회 API 테스트 수행 후 프론트엔드 연동 확인

### Tests for User Story 1

- [X] T012 [P] [US1] Create integration test for `GET /api/v1/jobs` in `backend/src/test/java/com/hiresync/controllers/JobPostingControllerTest.java`
- [X] T013 [P] [US1] Create unit test for `JobPostingService.findAll()` in `backend/src/test/java/com/hiresync/services/JobPostingServiceTest.java`

### Implementation for User Story 1

- [X] T014 [US1] Implement `JobPosting` entity and DTOs in `backend/src/main/java/com/hiresync/models/`
- [X] T015 [US1] Implement `JobPostingController.getAll()` and `getById()` in `backend/src/main/java/com/hiresync/controllers/JobPostingController.java`
- [X] T016 [US1] Create `JobListPage` and `JobDetailPage` in `frontend/src/pages/` (React 19)
- [X] T017 [US1] Add basic styling and responsive layout for job listings in `frontend/src/components/`

**Checkpoint**: 구직자용 기본 조회 기능 완성 및 독립 테스트 통과

---

## Phase 4: User Story 2 - 관리자 채용 정보 등록 (PDF/URL) (Priority: P2)

**Goal**: OpenAI API를 사용하여 PDF 바이너리 직접 분석 및 URL에서 정보 추출

**Independent Test**: 관리자 권한으로 PDF 업로드 시 OpenAI 분석 성공 및 `JobPosting` 자동 생성 확인

### Tests for User Story 2

- [X] T018 [P] [US2] Create integration test for PDF upload and AI extraction mock in `backend/src/test/java/com/hiresync/controllers/PdfAnalysisControllerTest.java`
- [X] T019 [P] [US2] Create unit test for URL scraping with JSoup in `backend/src/test/java/com/hiresync/services/UrlScrapingServiceTest.java`

### Implementation for User Story 2

- [X] T020 [US2] Implement OpenAI Document Analysis service for PDF files (direct binary upload via Spring AI) in `backend/src/main/java/com/hiresync/services/PdfAnalysisService.java`
- [X] T021 [US2] Implement OpenAI extraction engine for URL/Email content structuring in `backend/src/main/java/com/hiresync/extraction/OpenAiExtractionEngine.java`
- [X] T022 [US2] Implement URL scraping service using JSoup 1.17.x in `backend/src/main/java/com/hiresync/services/UrlScrapingService.java`
- [X] T023 [US2] Implement security filtering and sanitization (SR-004) using JSoup Cleaner in `backend/src/main/java/com/hiresync/services/UrlScrapingService.java`
- [X] T024 [US2] Implement validation for PDF file size and page limits in `backend/src/main/java/com/hiresync/services/PdfAnalysisService.java`
- [X] T024 [US2] Implement retry logic and response consistency for AI extraction in `backend/src/main/java/com/hiresync/extraction/`
- [X] T025 [US2] Create admin upload UI in `frontend/src/pages/admin/` using React 19 Actions
- [X] T026 [US2] Implement automatic registration logic (Extract -> Save -> Publish) in `backend/src/main/java/com/hiresync/services/JobPostingService.java`

---

## Phase 5: User Story 3 - 이메일 통한 정보 등록 (Priority: P3)

**Goal**: "[연암취업정보]" 제목의 이메일 수신 시 자동 정보 추출 및 등록

**Independent Test**: Mock JavaMailSender를 사용하여 이메일 수신 트리거 시 정보 등록 확인

### Tests for User Story 3

- [X] T027 [P] [US3] Create unit test for email subject filtering logic in `backend/src/test/java/com/hiresync/services/EmailServiceTest.java`
- [X] T028 [US3] Integration test for email-to-extraction-engine flow in `backend/src/test/java/com/hiresync/integration/EmailIntegrationTest.java`

### Implementation for User Story 3

- [X] T029 [US3] Configure Spring Mail for IMAP/POP3 receiving in `backend/src/main/java/com/hiresync/config/MailConfig.java`
- [X] T030 [US3] Implement email body parsing and URL detection logic in `backend/src/main/java/com/hiresync/services/EmailService.java`
- [X] T031 [US3] Integrate email service with Phase 4's extraction engine in `backend/src/main/java/com/hiresync/services/EmailService.java`

---

## Phase 6: User Story 4 - 채용 정보 관리 (수정/삭제) (Priority: P2)

**Goal**: 관리자가 등록된 정보를 사후에 수정하거나 삭제함

### Tests for User Story 4

- [X] T032 [P] [US4] Integration test for `PUT` and `DELETE` endpoints in `backend/src/test/java/com/hiresync/controllers/AdminControllerTest.java`

### Implementation for User Story 4

- [X] T033 [US4] Implement `PUT /api/admin/job-postings/{id}` and `DELETE` logic in `backend/src/main/java/com/hiresync/controllers/JobPostingController.java`
- [X] T034 [US4] Create Admin Dashboard UI for managing (Edit/Delete) existing posts in `frontend/src/pages/admin/`
- [X] T035 [US4] Add authority validation for admin-only operations in `backend/src/main/java/com/hiresync/security/WebSecurityConfig.java`

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T036 [P] Update `quickstart.md` with Java 21 and Gradle build instructions in `specs/001-job-info-service/quickstart.md`
- [X] T037 Optimize OpenAI prompts for higher extraction accuracy in PDF analysis in `backend/src/main/resources/prompts/`
- [X] T038 [P] Final security hardening and API documentation review in `backend/src/main/java/com/hiresync/config/SecurityConfig.java`
- [X] T039 Implement token usage logging for OpenAI API in `backend/src/main/java/com/hiresync/extraction/`
- [X] T040 Perform final end-to-end validation of all scenarios in project root

---

## Dependencies & Execution Order

1. **Phase 1 & 2**: 필수 인프라 구축 (Blocking)
2. **Phase 3 (US1)**: 구직자 조회 기능 완성 (P1)
3. **Phase 4 (US2)**: AI 기반 PDF/URL 추출 엔진 구현 (US1 의존)
4. **Phase 5 (US3)**: 이메일 연동 (US2 엔진 재사용)
5. **Phase 6 (US4)**: 사후 관리 기능

---

## Implementation Strategy

- **OpenAI PDF 직접 분석**: PDFBox를 사용하지 않고 Spring AI를 통해 바이너리를 직접 전달하여 환각 현상을 줄이고 정확도를 높임.
- **Incremental MVP**: US1(조회)을 먼저 완성하여 결과물을 빠르게 시각화함.
- **가상 스레드 활용**: 다수의 URL 스크래핑 및 외부 API 호출 시의 성능 확보.
- **비결정적 응답 대응**: AI 응답의 일관성을 위한 유효성 검사 및 재시도 로직 포함.
