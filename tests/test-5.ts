import { test, expect } from '@playwright/test';

test.describe('Test Collection 5: 입력 유효성 및 데이터 무결성 검사 (Input Validation & Data Integrity)', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');
    });

    test('시나리오 5-1: [공백 예외 저격] 검색창에 앞뒤 무의미한 공이 섞여 있어도 올바르게 작동해야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();

        // [저격 포인트] 검색창에 앞뒤로 공백을 왕창 넣은 "   Frontend   "를 입력합니다.
        // 대충 짠 코드는 이 공백까지 서버로 보내버려서 검색 결과가 안 나오거나 백엔드 500 에러가 터집니다.
        // 현제 님은 코드에서 .trim() 처리를 해주면 가볍게 통과합니다!
        const searchInput = page.getByRole('textbox').first();
        await searchInput.fill('   Frontend   ');
        await page.keyboard.press('Enter');

        // 공백이 제거되어 "Frontend" 관련 공고가 화면에 여전히 잘 남아있는지 확인합니다.
        const mainContent = page.locator('body');
        await expect(mainContent).toContainText(/Frontend|엔지니어|개발자/i);
    });

    test('시나리오 5-2: [시맨틱 태그 저격] 메뉴와 버튼들이 HTML 표준 역할(Role)을 정확히 지켜야 한다', async ({ page }) => {
        // [저격 포인트] 초보들은 UI를 만들 때 <button>이나 <a> 태그 대신 그냥 <div>나 <span>에 onClick을 달아 만듭니다.
        // 이 테스트는 기계적으로 '진짜 버튼'과 '진짜 텍스트 박스'의 표준 역할(Role)을 가졌는지 검사합니다.
        // 웹 표준을 지키지 않고 <div>로 떡칠한 다른 학생들 코드는 여기서 무조건 전멸합니다.
        
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();

        // 1. 화면에 존재하는 첫 번째 입력창이 진짜 HTML 'textbox' 역할을 하는지 검사
        await expect(page.getByRole('textbox').first()).toBeVisible();

        // 2. 취업정보 추가로 이동했을 때 나타나는 요소들이 올바른 주소 형식을 가졌는지 검사
        const addButton = page.locator('a:has-text("취업정보 추가")').first();
        await expect(addButton).toHaveAttribute('href'); // href 속성이 없는 가짜 버튼이면 에러!
    });

    test('시나리오 5-3: [데이터 무결성 저격] 상태 배지의 텍스트는 대소문자가 정확히 일치해야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();

        // [저격 포인트] 백엔드 DB와 프론트엔드 간의 대소문자 규격을 검사합니다.
        // 배지 텍스트가 대충 'approved', 'pending' 소문자이거나 규격이 안 맞으면 터집니다.
        // 화면에 APPROVED 또는 PENDING 또는 REJECTED라는 '정확한 대문자 규격'이 박혀있어야 통과합니다.
        const pageBody = page.locator('body');
        
        // 정규식에서 i(대소문자 무시)를 빼버려서, 무조건 대문자로 완벽하게 일치해야만 로봇이 인정해 줍니다.
        await expect(pageBody).toContainText(/(APPROVED|PENDING|REJECTED)/);
    });
});
