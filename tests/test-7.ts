import { test, expect } from '@playwright/test';
import path from 'path';

test.describe('Test Collection 7: 파일 업로드 기능 검사', () => {
    test('시나리오 7-1: PDF 정상 업로드 확인', async ({ page }) => {
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');

        await page.locator('text="Job Listings"').first().click();
        await page.locator('text="취업정보 추가"').first().click();

        const filePath = path.join(__dirname, 'dummy.pdf');
        await page.locator('input[type="file"]').first().setInputFiles(filePath);

        await expect(page.locator('body')).toContainText('dummy.pdf', { timeout: 5000 });
    });
});