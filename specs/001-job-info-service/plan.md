# Implementation Plan: HireSync 채용 정보 서비스

**Branch**: `001-job-info-service` | **Date**: 2026-03-29 | **Spec**: [/specs/001-job-info-service/spec.md](/specs/001-job-info-service/spec.md)

## Summary

HireSync 채용 정보 서비스는 구직자에게 통합된 채용 정보를 제공하고, 관리자에게는 PDF, URL, 이메일 등 다양한 경로를 통한 정보 등록 자동화 기능을 제공합니다. 본 계획은 Java 21, Gradle 8+, Spring Boot 3.5.x, React 19 및 OpenAI API(Spring AI)를 활용한 현대적이고 확장 가능한 아키텍처 구축을 목표로 합니다.

## Technical Context

**Language/Version**: Java 21, JavaScript ES6+  
**Primary Dependencies**: Spring Boot 3.5.x, Gradle 8+, React 19, Spring AI (OpenAI API), JSoup 1.17.x, SpringDoc OpenAPI 2.x  
**Storage**: PostgreSQL (pgcrypto를 활용한 개인정보 암호화), H2 (테스트용)  
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

### 1. 기술 스택 및 빌드 환경 전환 (Migration) [Ref: CHK009]
- **Gradle 8.x**: `settings.gradle`을 통해 백엔드와 프론트엔드 빌드 수명 주기를 통합 관리함. 프론트엔드 빌드 결과물(dist)을 백엔드 `resources/static`으로 자동 복사하는 Task를 구성함.
- **Java 21 Virtual Threads**: `spring.threads.virtual.enabled=true` 설정을 활성화함. 외부 URL 스크래핑 및 OpenAI API 호출과 같은 블로킹 I/O 작업 시 가상 스레드를 할당하여 처리량을 극대화함. 단, `synchronized` 블록 내에서의 블로킹 작업이 스레드 고정(Pinning) 현상을 유발하지 않도록 `ReentrantLock`으로 대체하거나 코드 구조를 최적화함. [Ref: CHK010]

### 2. 채용 정보 자동 추출 엔진 (OpenAI API)
- **PDF/이미지 분석**: Spring AI를 사용하여 OpenAI의 `gpt-4o` 모델(Vision 지원)에 파일을 직접 전달함. 텍스트 추출이 어려운 이미지는 시각적 분석을 병행함.
- **대용량 문서 분할 (Chunking)**: 토큰 제한을 초과하는 문서는 문장/단락 단위로 분할하여 핵심 섹션(모집요강, 자격요건 등)을 식별한 후 개별 분석함. 최종적으로 각 청크의 결과를 병합하여 하나의 `JobPosting` 객체로 구조화함. [Ref: CHK016, CHK017]
- **URL 분석**: JSoup으로 HTML 본문을 파싱하고 불필요한 태그(scripts, ads)를 제거한 순수 텍스트를 AI에 전달함.

### 3. 데이터 보안 및 정합성 [Ref: CHK015]
- **개인정보 암호화**: PostgreSQL의 `pgcrypto` 확장 기능을 사용하여 관리자 계정 정보 및 민감한 추출 로그를 DB 수준에서 암호화함. Spring Data JPA의 `AttributeConverter`를 활용하여 애플리케이션 계층에서의 투명한 암복호화를 구현함.
- **중복 검증 로직**: `JobPostingService`에서 저장 전 (회사명, 직무, 마감일) 해시값을 비교하여 중복 여부를 판단함.

### 4. 프론트엔드 UX 혁신 (React 19) [Ref: CHK011]
- **Actions API**: 관리자 폼 제출 시 `useActionState`를 사용하여 로딩 및 에러 상태를 선언적으로 관리함.
- **Optimistic UI**: `useOptimistic`을 적용하여 채용 공고 삭제나 상태 변경 시 서버 응답 전 화면에 즉시 반영하여 체감 속도를 높임.
- **Server Components**: 목록 조회와 같이 정적인 데이터는 서버 컴포넌트 활용을 검토하여 초기 로딩 성능 최적화.

### 5. AI 추출 품질 관리 [Ref: CHK012, CHK013]
- **Schema Validation**: AI 응답을 Jackson을 통해 `JobPostingDTO`로 역직렬화할 때 유효성 검사를 수행함.
- **성공 지표 측정**: 테스트 단계에서 사전 정의된 50개 이상의 채용 공고 샘플(Golden Set)을 사용하여 추출 정확도를 자동 측정하는 통합 테스트 코드를 작성함.
