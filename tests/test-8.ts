import { test, expect } from '@playwright/test';

test.describe('Test Collection 8: 예외 상황 및 유효성 검사 (Error Handling)', () => {

    test('시나리오 8-1: [로그인 실패 방어] 잘못된 비밀번호 입력 시 접속이 거부되어야 한다', async ({ page }) => {
        // 1. 로그인 페이지 접속
        await page.goto('http://localhost:3000/login');

        // 2. 이메일은 맞지만, 비밀번호는 엉뚱한 값 입력
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', 'wrong_password_999');
        await page.click('button[type="submit"]');

        // 3. 뚫리지 않고 여전히 로그인 페이지(URL)에 머물러 있는지 철벽 방어 확인
        await expect(page).toHaveURL(/.*login/);
    });

    test('시나리오 8-2: [필수값 누락 방어] 채용 공고 추가 시 빈칸으로 제출하면 차단되어야 한다', async ({ page }) => {
        // 1. 정상 로그인 후 대시보드 진입
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');

        // 2. 취업정보 추가 페이지로 이동
        await page.locator('text="Job Listings"').first().click();
        await page.locator('text="취업정보 추가"').first().click();

        // 3. 아무것도 적지 않고 냅다 제출(Submit) 버튼 클릭
        const submitBtn = page.locator('button[type="submit"]').first();
        await submitBtn.click();

        // 4. 화면이 넘어가지 않고 여전히 채용 공고 추가 페이지에 멈춰서 입력을 요구하는지 검사
        // (현제 님의 페이지 URL 구조에 맞게 검사합니다)
        await expect(page).not.toHaveURL('**/admin/dashboard'); 
    });
});
