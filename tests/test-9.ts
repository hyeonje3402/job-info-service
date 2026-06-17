import { test, expect } from '@playwright/test';

test.describe('Test Collection 9: 시스템 환경 설정 기능 (System Settings)', () => {

    test('시나리오 9-1: [환경 설정] 관리자가 대시보드에서 시스템 설정 메뉴에 진입하여 이메일 알림을 활성화하고 저장할 수 있어야 한다', async ({ page }) => {
        // 1. 로그인 후 대시보드 진입
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');

        // 2. 사이드바에서 'Settings' 또는 '시스템 설정' 메뉴 클릭 
        // 💡(학생들은 코드를 분석하고 직접 이 메뉴를 프론트엔드에 추가해야 테스트를 통과할 수 있습니다)
        await page.locator('text="Settings"').or(page.locator('text="시스템 설정"')).first().click();

        // 3. 알림 수신 체크박스를 찾아 체크
        const notificationCheckbox = page.locator('input[type="checkbox"]').first();
        await notificationCheckbox.check();

        // 4. '저장(Save)' 버튼 클릭
        await page.locator('button:has-text("저장")').or(page.locator('button:has-text("Save")')).first().click();

        // 5. '설정이 저장되었습니다' 성공 알림이 화면에 뜨는지 확인
        const successMessage = page.locator('text="설정이 저장되었습니다"').or(page.locator('text="Settings saved"'));
        await expect(successMessage).toBeVisible({ timeout: 5000 });
    });
});
