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

### 3. API 사용 로그 조회
- **Method**: `GET`
- **Path**: `/api/v1/api-logs`
- **Description**: 시스템 내 API 호출 이력(로그)을 페이징 처리하여 조회합니다.
- **Security**: 관리자 권한 필수 (`ROLE_ADMIN`)
- **Query Parameters**:
  - `page` (Integer): 조회할 페이지 번호 (기본값: 0)
  - `size` (Integer): 한 페이지당 노출할 로그 개수 (기본값: 10)
  - `sort` (String): 정렬 기준 필드 및 방향 (기본값: "createdAt,desc")
- **Response Body**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "ipAddress": "127.0.0.1",
        "httpMethod": "GET",
        "requestUri": "/api/v1/users",
        "username": "admin@example.com",
        "createdAt": "2026-06-04T15:55:31"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "numberOfElements": 1,
    "first": true,
    "last": true,
    "empty": false
  }
}
```

## 공통 사항
- 응답의 `status` 필드는 성공 시 `"success"`, 실패 시 `"error"`를 반환합니다.
- 개인정보 보호를 위해 비밀번호(`password`) 필드는 응답에서 제외됩니다.
