# Quickstart: HireSync 채용 정보 서비스

본 가이드는 프로젝트 개발 환경 구축 및 실행을 위한 단계별 안내입니다.

## Prerequisites
- **Backend**: JDK 21, Gradle 8+
- **Frontend**: Node.js 18+, npm 9+ (React 19)
- **Database**: PostgreSQL 15+ (또는 테스트용 H2 내장 DB)
- **AI**: OpenAI API Key (Spring AI 연동용)

## Database Setup
1. PostgreSQL 서버를 실행합니다.
2. `hiresync` 데이터베이스를 생성합니다.
   ```sql
   CREATE DATABASE hiresync;
   ```
3. `backend/src/main/resources/schema.sql`에 정의된 스키마를 참고하여 테이블을 생성하거나 Spring Boot의 Hibernate ddl-auto 설정을 활용합니다.

## Backend Startup
1. `backend` 디렉토리로 이동합니다.
2. `src/main/resources/application.properties` 파일에 DB 연결 정보 및 `OPENAI_API_KEY`를 설정합니다.
3. 애플리케이션을 빌드하고 실행합니다.
   ```bash
   ./gradlew bootRun
   ```

## Frontend Startup
1. `frontend` 디렉토리로 이동합니다.
2. 의존성 라이브러리를 설치합니다.
   ```bash
   npm install
   ```
3. 애플리케이션을 실행합니다.
   ```bash
   npm start
   ```

## Validation Checklist
- [ ] 구직자용 채용 목록 조회 API(`/api/job-postings`)가 정상 작동하는가?
- [ ] 관리자 권한(`ADMIN` role)으로 `/api/admin/**` 엔드포인트 접근 시 권한 검증이 이루어지는가?
- [ ] PDF 업로드 시 OpenAI를 통해 정보가 추출되고 `sources` 테이블에 기록되는가?
- [ ] 프론트엔드 관리자 페이지에서 채용 공고 승인 및 삭제가 가능한가?
