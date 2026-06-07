# Data Model: 취업 정보 포털 (Job Information Portal)

데이터베이스 스키마 구조와 주요 엔티티의 필드 구성을 정의합니다.

## 엔티티 정의 (Entities)

### 1. JobPosting (채용 공고)
채용 정보의 핵심 데이터를 저장합니다.

| 필드명 | 타입 | 설명 | 제약 조건 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 고유 식별자 | PK, Auto Increment |
| company_name | VARCHAR(100) | 회사명 | NOT NULL |
| location | VARCHAR(255) | 회사 위치 | NOT NULL |
| position | VARCHAR(100) | 채용 분야 | NOT NULL |
| requirements | TEXT | 채용 조건 | NOT NULL |
| openingDate | DATE | 모집 시작일 | NOT NULL |
| closingDate | DATE | 모집 마감일 | NOT NULL |
| application_method | VARCHAR(255) | 지원 방법 | NOT NULL |
| expected_salary | VARCHAR(100) | 예상 급여 | NOT NULL |
| status | VARCHAR(20) | 게시 상태 (PENDING, APPROVED, EXPIRED) | NOT NULL |
| source_id | BIGINT | 정보 소스 | FK (Source.id) |
| created_at | DATETIME | 생성 일시 | NOT NULL |
| updated_at | DATETIME | 수정 일시 | NOT NULL |

### 2. User (사용자)
시스템 관리 권한을 가진 사용자와 일반 사용자 정보를 저장합니다.

| 필드명 | 타입 | 설명 | 제약 조건 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 고유 식별자 | PK, Auto Increment |
| email | VARCHAR(50) | 이메일 겸 아이디 (암호화 대상) | UNIQUE, NOT NULL |
| password | VARCHAR(255) | 비밀번호 (해시) | NOT NULL |
| name | VARCHAR(100) | 이름 | NOT NULL |
| role | VARCHAR(100) | 역할 (ADMIN, GENERAL) | NOT NULL |
| status | VARCHAR(20) | 사용자 상태 (ACTIVE, INACTIVE, REVOKED) | NOT NULL |
| created_at | DATETIME | 생성 일시 | NOT NULL |
| updated_at | DATETIME | 수정 일시 | NOT NULL |

### 3. Source (정보 소스)
외부 소스로부터 정보를 수집한 이력을 관리합니다.

| 필드명 | 타입 | 설명 | 제약 조건 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 고유 식별자 | PK, Auto Increment |
| type | VARCHAR(20) | 소스 타입 (PDF, URL, EMAIL) | NOT NULL |
| reference_uri | VARCHAR(500) | 파일 경로 또는 URL 주소 | NOT NULL |
| status | VARCHAR(20) | 상태 (SUCCESS, FAILED, PROCESSING) | NOT NULL |
| created_at | DATETIME | 생성 일시 | NOT NULL |
| updated_at | DATETIME | 수정 일시 | NOT NULL |

### 4. ExtractionLog (추출 로그) [Ref: SC-014]
AI 추출 엔진의 처리 상세 기록을 관리합니다.

| 필드명 | 타입 | 설명 | 제약 조건 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 고유 식별자 | PK, Auto Increment |
| source_id | BIGINT | 정보 소스 ID | FK (Source.id) |
| model_version | VARCHAR(50) | 사용된 AI 모델 버전 | NOT NULL |
| raw_data | TEXT | 추출된 원본 데이터 (JSON) | NOT NULL |
| processing_time_ms | INTEGER | 처리 소용 시간 (ms) | NOT NULL |
| error_message | TEXT | 실패 시 에러 메시지 | |
| processed_at | DATETIME | 처리 일시 | NOT NULL |

### 5. ApiUsageLog (API 사용 로그)
API 호출이 발생할 때마다 요청자의 상세 정보를 데이터베이스에 로깅합니다.

| 필드명 | 타입 | 설명 | 제약 조건 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 고유 식별자 | PK, Auto Increment |
| ip_address | VARCHAR(45) | 요청자 IP 주소 | NOT NULL |
| http_method | VARCHAR(10) | HTTP 메서드 | NOT NULL |
| request_uri | VARCHAR(500) | 요청 API URI | NOT NULL |
| username | VARCHAR(100) | 로그인 사용자 식별 정보 (이메일 등, 비로그인 시 null) | |
| created_at | DATETIME | 요청 발생 시간 | NOT NULL |

## 상태 전이 (State Transitions)

### JobPosting

1. **PENDING**: 소스(PDF, URL, 이메일)로부터 정보가 추출되었으나 필수 항목이 누락되어 관리자 검토를 대기하는 상태.
2. **APPROVED**: 정보가 완전하며 관리자가 승인하여 일반 사용자에게 노출되는 상태. (추출 성공 시 즉시 공개 원칙에 따라 대개 자동 전환됨)
3. **EXPIRED**: 마감일(closingDate)이 지나 시스템에 의해 자동으로 숨겨진 상태.

### User

1. **ACTIVE**: 정상 상태
2. **INACTIVE**: 정지 상태
3. **REVOKED**: 자격 탈퇴 상태

### Source

1. **PROCESSING**: 정보 추출이 진행 중인 상태
2. **SUCCESS**: 정상적으로 정보가 추출되어 JobPosting이 생성된 상태
3. **FAILED**: 정보 추출이 최종적으로 실패한 상태