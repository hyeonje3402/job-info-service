import { test, expect } from '@playwright/test';

test.describe('페이지 네비게이션 및 라우팅 테스트', () => {
    test.beforeEach(async ({ page }) => {
        test.setTimeout(120000);
        await page.setViewportSize({ width: 1280, height: 1000 });
    });

    test('성공 시나리오: 비로그인 상태에서 메인 화면 및 로그인/회원가입 간의 화면 전환이 정상적이어야 한다', async ({ page }) => {
        // 1. 로그인 페이지 직접 진입
        await page.goto('http://localhost:3000/login', { waitUntil: 'load', timeout: 60000 });
        await expect(page).toHaveURL('http://localhost:3000/login');

        // 2. 하단 '아이디 등록' 링크 대기 및 클릭
        await page.waitForSelector('.signup-link', { state: 'visible', timeout: 30000 });
        await page.click('.signup-link');
        
        // 3. 회원가입 페이지 리다이렉트 검증
        await page.waitForURL('**/register', { timeout: 60000 });
        await expect(page).toHaveURL('http://localhost:3000/register');

        // 4. 회원가입 화면에서 '로그인 화면으로 이동' 링크 또는 뒤로가기 동작 (취소 버튼 등) 클릭 시도
        // (RegisterPage.jsx를 보면 로그인 페이지 링크 혹은 취소 버튼이 제공될 것입니다)
        const loginLink = page.locator('.login-footer a').filter({ hasText: '로그인' });
        await loginLink.click();
        await page.waitForURL('**/login', { timeout: 60000 });

        // 5. 로그인 페이지에서 취소(Cancel) 버튼 클릭 시 메인 리스트로 이동 검증
        await page.click('button:has-text("취소")');
        await page.waitForURL('http://localhost:3000/', { timeout: 60000 });
        await expect(page).toHaveURL('http://localhost:3000/');
    });
});
