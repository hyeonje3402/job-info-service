import { test, expect } from '@playwright/test';

test.describe('Test Collection 1: 인증 및 세션 관리 시나리오', () => {

    // 각 테스트가 시작되기 전에 무조건 로그인 페이지로 먼저 이동합니다.
    test.beforeEach(async ({ page }) => {
        await page.goto('http://localhost:3000/login', { waitUntil: 'domcontentloaded' });
    });

    test('시나리오 1-1: 올바른 계정 정보로 로그인 성공 시 대시보드로 이동해야 한다', async ({ page }) => {
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');

        // 대시보드 진입 및 간판(h2) 확인
        await page.waitForURL('**/admin/dashboard');
        await expect(page).toHaveURL('http://localhost:3000/admin/dashboard');
        
        const dashboardHeader = page.locator('h2:has-text("관리자 대시보드")');
        await expect(dashboardHeader).toBeVisible();
    });

    test('시나리오 1-2: 잘못된 계정 정보 입력 시 에러 메시지가 나타나야 한다', async ({ page }) => {
        await page.waitForLoadState('networkidle');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', 'wrong_password');
        await page.click('button[type="submit"]');

        // 빨간색 에러 메시지 태그가 화면에 뜨는지 확인
        const errorMsg = page.locator('div[style*="color: var(--color-error)"], div[style*="color:var(--color-error)"]');
        await expect(errorMsg).toBeVisible();
    });

    test('시나리오 1-3: 대시보드에서 로그아웃 클릭 시 다시 로그인 화면으로 쫓겨나야 한다', async ({ page }) => {
        // 1. 먼저 로그인 시키기
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');

        // 2. 로그아웃 버튼 찾아서 누르기 (모바일/데스크톱 중복 방지를 위해 first() 사용)
        const logoutButton = page.locator('.desktop-sidebar .logout-btn, button.menu-link.logout-btn').first();
        await logoutButton.click();

        // 3. 로그인 화면으로 돌아왔는지 확인
        await page.waitForURL('**/login');
        await expect(page).toHaveURL('http://localhost:3000/login');
    });
});
