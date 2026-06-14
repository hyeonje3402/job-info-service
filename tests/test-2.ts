import { test, expect } from '@playwright/test';

test.describe('Test Collection 2: 보안 및 예외 처리 (Security & Robustness)', () => {

    test('시나리오 2-1: [보안] 비로그인 상태로 관리자 대시보드 URL 강제 진입 시도 시 튕겨내야 한다', async ({ page }) => {
        await page.goto('http://localhost:3000/admin/dashboard');
        await expect(page).toHaveURL(/.*\/login/);
    });

    test('시나리오 2-2: [예외 처리] 비밀번호 입력란에 악의적인 SQL 특수문자 입력 시 앱이 뻗지 않아야 한다', async ({ page }) => {
        await page.goto('http://localhost:3000/login');

        // 이메일 칸은 정상적으로 적고, 검사가 없는 비밀번호 칸에 폭탄(SQL 인젝션)을 투하합니다!
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', "' OR '1'='1");
        await page.click('button[type="submit"]');

        // 서버가 막아내고 프론트엔드가 빨간색 에러를 띄우는지 확인
        const errorMsg = page.locator('div[style*="color: var(--color-error)"], div[style*="color:var(--color-error)"]');
        await expect(errorMsg).toBeVisible();
    });

    test('시나리오 2-3: [예외 처리] 1000자가 넘는 비정상적인 긴 문자열 입력 시 화면이 멈추지 않아야 한다', async ({ page }) => {
        await page.goto('http://localhost:3000/login');
        const longText = 'A'.repeat(1000);
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', longText);
        await page.click('button[type="submit"]');

        const errorMsg = page.locator('div[style*="color: var(--color-error)"], div[style*="color:var(--color-error)"]');
        await expect(errorMsg).toBeVisible();
    });
});