# Tasks: HireSync 채용 정보 서비스

**Input**: Design documents from `/specs/001-job-info-service/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: As per the Project Constitution (Principle I: TDD), tests are MANDATORY for all feature implementations. All tests MUST be written first and fail before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions
- **Backend**: `backend/src/main/java/com/hiresync/`, `backend/src/test/java/com/hiresync/`
- **Frontend**: `frontend/src/`, `frontend/tests/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create project structure for backend (Spring Boot) and frontend (React)
- [X] T002 [P] Initialize Spring Boot 3.5.x project with dependencies (Web, JPA, MySQL, Security, Mail) in `backend/`
- [X] T003 [P] Initialize React project with JavaScript (no TS) and axios in `frontend/`
- [X] T004 [P] Configure linting and formatting for both projects
- [X] T005 [P] Setup MySQL database schema `hiresync` per `data-model.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure and security (JWT)

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T006 Implement `Admin` entity and repository in `backend/src/main/java/com/hiresync/models/Admin.java`
- [X] T007 [P] Create `AdminRepository` in `backend/src/main/java/com/hiresync/repositories/AdminRepository.java`
- [X] T008 [P] Write unit tests for JWT token generation and validation in `backend/src/test/java/com/hiresync/security/JwtTokenTest.java`
- [X] T009 Implement JWT security configuration in `backend/src/main/java/com/hiresync/security/JwtUtils.java`
- [X] T010 [P] Write integration tests for Admin login endpoint in `backend/src/test/java/com/hiresync/controllers/AdminAuthControllerTest.java`
- [X] T011 Implement `AdminAuthController` for `/api/v1/admin/login` in `backend/src/main/java/com/hiresync/controllers/AdminAuthController.java`

**Checkpoint**: Foundation ready - JWT Auth is functional.

---

## Phase 3: User Story 1 - 채용 정보 조회 (구직자) (Priority: P1) 🎯 MVP

**Goal**: 구직자가 채용 정보 목록 및 상세 내용을 조회할 수 있음

**Independent Test**: 구직자 권한(토큰 없음)으로 `/api/v1/job-postings` 호출 시 데이터 목록이 반환되는지 확인

### Tests for User Story 1 ⚠️

- [X] T012 [P] [US1] Write unit tests for `JobPostingService.findAll()` in `backend/src/test/java/com/hiresync/services/JobPostingServiceTest.java`
- [X] T013 [P] [US1] Write integration tests for `JobPostingController` GET endpoints in `backend/src/test/java/com/hiresync/controllers/JobPostingControllerTest.java`
- [ ] T014 [P] [US1] Write frontend tests for Job List component in `frontend/tests/JobListPage.test.js`

### Implementation for User Story 1

- [ ] T015 [P] [US1] Create `JobPosting` entity in `backend/src/main/java/com/hiresync/models/JobPosting.java`
- [ ] T016 [P] [US1] Create `JobPostingRepository` in `backend/src/main/java/com/hiresync/repositories/JobPostingRepository.java`
- [ ] T017 [US1] Implement `JobPostingService` list/detail logic in `backend/src/main/java/com/hiresync/services/JobPostingService.java`
- [ ] T018 [US1] Implement `JobPostingController` public endpoints in `backend/src/main/java/com/hiresync/controllers/JobPostingController.java`
- [ ] T019 [US1] Implement Frontend Job List page in `frontend/src/pages/JobListPage.js`
- [ ] T020 [US1] Implement Frontend Job Detail page in `frontend/src/pages/JobDetailPage.js`

**Checkpoint**: User Story 1 (MVP) complete and testable.

---

## Phase 4: User Story 2 - 관리자 채용 정보 등록 (PDF/URL) (Priority: P2)

**Goal**: 관리자가 PDF 업로드 및 URL 등록을 통해 채용 정보를 자동 등록함

**Independent Test**: 관리자 토큰으로 PDF 업로드 시 `JobPosting`과 `ExtractionLog`가 자동 생성되는지 확인

### Tests for User Story 2 ⚠️

- [ ] T021 [P] [US2] Write unit tests for PDF extraction (Apache PDFBox) in `backend/src/test/java/com/hiresync/services/PdfExtractionServiceTest.java`
- [ ] T022 [P] [US2] Write unit tests for URL scraping (Jsoup) in `backend/src/test/java/com/hiresync/services/UrlScrapingServiceTest.java`
- [ ] T023 [P] [US2] Write integration tests for admin upload/register endpoints in `backend/src/test/java/com/hiresync/controllers/AdminJobPostingControllerTest.java`

### Implementation for User Story 2

- [ ] T024 [P] [US2] Create `ExtractionLog` entity and repository in `backend/src/main/java/com/hiresync/models/ExtractionLog.java`
- [ ] T025 [US2] Implement `PdfExtractionService` using PDFBox in `backend/src/main/java/com/hiresync/services/PdfExtractionService.java`
- [ ] T026 [US2] Implement `UrlScrapingService` using Jsoup in `backend/src/main/java/com/hiresync/services/UrlScrapingService.java`
- [ ] T027 [US2] Implement Admin Job Registration Controller in `backend/src/main/java/com/hiresync/controllers/AdminJobPostingController.java`
- [ ] T028 [US2] Implement Frontend Admin Upload page in `frontend/src/pages/AdminUploadPage.js`

**Checkpoint**: User Story 2 complete.

---

## Phase 5: User Story 4 - 채용 정보 관리 (수정/삭제) (Priority: P2)

**Goal**: 관리자가 등록된 정보를 직접 수정하거나 삭제할 수 있음

**Independent Test**: 관리자 페이지에서 수정 후 구직자 화면에 즉시 반영되는지 확인

### Tests for User Story 4 ⚠️

- [ ] T029 [P] [US4] Write unit tests for Update/Delete in `JobPostingService`
- [ ] T030 [P] [US4] Write integration tests for PUT/DELETE endpoints in `AdminJobPostingControllerTest.java`

### Implementation for User Story 4

- [ ] T031 [US4] Implement Update/Delete logic in `JobPostingService.java`
- [ ] T032 [US4] Add PUT/DELETE mapping to `AdminJobPostingController.java`
- [ ] T033 [US4] Implement Frontend Admin Management List in `frontend/src/pages/AdminManagementPage.js`
- [ ] T034 [US4] Implement Frontend Edit Form component in `frontend/src/components/JobEditForm.js`

**Checkpoint**: User Story 1, 2, 4 complete.

---

## Phase 6: User Story 3 - 이메일을 통한 정보 등록 (Priority: P3)

**Goal**: 특정 제목의 이메일 수신 시 자동으로 정보 추출 및 등록

**Independent Test**: "[연암취업정보]" 제목의 메일 발송 시 DB에 새로운 공고가 추가되는지 확인

### Tests for User Story 3 ⚠️

- [ ] T035 [P] [US3] Write unit tests for Email parsing and filter logic in `backend/src/test/java/com/hiresync/services/EmailProcessingServiceTest.java`

### Implementation for User Story 3

- [ ] T036 [US3] Configure Spring Mail Receiver for IMAP/POP3 in `backend/src/main/resources/application.properties`
- [ ] T037 [US3] Implement `EmailProcessingService` to filter and extract URL from body in `backend/src/main/java/com/hiresync/services/EmailProcessingService.java`

**Checkpoint**: All core User Stories complete.

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] T038 [P] Documentation updates in `README.md` and `docs/`
- [ ] T039 Code cleanup and refactoring (Backend/Frontend)
- [ ] T040 [P] Security audit of Admin endpoints
- [ ] T041 Run `quickstart.md` validation from scratch

---

## Dependencies & Execution Order

1. **Setup (Phase 1)**: Must be first.
2. **Foundational (Phase 2)**: Blocks all Admin-related stories (US2, US3, US4).
3. **US1 (Phase 3)**: Independent, can start after Foundation.
4. **US2 (Phase 4)**: Can start after Foundation.
5. **US4 (Phase 5)**: Depends on US2 (needs data to manage).
6. **US3 (Phase 6)**: Independent, can start after Foundation.

---

## Parallel Opportunities

- T002 (Backend) and T003 (Frontend) can run in parallel.
- T008 (JWT Auth) and T012 (US1 Service Test) can run in parallel.
- US1 (Phase 3) and US2 (Phase 4) can run in parallel by different developers once Foundation is ready.

---

## Implementation Strategy

1. **MVP First**: Setup -> Foundational -> US1 (Job List/Detail). This provides a working service for seekers.
2. **Incremental**: Add US2 (Admin Registration) to populate data.
3. **Control**: Add US4 (Management) to maintain data quality.
4. **Automation**: Add US3 (Email) as a convenience feature.
