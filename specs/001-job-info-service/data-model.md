# Data Model: HireSync 채용 정보 서비스

## Database: PostgreSQL

### 1. job_postings (채용 공고 테이블)
각 채용 공고의 핵심 정보를 저장하는 테이블입니다.

| Column Name | Type | Constraints | Description |
|-------------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | 고유 식별자 |
| company_name | VARCHAR(255) | NOT NULL | 회사명 |
| location | VARCHAR(255) | | 회사 위치 |
| field | VARCHAR(100) | NOT NULL | 채용 분야 |
| conditions | TEXT | | 채용 조건 |
| deadline | VARCHAR(100) | | 지원 기간 (마감일) |
| method | TEXT | | 지원 방법 |
| expected_salary | VARCHAR(100) | | 예상 급여 |
| source | VARCHAR(20) | NOT NULL | 정보 출처 (PDF, URL, EMAIL) |
| source_origin | VARCHAR(500) | | 원본 파일명/URL/이메일 주소 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 수정일 |

### 2. admins (관리자 테이블)
시스템 관리자 계정 정보를 저장하는 테이블입니다.

| Column Name | Type | Constraints | Description |
|-------------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | 고유 식별자 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 관리자 아이디 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 관리자 이메일 |
| role | VARCHAR(20) | DEFAULT 'ADMIN' | 권한 레벨 |
| last_login | TIMESTAMP | | 마지막 로그인 일시 |

### 3. extraction_logs (추출 로그 테이블)
PDF, URL, 이메일로부터 정보를 추출한 내역을 기록하는 테이블입니다.

| Column Name | Type | Constraints | Description |
|-------------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | 고유 식별자 |
| source_type | VARCHAR(20) | NOT NULL | 추출 대상 종류 |
| source_origin | VARCHAR(500) | | 추출 원본 경로/URL |
| status | VARCHAR(20) | NOT NULL | 추출 성공 여부 (SUCCESS, FAIL) |
| error_message | TEXT | | 실패 시 오류 내용 |
| extracted_data | JSONB | | 추출된 데이터 요약 |
| processed_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 처리 일시 |

## Entity Relationships
- **Admins**와 **JobPostings**: 관리자가 채용 공고를 생성/수정/삭제함 (1:N 관계).
- **JobPostings**와 **ExtractionLogs**: 특정 추출 로그로부터 채용 공고가 생성될 수 있음 (1:1 또는 N:1 관계 가능).
