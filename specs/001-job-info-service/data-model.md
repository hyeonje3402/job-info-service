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
| source | BIGINT | 정보 소스 | FK (source.id) |
| created_at | DATETIME | 생성 일시 | NOT NULL |
| updated_at | DATETIME | 수정 일시 | NOT NULL |

### 2. User (사용자)
시스템 관리 권한을 가진 사용자와 일반 사용자 정보를 저장합니다.

| 필드명 | 타입 | 설명 | 제약 조건 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | 고유 식별자 | PK, Auto Increment |
| email | VARCHAR(50) | 이메일 겸 아이디 | UNIQUE, NOT NULL |
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
| type | VARCHAR(20) | 소스 값 (pdf 텍스트, url 링크 등) | NOT NULL |
| status | VARCHAR(20) | 상태 (SUCCESS, FAILED) | NOT NULL |
| created_at | DATETIME | 생성 일시 | NOT NULL |
| updated_at | DATETIME | 수정 일시 | NOT NULL |

## 상태 전이 (State Transitions)

### JobPosting

1. **PENDING**: 소스(PDF, URL, 이메일)로부터 정보가 추출되어 관리자 승인을 대기하는 상태.
2. **APPROVED**: 관리자가 검토 후 승인하여 일반 사용자에게 노출되는 상태.
3. **EXPIRED**: 마감일(deadline)이 지나 시스템에 의해 자동으로 숨겨진 상태.

### User

1. **ACTIVE**: 정상 상태
2. **INACTIVE**: 정지 상태
3. **REVOKED**: 자격 탈퇴 상태

### Source

1. **SUCCESS**: 정상적으로 정보가 추출된 상태
2. **FAILED**: 정보 추출이 실패한 상태