import { test, expect } from '@playwright/test';

test.describe('Test Collection 10: 관리자 필수 기능 - 회원 관리 (User Management)', () => {

    test('시나리오 10-1: [회원 통제] 관리자는 회원 관리 메뉴에서 악성 유저를 조회하고 차단할 수 있어야 한다', async ({ page }) => {
        // 다이얼로그(confirm/alert) 발생 시 자동으로 수락(confirm)하도록 핸들러 글로벌 등록
        page.on('dialog', async (dialog) => {
            await dialog.accept();
        });

        // 1. 관리자 계정으로 로그인 후 대시보드 진입
        await page.goto('http://localhost:3000/login');
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');

        // 2. 사이드바에서 '회원 관리', 'User Management' 또는 'Users' 메뉴 클릭 (보이는 요소 선택)
        // 💡(학생들은 이 메뉴와 회원 목록 테이블을 프론트엔드에 추가해야 합니다!)
        await page.locator('text="Users"').or(page.locator('text="회원 관리"')).or(page.locator('text="User Management"')).filter({ visible: true }).first().click();

        // 3. 회원 목록 테이블이 화면에 보이는지 확인
        await expect(page.locator('table')).toBeVisible({ timeout: 5000 });

        // 3-1. 로딩 인디케이터가 사라지고 테이블 행 데이터가 준비될 때까지 대기
        await expect(page.locator('text="사용자 데이터를 로딩 중입니다..."')).not.toBeVisible({ timeout: 10000 });

        // 4. 첫 번째 행의 점 3개(⋮) 더보기 버튼을 클릭하여 드롭다운 메뉴 열기
        const moreButton = page.locator('table tbody tr').first().locator('button.more-btn');
        await moreButton.click();

        // 4-1. 특정 악성 유저의 '차단' 또는 'Block' 버튼을 찾아 클릭
        const blockButton = page.locator('button:has-text("차단")').or(page.locator('button:has-text("Block")')).first();
        await blockButton.click();

        // 5. '회원이 차단되었습니다'라는 성공 알림이 뜨는지 확인
        const successMessage = page.locator('text="차단되었습니다"').or(page.locator('text="User blocked"'));
        await expect(successMessage).toBeVisible({ timeout: 10000 });
    });
});
