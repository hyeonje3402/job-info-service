# API Contracts: 취업 정보 포털 (Job Information Portal)

백엔드와 프론트엔드 간의 통신 규격을 정의합니다. 모든 API는 JSON 형식을 지원합니다.

## 공통 데이터 구조

### 1. JobPosting (채용 공고)
| 필드명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | `Long` | 고유 식별자 |
| `companyName` | `String` | 회사명 |
| `location` | `String` | 근무 지역 |
| `position` | `String` | 채용 직무 |
| `requirements` | `String` | 자격 요건 (TEXT) |
| `openingDate` | `String (ISO-8601)` | 공고 시작일 (YYYY-MM-DD) |
| `closingDate` | `String (ISO-8601)` | 공고 마감일 (YYYY-MM-DD) |
| `applicationMethod` | `String` | 지원 방법 |
| `expectedSalary` | `String` | 예상 연봉 |
| `status` | `String` | 공고 상태 (`PENDING`, `APPROVED`, `EXPIRED`) |
| `source` | `Source` | 정보 출처 객체 |
| `createdAt` | `String (ISO-8601)` | 생성 일시 (YYYY-MM-DDTHH:mm:ss) |
| `updatedAt` | `String (ISO-8601)` | 수정 일시 (YYYY-MM-DDTHH:mm:ss) |

### 2. Source (정보 출처)
| 필드명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | `Long` | 고유 식별자 |
| `type` | `String` | 출처 유형 (PDF, URL 등) |
| `status` | `String` | 파싱 상태 (`SUCCESS`, `FAILED`) |
| `createdAt` | `String (ISO-8601)` | 생성 일시 |
| `updatedAt` | `String (ISO-8601)` | 수정 일시 |

### 3. Pagination Response (페이징 응답 - Spring Data Page)
| 필드명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `content` | `Array` | 실제 데이터 목록 (예: `JobPosting[]`) |
| `totalPages` | `Integer` | 전체 페이지 수 |
| `totalElements` | `Long` | 전체 데이터 수 |
| `size` | `Integer` | 페이지당 데이터 수 |
| `number` | `Integer` | 현재 페이지 번호 (0부터 시작) |
| `last` | `Boolean` | 마지막 페이지 여부 |
| `first` | `Boolean` | 첫 번째 페이지 여부 |
| `numberOfElements` | `Integer` | 현재 페이지의 데이터 수 |

### 4. User (사용자)
| 필드명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | `Long` | 고유 식별자 |
| `email` | `String` | 이메일 주소. 로그인 아이디로 사용 |
| `password` | `String` | 비밀번호 (그대로 노출되면 안 됨) |
| `name` | `String` | 실제 이름 |
| `role` | `String` | 역할 (`ADMIN`, `GENERAL`) |
| `status` | `String` | 상태 (`ACTIVE`, `INACTIVE`, `REVOKED`) |
| `createdAt` | `String (ISO-8601)` | 생성 일시 |
| `updatedAt` | `String (ISO-8601)` | 수정 일시 |

## 공통 응답 구조
```json
{
  "status": "success",
  "data": { ... } // 단일 객체 또는 페이징 응답 객체
}
```

## 일반 사용자용 API

### 채용 정보 목록 조회
- **Method**: `GET`
- **Path**: `/api/v1/jobs`
- **Query Params**:
  - `page` (Integer, default: 0): 페이지 번호
  - `size` (Integer, default: 10): 페이지당 개수
  - `sort` (String, optional): 정렬 기준 (예: `createdAt,desc`)
- **Response Body**:
  - `status`: "success"
  - `data`: `Pagination Response` 객체

### 채용 정보 상세 조회
- **Method**: `GET`
- **Path**: `/api/v1/jobs/{id}`
- **Response Body**:
  - `status`: "success"
  - `data`: `JobPosting` 객체

### 사용자 등록
- **Method**: `POST`
- **Path**: `/api/v1/auth/register`
- **Request Body**:
  - `email`: `String`
  - `password`: `String`
  - `name`: `String`
  - `role`: `String`
- **Response Body**:
  - `status`: "success"
  - `data`: `User` 객체

### 사용자 로그인
- **Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Request Body**:
  - `email`: `String`
  - `password`: `String`
- **Response Body**:
  - `status`: "success"
  - `data`: JWT 값

### 사용자 로그아웃
- **Method**: `POST`
- **Path**: `/api/v1/auth/logout`
- **Response Body**:
  - `status`: "success"
  - `data`: null

### 사이트 상태 확인
- **Method**: `POST`
- **Path**: `/api/v1/healthcheck`
- **Response Body**:
  - `status`: "success"
  - `data`: null

## 관리자용 API

### PDF 업로드 및 정보 추출
- **Method**: `POST`
- **Path**: `/api/v1/sources/pdf`
- **Content-Type**: `multipart/form-data`
- **Request Body**:
  - `file`: `MultipartFile` (PDF 파일)
- **Response Body**:
  - `status`: "success"
  - `data`: 생성된 `JobPosting` 객체 (`status`는 `PENDING`)

### URL 등록 및 정보 추출
- **Method**: `POST`
- **Path**: `/api/v1/sources/url`
- **Request Body**:
  - `url`: `String` (채용 페이지 URL)
- **Response Body**:
  - `status`: "success"
  - `data`: 생성된 `JobPosting` 객체 (`status`는 `PENDING`)

### 채용 정보 수정 (승인도 여기서 처리)
- **Method**: `PUT`
- **Path**: `/api/v1/jobs/{id}`
- **Request Body**: `JobPosting` (수정할 필드 포함)
- **Response Body**:
  - `status`: "success"
  - `data`: 수정된 `JobPosting` 객체

### 채용 정보 삭제
- **Method**: `DELETE`
- **Path**: `/api/v1/jobs/{id}`
- **Response Body**:
  - `status`: "success"
  - `data`: `null`
