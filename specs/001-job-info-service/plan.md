# Implementation Plan: HireSync 채용 정보 서비스

**Branch**: `001-job-info-service` | **Date**: 2026-03-29 | **Spec**: [/specs/001-job-info-service/spec.md](/specs/001-job-info-service/spec.md)

## Summary

HireSync 채용 정보 서비스는 구직자에게 통합된 채용 정보를 제공하고, 관리자에게는 PDF, URL, 이메일 등 다양한 경로를 통한 정보 등록 자동화 기능을 제공합니다. 본 계획은 Java 21, Gradle 8+, Spring Boot 3.5.x, React 19 및 OpenAI API(Spring AI)를 활용한 현대적이고 확장 가능한 아키텍처 구축을 목표로 합니다.

## Technical Context

**Language/Version**: Java 21, JavaScript ES6+  
**Primary Dependencies**: Spring Boot 3.5.x, Gradle 8+, React 19, Spring AI (OpenAI API), JSoup 1.17.x, SpringDoc OpenAPI 2.x  
**Storage**: PostgreSQL (개인정보 암호화 및 영속성 보장), H2 (테스트용)  
**Testing**: JUnit 5, Mockito, AssertJ (Backend) / Jest, React Testing Library (Frontend)  
**Target Platform**: Linux Server (Docker/AWS) 
**Project Type**: Web Application (Spring Boot REST API + React SPA)  
**Performance Goals**: URL 정보 추출 및 분석 10초 이내 (SC-002), PDF 추출 정확도 90% 이상 (SC-003)  
**Constraints**: <200ms API 응답 (P95), HTTPS 필수, OAuth 2.0/JWT 보안 적용  
**Scale/Scope**: 관리자 10명 이내, 구직자 동시 접속 100명 이상 대응

## Constitution Check

*GATE: All principles from /home/hjj/projects/github-hyeonje3402/.specify/memory/constitution.md must be followed.*

1. **테스트 중심 개발 (TDD)**: 모든 신규 기능은 Red-Green-Refactor 사이클을 준수하며, 테스트 커버리지를 우선 확보함. (PASS)
2. **보안 및 개인정보 보호 우선**: JWT 기반 인증 및 암호화 저장 적용. HTTPS 통신 강제. (PASS)
3. **한국어 중심 산출물**: 모든 주석, 문서, 커밋 메시지는 한국어로 작성. (PASS)
4. **AI 기반 매칭 및 UX**: OpenAI API를 활용하여 PDF 및 URL에서 정교한 정보 추출 및 데이터 구조화 수행. (PASS)
5. **지속적인 통합 및 품질 관리**: Gradle 기반 빌드 자동화 및 CI 파이프라인 연계. (PASS)

## Project Structure

### Documentation

- `specs/001-job-info-service/plan.md`: 본 문서 (구현 전략 및 설계)
- `specs/001-job-info-service/spec.md`: 요구사항 명세서

### Source Code Strategy (Migration to Gradle)

기존 Maven 프로젝트를 Gradle 멀티 프로젝트 구조로 전환하여 관리 효율성을 높입니다.

```text
backend/
├── build.gradle (Gradle 8+)
├── src/
│   ├── main/
│   │   ├── java/com/hiresync/
│   │   │   ├── controllers/      # REST API 엔드포인트
│   │   │   ├── services/         # 비즈니스 로직 (PDF, URL, Email 처리)
│   │   │   ├── models/           # JPA 엔티티 및 DTO
│   │   │   ├── security/         # JWT 및 Spring Security 설정
│   │   │   └── extraction/       # OpenAI 기반 추출 엔진
│   │   └── resources/
│   └── test/                     # TDD를 위한 테스트 코드
frontend/
├── package.json (React 19)
├── src/
│   ├── components/               # 공용 UI 컴포넌트
│   ├── pages/                    # 채용 목록, 상세, 관리자 페이지
│   ├── services/                 # API 통신 모듈 (Axios)
│   └── utils/
└── tests/                        # 프론트엔드 유닛 및 통합 테스트
```

## Strategy & Research Integration

### 1. 기술 스택 전환 (Migration)
- **Java 21**: Virtual Threads를 활용하여 동시성 처리 성능 향상 (특히 URL 스크래핑 시 이점).
- **Gradle 8+**: Maven(`pom.xml`)에서 Gradle로 전환하여 빌드 속도 개선 및 유연한 빌드 스크립트 작성.
- **Spring Boot 3.5.x**: 최신 기능을 활용하여 Spring AI와의 호환성 극대화.

### 2. 채용 정보 자동 추출 엔진 (OpenAI API)
- **PDF 분석**: 외부 라이브러리(PDFBox 등)를 통한 로컬 파싱을 배제하고, OpenAI의 Document 분석 기능(Spring AI 통합)을 사용하여 PDF 바이너리를 직접 전달하여 구조화된 JSON(회사명, 급여 등)으로 추출.
- **URL 분석**: JSoup으로 HTML을 파싱하고, 핵심 텍스트를 추출하여 AI가 컨텍스트를 파악하도록 설계.
- **구조화**: OpenAI의 JSON Mode 또는 Function Calling을 활용하여 `JobPosting` 엔티티 형식에 맞는 정확한 데이터 확보.

### 3. 이메일 처리 로직
- Spring Mail 수신 기능을 연동하여 "[연암취업정보]" 제목 필터링.
- 본문에 포함된 URL이나 텍스트를 추출 엔진으로 전달하여 자동 등록 프로세스 가동.

### 4. 프론트엔드 (React 19)
- React 19의 새로운 기능(Actions, useOptimistic 등)을 활용하여 관리자 페이지의 CRUD 반응성 향상.
- TypeScript를 통한 타입 안정성 확보.

## Data Model (Summary)

- **JobPosting**: id(Long), companyName(String), location(String), field(String), conditions(Text), deadline(String), method(Text), expectedSalary(String), source(Enum: PDF, URL, EMAIL), createdAt(DateTime), updatedAt(DateTime)
- **ExtractionLog**: id, sourceId, sourceType, status(SUCCESS/FAIL), rawText, extractedData, errorMessage, processedAt

## API Contracts (Summary)

- `GET /api/job-postings`: 채용 목록 조회
- `GET /api/job-postings/{id}`: 상세 정보 조회
- `POST /api/admin/job-postings/upload-pdf`: PDF 업로드 및 자동 추출
- `POST /api/admin/job-postings/register-url`: URL 등록 및 자동 추출
- `PUT/DELETE /api/admin/job-postings/{id}`: 관리자 수정/삭제
