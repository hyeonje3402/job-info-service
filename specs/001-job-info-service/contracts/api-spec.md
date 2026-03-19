

# API Contract: HireSync 채용 정보 서비스

## Base URL: `/api/v1`

### 1. Public API (구직자용)

#### GET `/job-postings`
채용 공고 목록을 조회합니다.
- **Query Parameters**:
  - `page`: 페이지 번호 (default: 0)
  - `size`: 페이지 크기 (default: 10)
  - `category`: 채용 분야 필터 (optional)
- **Response (200 OK)**:
  ```json
  {
    "content": [
      {
        "id": 1,
        "companyName": "Sample Company",
        "jobCategory": "Backend Developer",
        "createdAt": "2026-03-19T10:00:00Z"
      }
    ],
    "totalPages": 1,
    "totalElements": 1
  }
  ```

#### GET `/job-postings/{id}`
특정 채용 공고의 상세 정보를 조회합니다.
- **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "companyName": "Sample Company",
    "companyLocation": "Seoul, Korea",
    "jobCategory": "Backend Developer",
    "jobRequirements": "Experience with Spring Boot...",
    "applicationPeriod": "2026-03-19 to 2026-04-19",
    "applicationMethod": "Email apply",
    "expectedSalary": "competitive",
    "sourceType": "PDF"
  }
  ```

### 2. Admin API (관리자 전용)
*모든 요청은 Authorization 헤더에 JWT Bearer 토큰이 필요함.*

#### POST `/admin/login`
관리자 로그인을 수행하고 토큰을 발급받습니다.
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "password123"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```

#### POST `/admin/job-postings/upload-pdf`
PDF 파일을 업로드하여 정보를 추출하고 등록합니다.
- **Request Body**: `multipart/form-data` (file: `pdfFile`)
- **Response (201 Created)**:
  ```json
  {
    "id": 2,
    "status": "SUCCESS",
    "extractedData": { ... }
  }
  ```

#### POST `/admin/job-postings/register-url`
URL을 등록하여 정보를 추출하고 저장합니다.
- **Request Body**:
  ```json
  {
    "url": "https://company.com/jobs/1"
  }
  ```
- **Response (201 Created)**:
  ```json
  {
    "id": 3,
    "status": "SUCCESS"
  }
  ```

#### PUT `/admin/job-postings/{id}`
기존 채용 공고 정보를 수정합니다.
- **Request Body**: (Full JobPosting object)
- **Response (200 OK)**:
  ```json
  { "message": "Updated successfully" }
  ```

#### DELETE `/admin/job-postings/{id}`
특정 채용 공고를 삭제합니다.
- **Response (204 No Content)**: Empty body
