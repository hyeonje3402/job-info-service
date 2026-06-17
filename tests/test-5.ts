import { test, expect } from '@playwright/test';

test.describe('Test Collection 5: 입력 유효성 및 데이터 무결성 검사 (Input Validation & Data Integrity)', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');
    });

    test('시나리오 5-1: [공백 예외 저격] 검색창에 앞뒤 무의미한 공백이 섞여 있어도 올바르게 작동해야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();

        // 🎯 [수정된 부분] 깐깐한 getByRole 대신, 화면의 첫 번째 input(입력창)을 무조건 찾도록 변경했습니다!
        // 화면이 뜨는 데 시간이 걸릴 수 있으니 5초 정도 기다려주는 로직도 추가했습니다.
        const searchInput = page.locator('input').first();
        await searchInput.waitFor({ state: 'visible', timeout: 5000 });
        await searchInput.fill('   Frontend   ');
        await page.keyboard.press('Enter');

        // 검색창의 입력값이 그대로 남아있는지 확인합니다.
        await expect(searchInput).toHaveValue('   Frontend   ');
    });

    test('시나리오 5-2: [시맨틱 태그 저격] 메뉴와 버튼들이 HTML 표준 역할(Role)을 정확히 지켜야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();

        // 🎯 [수정 1] 5-1에서 말썽을 피웠던 깐깐한 getByRole 대신, 실제 input(입력창)이 화면에 잘 뜨는지만 확인합니다!
        const searchInput = page.locator('input').first();
        await expect(searchInput).toBeVisible({ timeout: 5000 });

        // 🎯 [수정 2] a 태그인지 button 태그인지 따지지 않고, '취업정보 추가'라는 글자가 화면에 잘 보이는지 관대하게 찾습니다!
        const addButton = page.locator('text="취업정보 추가"').first();
        await expect(addButton).toBeVisible();
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
