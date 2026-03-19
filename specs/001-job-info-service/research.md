# Research: HireSync 채용 정보 서비스

## Technical Decisions

### Backend Framework: Spring Boot 3.5.x
- **Decision**: Spring Boot 3.5.x 사용
- **Rationale**: 사용자의 명시적인 요구사항이며, 최신 Spring 생태계의 기능을 활용하여 견고한 백엔드 구축 가능.
- **Alternatives**: Node.js, FastAPI 등이 있으나 Java/Spring 환경의 안정성과 확장성 선호.

### Database: MySQL
- **Decision**: MySQL 사용
- **Rationale**: 관계형 데이터베이스의 표준이며, 채용 정보와 같은 정형화된 데이터 관리 및 트랜잭션 처리에 적합.
- **Alternatives**: PostgreSQL (프로젝트 헌장에는 언급되었으나 사용자가 MySQL을 명시함).

### Frontend Library: React (JavaScript)
- **Decision**: React + JavaScript 사용
- **Rationale**: 컴포넌트 기반 개발로 생산성이 높으며, 사용자의 명시적인 요구(TypeScript 제외)를 따름.
- **Alternatives**: Vue.js, Angular, TypeScript (사용자 제외 요청).

### PDF Extraction: Apache PDFBox
- **Decision**: Apache PDFBox 사용
- **Rationale**: Java 환경에서 PDF 텍스트 추출을 위한 안정적이고 널리 사용되는 라이브러리.
- **Alternatives**: iText (라이선스 고려 필요), Apache Tika (범용적이지만 PDF 전용으로는 PDFBox가 더 세밀함).

### Web Scraping: Jsoup
- **Decision**: Jsoup 사용
- **Rationale**: URL로부터 HTML을 파싱하고 필요한 정보를 추출하는 데 매우 직관적이고 가벼운 Java 라이브러리.
- **Alternatives**: Selenium (동적 렌더링이 필요한 경우 사용하나, 우선 Jsoup으로 시작).

### Email Integration: Spring Boot Starter Mail
- **Decision**: Spring Boot Starter Mail 사용
- **Rationale**: Spring 생태계 내에서 이메일 수신 및 처리를 위한 표준적인 접근 방식.
- **Alternatives**: 직접 JavaMail 사용.

## Integration Patterns
- **Communication**: RESTful API
- **Format**: JSON
- **Authentication**: JWT (JSON Web Token)를 통한 관리자 인증.
- **File Storage**: 업로드된 PDF는 파일 시스템 또는 AWS S3에 저장 (구현 단계에서 결정).
