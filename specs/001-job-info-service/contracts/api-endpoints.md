# 사용자 관리 API 엔드포인트

## 개요
이 문서는 등록된 사용자 목록을 조회하고 개별 사용자의 상세 정보를 확인하는 API 엔드포인트를 정의합니다. 이 API들은 관리자(ADMIN) 권한이 필요합니다.

## 엔드포인트 목록

### 1. 전체 사용자 목록 조회
- **Method**: `GET`
- **Path**: `/api/v1/users`
- **Description**: 시스템에 등록된 모든 사용자의 목록을 조회합니다.
- **Security**: 관리자 권한 필수 (`ROLE_ADMIN`)
- **Response Body**:
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "GENERAL",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "email": "admin@example.com",
      "name": "관리자",
      "role": "ADMIN",
      "status": "ACTIVE"
    }
  ]
}
```

### 2. 특정 사용자 상세 조회
- **Method**: `GET`
- **Path**: `/api/v1/users/{id}`
- **Description**: 특정 사용자 ID를 기반으로 상세 정보를 조회합니다.
- **Security**: 관리자 권한 필수 (`ROLE_ADMIN`)
- **Response Body**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "GENERAL",
    "status": "ACTIVE"
  }
}
```

## 공통 사항
- 응답의 `status` 필드는 성공 시 `"success"`, 실패 시 `"error"`를 반환합니다.
- 개인정보 보호를 위해 비밀번호(`password`) 필드는 응답에서 제외됩니다.
