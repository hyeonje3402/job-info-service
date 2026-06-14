# Hiresync 핵심 기능 고도화 및 UI 개선 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**목표:** 관리자 검토 단계(DRAFT -> PUBLISHED) 도입과 사람인 스타일의 컴팩트한 구직자 목록 UI를 구현하여 서비스의 신뢰도와 사용성을 높입니다.

**아키텍처:** 
1. 백엔드: `JobPosting` 상태를 `DRAFT`, `PUBLISHED`, `EXPIRED`로 관리하고, 정보 추출 시 `rawContent`를 저장하여 관리자 대조 기능을 지원합니다.
2. 프론트엔드: 구직자용 페이지는 텍스트 중심의 테이블 리스트로 개편하고, 관리자용 페이지는 원본과 추출 데이터를 나란히 비교(Side-by-Side)할 수 있는 검토 화면을 추가합니다.

**기술 스택:** Java 21, Spring Boot 3.5.x, Spring Data JPA, React 19, Vanilla CSS

---

### Task 1: 데이터 모델 및 스키마 업데이트

**Files:**
- Modify: `backend/src/main/java/com/hiresync/models/Source.java`
- Modify: `backend/src/main/java/com/hiresync/models/JobPosting.java`
- Modify: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: Source 엔티티에 rawContent 필드 추가**
```java
// Source.java 에 추가
@Column(name = "raw_content", columnDefinition = "TEXT")
private String rawContent;
```

- [ ] **Step 2: schema.sql 업데이트**
```sql
ALTER TABLE sources ADD COLUMN raw_content TEXT;
```

- [ ] **Step 3: JobPosting 상태 값 주석 업데이트 및 기본값 확인**
```java
// JobPosting.java
private String status; // DRAFT, PUBLISHED, EXPIRED
```

- [ ] **Step 4: 변경 사항 커밋**
```bash
git add backend/src/main/java/com/hiresync/models/Source.java backend/src/main/resources/schema.sql
git commit -m "chore: 원본 대조를 위한 Source.rawContent 필드 추가 및 스키마 업데이트"
```

---

### Task 2: 정보 추출 로직 업데이트 (추출 시 DRAFT 저장)

**Files:**
- Modify: `backend/src/main/java/com/hiresync/controllers/JobPostingController.java`
- Modify: `backend/src/main/java/com/hiresync/controllers/PdfAnalysisController.java`
- Test: `backend/src/test/java/com/hiresync/controllers/JobPostingControllerTest.java`

- [ ] **Step 1: URL 추출 시 원본 텍스트 저장 및 상태를 DRAFT로 변경**
`JobPostingController.registerUrl` 내에서 `source.setRawContent(rawText)`를 호출하고, `posting.setStatus("DRAFT")`로 설정합니다.

- [ ] **Step 2: PDF 추출 시 원본 텍스트 저장 및 상태를 DRAFT로 변경**
`PdfAnalysisController.analyzePdf` 내에서 `source.setRawContent(rawText)`를 호출하고, `data.setStatus("DRAFT")`로 설정합니다.

- [ ] **Step 3: 통합 테스트 코드를 작성하여 DRAFT 저장을 확인합니다.**
`backend/src/test/java/com/hiresync/controllers/JobPostingControllerTest.java`에서 URL 등록 후 상태가 `DRAFT`인지 검증합니다.

- [ ] **Step 4: 테스트 실행 및 성공 확인**
```bash
./gradlew test --tests com.hiresync.controllers.JobPostingControllerTest
```

- [ ] **Step 5: 커밋**
```bash
git add backend/src/main/java/com/hiresync/controllers/JobPostingController.java backend/src/main/java/com/hiresync/controllers/PdfAnalysisController.java
git commit -m "feat: 정보 추출 시 원본 텍스트 저장 및 기본 상태를 DRAFT로 설정"
```

---

### Task 3: 구직자용 목록 UI 개선 (사람인 스타일)

**Files:**
- Modify: `frontend/src/pages/JobListPage.jsx`
- Modify: `frontend/src/pages/JobListPage.css`

- [ ] **Step 1: JobListPage.jsx를 테이블 기반의 컴팩트 레이아웃으로 변경**
카메라 형태가 아닌 텍스트 중심의 리스트로 변경합니다. (회사명, 직무, 지역, 급여, 마감일 순)

- [ ] **Step 2: 공개(PUBLISHED) 상태의 공고만 조회하도록 필터 적용**
`useEffect` 내 API 호출 시 `status=PUBLISHED` 파라미터를 추가합니다.

- [ ] **Step 3: CSS 스타일 업데이트 (JobListPage.css)**
여백을 줄이고 한 줄에 정보가 명확히 보이도록 스타일링합니다.

- [ ] **Step 4: 화면 확인 및 커밋**
```bash
git add frontend/src/pages/JobListPage.*
git commit -m "style: 구직자 목록 화면을 사람인 스타일의 컴팩트 리스트로 개편"
```

---

### Task 4: 관리자용 검토 및 승인 UI 구현 (Comparison View)

**Files:**
- Create: `frontend/src/pages/admin/AdminReviewPage.jsx`
- Modify: `frontend/src/pages/admin/AdminDashboard.jsx` (링크 연결)
- Modify: `frontend/src/services/api.js` (상태 업데이트 API 추가)

- [ ] **Step 1: AdminReviewPage.jsx 구현**
좌측에는 `source.rawContent`를 표시하고, 우측에는 해당 `JobPosting`의 데이터를 수정할 수 있는 폼을 배치합니다.

- [ ] **Step 2: 승인(Approve) 기능 연동**
`PATCH /api/v1/jobs/{id}/status`를 호출하여 상태를 `PUBLISHED`로 변경하는 기능을 추가합니다.

- [ ] **Step 3: 관리자 대시보드에서 DRAFT 목록만 따로 볼 수 있는 필터 추가**

- [ ] **Step 4: 커밋**
```bash
git add frontend/src/pages/admin/ frontend/src/services/api.js
git commit -m "feat: 원본 대조 및 승인 기능을 포함한 관리자 검토 페이지 구현"
```

---

### Task 5: 최종 검증 및 데이터 마이그레이션

**Files:**
- Modify: `backend/src/main/java/com/hiresync/config/DataInitializer.java`

- [ ] **Step 1: DataInitializer 업데이트**
`DataInitializer.java`에서 더미 데이터의 상태를 `PUBLISHED`로 변경하여 초기 화면에서 보이게 합니다.

- [ ] **Step 2: 전체 시스템 테스트**
PDF 업로드 -> DRAFT 생성 확인 -> 관리자 페이지 대조 및 수정 -> 승인 -> 구직자 화면 노출 확인 순으로 테스트합니다.

- [ ] **Step 3: 최종 커밋**
```bash
git add backend/src/main/java/com/hiresync/config/DataInitializer.java
git commit -m "chore: 초기 데이터 상태를 PUBLISHED로 변경 및 최종 검증 완료"
```
