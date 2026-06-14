# HireSync E2E 테스트 컬렉션 명세서

본 문서는 HireSync 웹 애플리케이션의 품질 검증을 위한 Playwright E2E 테스트 컬렉션의 사양과 시나리오, 그리고 활용되는 기술 및 API를 유형별로 분류하여 기술합니다.

---

## 1. 테스트 컬렉션 유형별 시나리오 명세

### 📂 유형 1: 인증 및 세션 관리 (Authentication & Session)
* **테스트 대상 파일**: [auth.spec.ts](file:///home/hjj/projects/github-hyeonje3402/tests/auth.spec.ts)
* **시나리오**:
  1. **성공 시나리오 (로그인 & 로그아웃)**
     - 사용자가 `/login` 페이지에서 관리자 계정 정보(`admin@example.com` / `1234`)를 입력하고 제출합니다.
     - 로그인 처리 완료 알럿 수락 후 자동으로 `/admin/dashboard` 경로로 리다이렉트되는지 검증합니다.
     - 대시보드의 사이드바에서 '로그아웃' 버튼을 클릭합니다.
     - 로컬 스토리지의 토큰 제거 및 백엔드 로그아웃 API 호출이 완료된 후 다시 `/login` 페이지로 복귀했는지 확인합니다.
  2. **실패 시나리오 (잘못된 자격 증명)**
     - 사용자가 존재하지 않는 가상의 계정 정보 입력 후 로그인을 시도합니다.
     - 로그인 실패 시 노출되는 오류 배너 요소(`color: var(--color-error)`)가 정상적으로 렌더링되며 "로그인에 실패했습니다"라는 에러 피드백을 노출하는지 검증합니다.

---

### 📂 유형 2: 채용 공고 관리 (Job Posting Management)
* **테스트 대상 파일**: [jobs.spec.ts](file:///home/hjj/projects/github-hyeonje3402/tests/jobs.spec.ts)
* **시나리오**:
  1. **실시간 검색어 필터링 검증**
     - 메인 채용 공고 페이지(`/`)에 진입하여 목록 최상단 검색창에 키워드(`TechCorp`)를 입력합니다.
     - 목록의 채용 공고 카드가 입력된 검색어 기반으로 필터링되어 `TechCorp Korea` 사명을 가진 카드 1개만 화면에 노출되는지 검증합니다.
  2. **신규 데이터 소스(URL) 추가 등록 흐름**
     - 관리자로 로그인한 상태에서 채용 데이터 수집 등록 페이지(`/jobs/new`)로 이동합니다.
     - 'URL 입력' 탭을 활성화한 후 네이버 채용 목록 주소를 입력하고 등록 확인을 누릅니다.
     - 등록 성공 수락 후 메인 리스트(`/`) 화면으로 자동 네비게이션되는 전체 수집 요청 라이프사이클을 검증합니다.

---

### 📂 유형 3: 관리자용 사용자 관리 (User Directory Control)
* **테스트 대상 파일**: [users.spec.ts](file:///home/hjj/projects/github-hyeonje3402/tests/users.spec.ts)
* **시나리오**:
  1. **사용자 목록 검색 및 필터링**
     - 관리자 권한으로 로그인 후 `/users-management` 경로에 접근합니다.
     - 사용자 목록 검색창에 `김지훈`을 입력하여 해당하는 유저 1명만 검색 결과 테이블에 노출되며 올바른 이메일 정보가 매칭되는지 검증합니다.
  2. **신규 사용자 등록 프로세스**
     - '사용자정보추가' 버튼을 클릭해 신규 사용자 등록 폼 `/users-management/add`로 이동합니다.
     - 사용자 이름, 이메일, 비밀번호, 연락처 정보를 기재한 뒤 등록을 완료합니다.
     - 성공 배너(`success`) 노출 확인 및 `/users-management` 목록 페이지로 자동 회귀됨을 검증합니다.

---

### 📂 유형 4: 라우팅 및 공개 페이지 이동 (Navigation & Public Routes)
* **테스트 대상 파일**: [navigation.spec.ts](file:///home/hjj/projects/github-hyeonje3402/tests/navigation.spec.ts)
* **시나리오**:
  1. **공개 메뉴 간 라우팅 전환 검증**
     - 사용자가 비로그인 상태로 메인 화면(`/`)에 접속한 상태에서 사이드바의 `Dashboard` 메뉴를 클릭하여 대시보드 화면(`/admin/dashboard`)으로의 경로 네비게이션을 수행합니다.
     - 다시 로그인 페이지(`/login`)로 수동 이동한 후, 하단의 '아이디 등록' 링크를 클릭하여 최종적으로 회원가입 페이지(`/register`)로 부드럽게 네비게이션되는 흐름을 확인합니다.

---

## 2. 사용되는 주요 API 명세

### 🛠️ Playwright API (브라우저 및 요소 조작)

테스트 코드 내에서 브라우저 행동을 시뮬레이션하고 DOM 요소를 검증하기 위해 다음과 같은 Playwright 내장 API를 사용하고 있습니다:

| API 분류 | 사용된 함수 / 속성 | 설명 |
| :--- | :--- | :--- |
| **페이지 제어** | `page.goto(url, { waitUntil })` | 지정된 로컬 호스트 URL로 브라우저 이동 및 로딩 대기 |
| | `page.setViewportSize({ width, height })` | 데스크톱 반응형 레이아웃(사이드바 활성화)을 위한 화면 크기 고정 |
| | `page.waitForURL(pattern, { timeout })` | 라우팅 이동 후 특정 주소 패턴에 매칭될 때까지 대기 |
| | `test.setTimeout(ms)` | 느린 가상 샌드박스 환경에서의 오동작을 예방하기 위한 수행 시간 증가 |
| **요소 탐색 및 조작** | `page.waitForSelector(selector, { state })` | 대상 컴포넌트가 DOM에 보이고 상호작용 가능할 때까지 명시적 대기 |
| | `page.fill(selector, value)` | 이메일, 비밀번호, 검색어 등의 텍스트 필드 값 입력 |
| | `page.click(selector)` | 버튼, 링크, 탭 등의 요소를 마우스 클릭 |
| | `page.locator(selector)` | 특정 클래스명이나 식별자를 가진 다수의 DOM 요소를 가리키는 포인터 획득 |
| **다이얼로그 제어** | `page.on('dialog', callback)` | 브라우저 `alert()` 알림창 발생 시 자동으로 수락(`accept()`) 처리 |
| **단언 및 검증 (Expect)** | `expect(locator).toBeVisible()` | 특정 에러 메시지나 버튼이 화면에 시각적으로 노출되었는지 확인 |
| | `expect(locator).toContainText(str)` | 컴포넌트 내부 텍스트 노드에 기대하는 텍스트가 포함되어 있는지 검사 |
| | `expect(locator).toHaveCount(number)` | 실시간 검색 필터링 적용 후 필터링된 결과 아이템 수가 정확한지 확인 |
| | `expect(page).toHaveURL(url)` | 최종 URL이 목표 주소와 정확히 부합하는지 최종 라우팅 확인 |

---

### 🌐 백엔드 연동 비즈니스 API

프론트엔드 리액트와 연동되어 E2E 시나리오 상에서 호출되는 백엔드(Spring Boot 8080 포트) API는 아래와 같습니다:

1. **인증 서비스 (`/api/v1/auth/*`)**
   - `POST /api/v1/auth/login`: 관리자 계정 자격 증명 검증 및 JWT 토큰 발급.
   - `POST /api/v1/auth/logout`: 세션 만료 처리 및 사용자 아웃.
   - `POST /api/v1/auth/register`: 신규 사용자의 이름, 이메일, 비밀번호, 역할, 연락처 데이터 영속화.
2. **채용 공고 서비스 (`/api/v1/jobs/*`)**
   - `GET /api/v1/jobs`: 페이징 처리된 채용 공고 목록 데이터 반환 (검색 필터링의 근간).
3. **데이터 수집 서비스 (`/api/v1/sources/*`)**
   - `POST /api/v1/sources/url`: 외부 채용 웹사이트 URL로부터 AI 파싱 수집 요청 접수.
4. **인사 관리 서비스 (`/api/v1/users/*`)**
   - `GET /api/v1/users`: 등록된 전체 사용자 리스트 반환 (관리자용).
