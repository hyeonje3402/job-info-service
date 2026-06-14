import { test, expect } from '@playwright/test';
test.describe('인증 및 로그인 시나리오 테스트', () => {
    test.beforeEach(async ({ page }) => {
        // 실제 로그인 페이지 주소로 이동합니다.
        await page.goto('http://localhost:3000/login');
    });
    test('실패 시나리오: 잘못된 자격 증명을 입력했을 때 로그인 실패', async ({ page }) => {
        // 이메일과 비밀번호 필드 입력
        await page.fill('#email', 'test@example.com');
        await page.fill('#password', '3456');
        // 로그인 버튼 클릭
        await page.click('button[type="submit"]');
        // 에러 메시지 노출 확인 (LoginPage.js 내 errorMsg가 렌더링되는 div 탐색)
        const errorMsg = page.locator('div[style*="color: var(--color-error)"], div[style*="color:var(--color-error)"]');
        await expect(errorMsg).toBeVisible();
        await expect(errorMsg).toHaveText(/Invalid credentials|로그인에 실패했습니다/);
        // URL이 로그인 페이지 그대로 유지되는지 검증
        await expect(page).toHaveURL('http://localhost:3000/login');
    });
    test('성공 시나리오: 올바른 자격 증명을 입력했을 때 로그인 성공 및 대시보드 리다이렉트', async ({ page }) => {
        // 이메일과 비밀번호 필드 입력
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        // 로그인 버튼 클릭
        await page.click('button[type="submit"]');
        // 대시보드(/admin/dashboard) 페이지로의 리다이렉트 및 특정 헤더 요소 렌더링 검증
        await page.waitForURL('**/admin/dashboard');
        await expect(page).toHaveURL('http://localhost:3000/admin/dashboard');
        const dashboardHeader = page.locator('h2:has-text("관리자 대시보드")');
        await expect(dashboardHeader).toBeVisible();
    });
});