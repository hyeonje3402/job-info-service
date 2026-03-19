# Data Model: HireSync 채용 정보 서비스

## Database: MySQL

### 1. JobPostings (채용 공고 테이블)
각 채용 공고의 핵심 정보를 저장하는 테이블입니다.

| Column Name | Type | Constraints | Description |
|-------------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 고유 식별자 |
| company_name | VARCHAR(255) | NOT NULL | 회사명 |
| company_location | VARCHAR(255) | | 회사 위치 |
| job_category | VARCHAR(100) | NOT NULL | 채용 분야 |
| job_requirements | TEXT | | 채용 조건 |
| application_period | VARCHAR(100) | | 지원 기간 |
| application_method | VARCHAR(255) | | 지원 방법 |
| expected_salary | VARCHAR(100) | | 예상 급여 |
| source_type | ENUM('PDF', 'URL', 'EMAIL') | NOT NULL | 정보 출처 |
| source_origin | VARCHAR(500) | | 원본 파일명/URL/이메일 주소 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 수정일 |

### 2. Admins (관리자 테이블)
시스템 관리자 계정 정보를 저장하는 테이블입니다.

| Column Name | Type | Constraints | Description |
|-------------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 고유 식별자 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 관리자 아이디 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 관리자 이메일 |
| role | VARCHAR(20) | DEFAULT 'ADMIN' | 권한 레벨 |
| last_login | TIMESTAMP | | 마지막 로그인 일시 |

### 3. ExtractionLogs (추출 로그 테이블)
PDF, URL, 이메일로부터 정보를 추출한 내역을 기록하는 테이블입니다.

| Column Name | Type | Constraints | Description |
|-------------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 고유 식별자 |
| source_type | ENUM('PDF', 'URL', 'EMAIL') | NOT NULL | 추출 대상 종류 |
| source_origin | VARCHAR(500) | | 추출 원본 경로/URL |
| status | ENUM('SUCCESS', 'FAILED') | NOT NULL | 추출 성공 여부 |
| error_message | TEXT | | 실패 시 오류 내용 |
| extracted_data_summary | JSON | | 추출된 데이터 요약 (임시 저장용) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 로그 생성일 |

## Entity Relationships
- **Admins**와 **JobPostings**: 관리자가 채용 공고를 생성/수정/삭제함 (1:N 관계).
- **JobPostings**와 **ExtractionLogs**: 특정 추출 로그로부터 채용 공고가 생성될 수 있음 (1:1 또는 N:1 관계 가능).
