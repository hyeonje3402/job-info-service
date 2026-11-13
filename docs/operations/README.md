# CI와 배포 현황

[메인 README](../../README.md)

## CI 워크플로

저장소의 GitHub Actions 설정은 용도가 둘입니다.

| 워크플로 | 트리거 | 동작 |
|---|---|---|
| .github/workflows/playwright.yml | main/master push 및 Pull Request | Node.js 설정, npm 의존성 설치, Playwright 브라우저 설치 후 브라우저 테스트 실행 및 결과 업로드 |
| .github/workflows/classroom.yml | push 및 repository_dispatch | GitHub Classroom Markdown 형식 검사 |

백엔드 Gradle 테스트는 개발자가 **./gradlew :backend:test**로 실행할 수 있지만, 현재 확인한 workflow에는 백엔드 테스트 job이 없습니다.

## CD·클라우드

저장소에는 Vercel, Render, AWS 등 특정 플랫폼의 배포 workflow나 서비스 명세가 없습니다. 따라서 현재 CI 설정만으로 운영 배포가 이뤄진다고 볼 수 없습니다.

## 운영 전 확인할 항목

- 영속 운영 DB와 스키마 변경 이력 관리 도구 선정
- 데이터베이스 비밀번호, 메일·LLM 키를 환경 변수/비밀 저장소로 이동
- DataInitializer 기본 계정이 프로덕션에서 생성되지 않게 분리하고 비밀번호 변경 강제
- 파일 업로드 크기·형식·저장 위치·악성 파일 방어와 보관 정책 확정
- 스크래핑 대상 사이트의 이용 조건, 속도 제한, 장애 대응 확인
- 인증·권한·CORS·세션 쿠키를 실제 HTTPS 도메인 기준으로 점검
- CI에서 백엔드 테스트와 프런트엔드 빌드를 함께 실행하도록 보강

## 현재 DB 메모

기본 구성은 H2 인메모리입니다. PostgreSQL 드라이버 의존성은 있지만 운영 DB URL이나 클라우드 DB가 연결된 배포 설정은 확인되지 않았습니다.