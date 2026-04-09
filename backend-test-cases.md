# 백엔드 통합 테스트 시나리오 (BDD 스타일)

이 문서는 `JobPostingController.java`와 관련된 API들에 대한 통합 테스트 시나리오를 Given-When-Then 구조로 정의합니다.

## 1. 채용 공고 관리 (JobPostingController)

### 1.1 Happy Path (정상 케이스)

#### 시나리오 1: 전체 채용 공고 목록 조회
*   **Given**: 시스템에 다수의 채용 공고 데이터가 저장되어 있음
*   **When**: `GET /api/v1/jobs` 요청을 보냄 (페이지네이션 매개변수 포함)
*   **Then**: HTTP 200 OK 응답과 함께 `ApiResponse` 내 `data` 필드에 페이징 처리된 채용 공고 목록이 반환됨

#### 시나리오 2: 특정 ID의 채용 공고 상세 조회
*   **Given**: ID가 `1`인 채용 공고가 존재함
*   **When**: `GET /api/v1/jobs/1` 요청을 보냄
*   **Then**: HTTP 200 OK 응답과 함께 해당 ID의 상세 정보가 `ApiResponse`의 `data`에 담겨 반환됨

#### 시나리오 3: URL 등록 및 AI 정보 추출 성공
*   **Given**: 유효한 채용 공고 URL이 제공됨
*   **When**: `POST /api/v1/url` 요청으로 URL을 전달함
*   **Then**: 
    *   시스템은 URL을 스크래핑하고 AI를 통해 정보를 추출함
    *   추출된 정보가 DB에 저장됨
    *   HTTP 201 Created 응답과 함께 생성된 첫 번째 채용 공고 데이터가 반환됨
    *   `Source` 및 `ExtractionLog` 테이블에 처리 기록이 남음

#### 시나리오 4: 관리자 권한으로 채용 공고 수정
*   **Given**: 
    *   관리자 권한으로 인증된 상태임
    *   수정할 대상 채용 공고(ID: 1)가 존재함
*   **When**: `PUT /api/v1/jobs/1` 요청으로 수정된 데이터(회사명, 위치 등)를 보냄
*   **Then**: HTTP 200 OK 응답과 함께 수정된 정보가 반환되며 DB에 반영됨

#### 시나리오 5: 관리자 권한으로 채용 공고 삭제
*   **Given**: 
    *   관리자 권한으로 인증된 상태임
    *   삭제할 대상 채용 공고(ID: 1)가 존재함
*   **When**: `DELETE /api/v1/jobs/1` 요청을 보냄
*   **Then**: HTTP 200 OK 응답을 받으며 DB에서 해당 데이터가 삭제됨

---

### 1.2 Exception Scenarios (예외 케이스)

#### 시나리오 1: 존재하지 않는 채용 공고 조회
*   **Given**: 시스템에 ID가 `999`인 공고가 없음
*   **When**: `GET /api/v1/jobs/999` 요청을 보냄
*   **Then**: HTTP 404 Not Found 응답을 받음

#### 시나리오 2: 잘못된 형식의 URL 등록 요청
*   **Given**: 유효하지 않은 URL 형식이 제공됨 (예: "not-a-url")
*   **When**: `POST /api/v1/url` 요청을 보냄
*   **Then**: HTTP 500 Internal Server Error (또는 400 Bad Request) 응답과 함께 에러 메시지가 반환됨

#### 시나리오 3: AI 정보 추출 실패 (데이터 없음)
*   **Given**: URL은 정상이나, AI 엔진이 유효한 채용 정보를 찾지 못함
*   **When**: `POST /api/v1/url` 요청을 보냄
*   **Then**: 
    *   HTTP 200 OK 응답을 받지만 `ApiResponse`의 `status`는 "error"이고 "No data extracted from URL" 메시지가 포함됨
    *   `Source`의 상태는 `FAILED`로 업데이트됨

#### 시나리오 4: 일반 사용자 권한으로 수정/삭제 시도
*   **Given**: 관리자가 아닌 일반 사용자(ROLE_USER) 권한으로 인증됨
*   **When**: `PUT` 또는 `DELETE /api/v1/jobs/1` 요청을 보냄
*   **Then**: HTTP 403 Forbidden 응답을 받음

---

### 1.3 Edge Cases (경계 조건 및 특이 케이스)

#### 시나리오 1: 채용 공고가 전혀 없는 상태에서 목록 조회
*   **Given**: DB에 저장된 채용 공고가 하나도 없음
*   **When**: `GET /api/v1/jobs` 요청을 보냄
*   **Then**: HTTP 200 OK 응답을 받으며, `data.content`는 빈 배열(`[]`)임

#### 시나리오 2: 매우 긴 스크래핑 텍스트 처리
*   **Given**: 텍스트 양이 극도로 많은 URL(예: 방대한 이용약관 페이지 등)이 제공됨
*   **When**: `POST /api/v1/url` 요청을 보냄
*   **Then**: 시스템은 타임아웃 없이 안전하게 텍스트를 AI에 전달하거나, 적절한 에러 핸들링을 통해 `FAILED` 로그를 남겨야 함

#### 시나리오 3: AI가 불완전한 정보를 추출할 경우
*   **Given**: AI가 회사명은 찾았으나 다른 필드(위치, 자격요건 등)를 찾지 못함
*   **When**: `POST /api/v1/url` 요청을 보냄
*   **Then**: 시스템은 기본값(예: "Unknown", "N/A")을 사용하여 `PENDING` 상태로 저장하고 201 Created를 반환함

#### 시나리오 4: 특수 문자가 포함된 검색/조회 (필요 시)
*   **Given**: 회사명에 특수 문자(&, #, %)가 포함된 공고를 조회하거나 수정함
*   **When**: 관련 API 요청을 보냄
*   **Then**: 인코딩 문제 없이 정상적으로 데이터가 입출력되어야 함
