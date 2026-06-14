import { test, expect } from '@playwright/test';

test.describe('인증 및 로그인 시나리오 테스트', () => {
    test.beforeEach(async ({ page }) => {
        // 실제 로그인 페이지 주소로 이동합니다.
        await page.goto('http://localhost:3000/login');
    });

    test('성공 시나리오: 대시보드에서 로그아웃 버튼 클릭 시 세션 만료 및 로그인 화면으로 이동', async ({ page }) => {
        // 1. 먼저 올바른 계정으로 로그인하여 대시보드로 진입합니다.
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');

        // 2. 대시보드 페이지가 완전히 열릴 때까지 대기
        await page.waitForURL('**/admin/dashboard');
        const dashboardHeader = page.locator('h2:has-text("관리자 대시보드")');
        await expect(dashboardHeader).toBeVisible();

        // 3. '로그아웃' 버튼 클릭 (데스크톱 사이드바 버튼을 확실하게 타겟팅하도록 수정)
        const logoutButton = page.locator('.desktop-sidebar .logout-btn, button.menu-link.logout-btn').first();
        await logoutButton.click();

        // 4. 로그아웃 후 다시 로그인 페이지로 리다이렉트 되었는지 확인
        await page.waitForURL('**/login');
        await expect(page).toHaveURL('http://localhost:3000/login');

        // 5. 로그인 폼이 다시 정상적으로 보이는지 검증
        await expect(page.locator('#email')).toBeVisible();
        await expect(page.locator('#password')).toBeVisible();
    });
});