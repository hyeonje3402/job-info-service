# 개발 환경·실행·테스트

[메인 README](../../README.md)

## 준비물

- JDK 21
- Node.js 18 이상 및 npm
- AI 분석을 시험할 경우 OpenAI 호환 API 키

## 환경 변수와 비밀값

application.properties의 AI API 키 설정을 확인하고 실제 키를 환경 변수로 공급합니다. 비밀값을 Java 설정 파일, README, 테스트 산출물에 직접 기록하거나 Git에 커밋하지 마세요. 공개 저장소에 노출된 키는 제공자 대시보드에서 폐기·재발급해야 합니다.

테스트 계정은 DataInitializer에서 로컬 개발용으로 생성됩니다. README나 공유 환경에 고정 비밀번호를 배포하지 말고, 외부에 공개하기 전에 초기 계정 생성·암호 변경 동작을 점검하세요.

## 백엔드 실행

저장소 루트에서:

~~~bash
./gradlew :backend:bootRun
~~~

Windows에서는 **gradlew.bat :backend:bootRun**을 사용할 수 있습니다. 기본 백엔드 주소는 **http://localhost:8080**이며 Swagger UI는 **http://localhost:8080/swagger-ui/index.html**에서 확인합니다. H2 콘솔은 설정에서 활성화되어 있습니다.

## 프런트엔드 실행

~~~bash
cd frontend
npm install
~~~

프런트엔드 환경 파일에서 백엔드 주소를 **REACT_APP_BACKEND=http://localhost:8080**으로 지정한 뒤:

~~~bash
npm start
~~~

CRA 개발 서버는 보통 **http://localhost:3000**에서 열립니다. Axios 세션 요청이 백엔드 쿠키를 보내도록 withCredentials 설정과 로컬 CORS 구성이 맞는지 확인하세요.

## 테스트

~~~bash
./gradlew :backend:test
~~~

~~~bash
cd frontend
npm test
~~~

브라우저 E2E는 저장소 루트에서 Playwright 의존성을 설치한 뒤 **npx playwright test**를 실행합니다. Newman 컬렉션 테스트는 백엔드 실행 상태에서 해당 Postman 컬렉션을 지정해 실행합니다.

자세한 화면·API 계약과 제품 요구사항은 specs/, docs/prd.md, DESIGN.md를 참고하세요.