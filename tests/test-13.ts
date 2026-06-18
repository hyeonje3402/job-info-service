import { test, expect } from '@playwright/test';

test.describe('Test Collection 13: 일반 사용자 권한 검사 (General User Role)', () => {

    test('시나리오 13-1: [일반 사용자 로그인] 일반 계정으로 로그인 시 채용공고 목록으로 이동하고 관리자 기능이 차단되어야 한다', async ({ page }) => {
        // 1. 로그인 페이지 접속 및 일반 사용자 정보 입력
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'tester@example.com');
        await page.fill('#password', '1234!');
        await page.click('button[type="submit"]');

        // 2. 로그인 즉시 메인 채용공고 목록 페이지(/)로 이동하는지 URL 검증
        // (포트번호 뒤가 깔끔하게 / 이거나 메인 주소인지 확인합니다)
        await page.waitForURL('http://localhost:3000/');
        await expect(page).toHaveURL('http://localhost:3000/');

        // 3. 일반 사용자용 메뉴 활성화 여부 검사
        // 'Job Listings' 메뉴는 화면에 정상적으로 보여야 합니다.
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await expect(jobListingsMenu).toBeVisible();

        // 4. 관리자 전용 메뉴 노출 차단 검사
        // 일반 사용자 화면에는 'User Management'나 'Settings' 글자가 절대 보이지 않아야 합니다.
        const adminUserMenu = page.locator('text="User Management"');
        const adminSettingMenu = page.locator('text="Settings"').or(page.locator('text="시스템 설정"'));
        
        await expect(adminUserMenu).not.toBeVisible();
        await expect(adminSettingMenu).not.toBeVisible();

        // 5. [강제 주소 접속 차단 검사] 일반 사용자가 주소창에 대시보드 URL을 강제로 쳐서 들어가려고 할 때의 방어벽 검사
        await page.goto('http://localhost:3000/admin/dashboard');

        // 관리자 페이지 안으로 진입하지 못하고, 다시 튕겨 나가거나 접근 거부 처리가 되는지 주소창을 검증합니다.
        // (접근이 차단되어 대시보드 주소가 아니게 되었는지 확인하는 로직입니다)
        await expect(page).not.toHaveURL('**/admin/dashboard');
    });
});
