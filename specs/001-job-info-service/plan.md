# Implementation Plan: HireSync 채용 정보 서비스

**Branch**: `001-job-info-service` | **Date**: 2026-03-19 | **Spec**: [specs/001-job-info-service/spec.md](specs/001-job-info-service/spec.md)

## Summary
HireSync는 구직자에게 채용 정보를 제공하고, 관리자가 PDF, URL, 이메일을 통해 정보를 자동 추출하여 등록할 수 있는 웹 서비스입니다. Spring Boot 3.5.x 기반의 백엔드와 React(JS) 기반의 프론트엔드로 구성됩니다.

## Technical Context
- **Backend Framework**: Spring Boot 3.5.x
- **Frontend Library**: React (JavaScript)
- **Database**: MySQL 8.0
- **Communication**: REST API
- **Key Libraries**: Apache PDFBox (PDF 추출), Jsoup (웹 스크래핑), Spring Boot Starter Mail (이메일 처리)

## Constitution Check
- [x] **테스트 중심 개발 (TDD)**: 모든 API 및 로직 구현 전 테스트 코드를 필수 작성함.
- [x] **보안 및 개인정보 보호**: 관리자 인증(JWT) 및 HTTPS 통신을 적용함.
- [x] **한국어 중심 산출물**: 모든 설계 및 주석을 한국어로 작성함.

## Project Structure
```text
backend/
├── src/main/java/com/hiresync/
│   ├── models/        # DB 엔티티
│   ├── repositories/  # DB 접근 인터페이스
│   ├── services/      # 비즈니스 로직 (추출 엔진 포함)
│   ├── controllers/   # REST API 컨트롤러
│   └── security/      # JWT 및 인증 설정
└── src/test/java/com/hiresync/ # TDD 테스트 코드

frontend/
├── src/
│   ├── components/    # 공용 컴포넌트
│   ├── pages/         # 구직자/관리자 페이지
│   ├── services/      # API 통신 로직
│   └── utils/         # 유틸리티 함수
└── tests/             # 프론트엔드 테스트
```

## Generated Artifacts
- **Research**: [research.md](research.md) (기술 결정 근거)
- **Data Model**: [data-model.md](data-model.md) (MySQL 스키마)
- **API Contracts**: [contracts/api-spec.md](contracts/api-spec.md) (REST API 명세)
- **Quickstart**: [quickstart.md](quickstart.md) (환경 구축 가이드)
