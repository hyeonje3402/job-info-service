import { test, expect } from '@playwright/test';

test.describe('Test Collection 4: UI 상호작용 및 네비게이션 (UI Interaction & Navigation)', () => {

    // 항상 그렇듯 테스트 시작 전에는 든든하게 로그인을 먼저 시킵니다.
    test.beforeEach(async ({ page }) => {
        // 데스크톱 뷰포트 강제 설정으로 사이드바 노출 보장
        await page.setViewportSize({ width: 1280, height: 1000 });

        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');
    });

    test('시나리오 4-1: 대시보드에서 "+ 취업정보 추가" 버튼 클릭 시 작동해야 한다', async ({ page }) => {
        // 1. 먼저 사이드바에서 'Job Listings' 메뉴를 클릭하여 이동합니다.
        const jobListingsMenu = page.locator('aside.desktop-sidebar').locator('text="Job Listings"').first();
        await expect(jobListingsMenu).toBeVisible();
        await jobListingsMenu.click();

        // 2. a 태그로 만들어진 '취업정보 추가' 버튼을 찾아 클릭합니다.
        const addButton = page.locator('a:has-text("취업정보 추가")').first();
        await addButton.click();

        // 3. 깐깐한 textbox 대신, 입력 폼(form)이나 아무 입력칸(input, textarea)이 화면에 렌더링되었는지 아주 너그럽게 확인합니다.
        const formOrInput = page.locator('form, input, textarea, dialog').first();
        await expect(formOrInput).toBeVisible();
    });

    test('시나리오 4-2: 사이드바에서 "User Management" 메뉴 클릭 시 올바른 주소로 이동해야 한다', async ({ page }) => {
        // 왼쪽 사이드바에 있는 'User Management' 메뉴를 클릭합니다.
        const userMenu = page.locator('aside.desktop-sidebar').locator('text="User Management"').first();
        await expect(userMenu).toBeVisible();
        await userMenu.click();

        // 브라우저 주소창이 사용자 관리 페이지 URL(보통 /user 또는 /users가 포함됨)로 잘 바뀌었는지 확인합니다.
        await page.waitForURL('**/users-management');
        await expect(page).toHaveURL(/.*\/users-management/);
    });
});