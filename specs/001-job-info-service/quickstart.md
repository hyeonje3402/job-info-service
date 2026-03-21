# Quickstart: HireSync 채용 정보 서비스

본 가이드는 프로젝트 개발 환경 구축 및 실행을 위한 단계별 안내입니다.

## Prerequisites
- **Backend**: JDK 17+, Maven 3.6+
- **Frontend**: Node.js 18+, npm 9+
- **Database**: MySQL 8.0+

## Database Setup
1. MySQL 서버를 실행합니다.
2. `hiresync` 데이터베이스를 생성합니다.
   ```sql
   CREATE DATABASE hiresync DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. `data-model.md`에 정의된 스키마를 참고하여 테이블을 생성하거나 Spring Boot의 Hibernate auto-ddl 기능을 활용합니다.

## Backend Startup
1. `backend` 디렉토리로 이동합니다.
2. `src/main/resources/application.properties` 파일에 DB 연결 정보를 설정합니다.
3. 애플리케이션을 빌드하고 실행합니다.
   ```bash
   mvn spring-boot:run
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
- [ ] 구직자용 채용 목록 조회 API(`/api/v1/job-postings`)가 정상 작동하는가?
- [ ] 관리자 로그인 후 JWT 토큰이 발급되는가?
- [ ] PDF 업로드 시 `ExtractionLog`가 정상적으로 기록되는가?
- [ ] 프론트엔드에서 백엔드 API를 성공적으로 호출하는가?
