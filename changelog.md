# Changelog

## [1.0.2] - 2026-03-26

### Added
- Swagger UI (SpringDoc OpenAPI) 연동: API 명세 확인 및 테스트를 위한 `springdoc-openapi-starter-webmvc-ui` 의존성 추가.
- `WebSecurityConfig.java`: Swagger UI 관련 경로(`/swagger-ui/**`, `/v3/api-docs/**` 등)에 대한 보안 예외 설정 추가.
- `spec.md`, `plan.md`, `tasks.md`: Swagger UI 도입 요구사항 및 관련 작업 태스크 추가.

### Changed
- `backend/pom.xml`: `springdoc-openapi-starter-webmvc-ui:2.8.5` 의존성 추가.

## [1.0.1] - 2026-03-26

### Added
- `changelog.md` 파일 생성: 프로젝트의 모든 변경 사항을 추적하기 위해 도입함.
- `.specify/memory/constitution.md` 업데이트: 향후 모든 변경 사항을 `changelog.md`에 기록하도록 하는 원칙 추가.

### Changed
- `.specify/memory/constitution.md`: 개발 워크플로우 및 거버넌스 섹션에 변경 이력 관리 원칙 추가.
