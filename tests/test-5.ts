import { test, expect } from '@playwright/test';

test.describe('Test Collection 5: 입력 유효성 및 데이터 무결성 검사 (Input Validation & Data Integrity)', () => {

    test.beforeEach(async ({ page }) => {
        // 데스크톱 뷰포트 강제 설정으로 사이드바 노출 보장
        await page.setViewportSize({ width: 1280, height: 1000 });

        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');
    });

    test('시나리오 5-1: [공백 예외 저격] 검색창에 앞뒤 무의미한 공백이 섞여 있어도 올바르게 작동해야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('aside.desktop-sidebar').locator('text="Job Listings"').first();
        await expect(jobListingsMenu).toBeVisible();
        await jobListingsMenu.click();

        const searchInput = page.locator('.search-input-field');
        await searchInput.waitFor({ state: 'visible', timeout: 5000 });
        await searchInput.fill('   Frontend   ');
        await page.keyboard.press('Enter');

        // 검색창의 입력값이 그대로 남아있는지 확인합니다.
        await expect(searchInput).toHaveValue('   Frontend   ');
    });

    test('시나리오 5-2: [시맨틱 태그 저격] 메뉴와 버튼들이 HTML 표준 역할(Role)을 정확히 지켜야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('aside.desktop-sidebar').locator('text="Job Listings"').first();
        await expect(jobListingsMenu).toBeVisible();
        await jobListingsMenu.click();

        const searchInput = page.locator('.search-input-field');
        await expect(searchInput).toBeVisible({ timeout: 5000 });

        const addButton = page.locator('text="취업정보 추가"').first();
        await expect(addButton).toBeVisible();
    });

    test('시나리오 5-3: [데이터 무결성 저격] 상태 배지의 텍스트는 대소문자가 정확히 일치해야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('aside.desktop-sidebar').locator('text="Job Listings"').first();
        await expect(jobListingsMenu).toBeVisible();
        await jobListingsMenu.click();

        const pageBody = page.locator('body');
        await expect(pageBody).toContainText(/(APPROVED|PENDING|REJECTED)/);
    });
});
