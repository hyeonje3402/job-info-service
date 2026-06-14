import { test, expect } from '@playwright/test';

test.describe('Test Collection 6: 폼 유효성 검사 및 모바일 반응형 (Form Validation & Responsiveness)', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');
    });

    test('시나리오 6-1: [폼 검증 저격] 필수 입력값을 비우고 공고를 등록하려 하면 차단해야 한다', async ({ page }) => {
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();
        const addButton = page.locator('a:has-text("취업정보 추가")').first();
        await addButton.click();

        const submitBtn = page.locator('button[type="submit"]').first();
        await submitBtn.click();

        const errorMsg = page.locator('text=/필수|입력|required/i').or(page.locator('div[style*="color: var(--color-error)"]'));
        await expect(errorMsg.first()).toBeVisible();
    });

    test('시나리오 6-2: [반응형 레이아웃 저격] 모바일 화면 크기(iPhone 사이즈)로 줄였을 때 가로 스크롤이 생기거나 화면이 깨지지 않아야 한다', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 812 });
        await page.waitForTimeout(500);

        const isOverflowing = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth);
        expect(isOverflowing).toBe(false);
    });

}); 