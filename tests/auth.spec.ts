import { test, expect } from '@playwright/test';

test.describe('인증(로그인 및 로그아웃) 시나리오 테스트', () => {
    test.beforeEach(async ({ page }) => {
        // 가상 환경의 론칭 지연을 고려해 타임아웃을 120초로 상향
        test.setTimeout(120000);

        // 데스크톱 뷰포트 강제 설정
        await page.setViewportSize({ width: 1280, height: 1000 });

        // 브라우저 alert 자동 수락 핸들러 등록
        page.on('dialog', async dialog => {
            await dialog.accept();
        });

        // 로그인 페이지로 이동
        await page.goto('http://localhost:3000/login', { waitUntil: 'load', timeout: 60000 });
    });

    test('성공 시나리오: 올바른 계정 정보로 로그인한 뒤 정상적으로 로그아웃할 수 있어야 한다', async ({ page }) => {
        // 1. 이메일/비밀번호 필드 대기 및 올바른 계정 입력 후 로그인 시도
        await page.waitForSelector('#email', { state: 'visible', timeout: 30000 });
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');

        // 2. 관리자 대시보드 리다이렉트 검증
        await page.waitForURL('**/admin/dashboard', { timeout: 60000 });
        await expect(page).toHaveURL('http://localhost:3000/admin/dashboard');

        // 3. 로그아웃 버튼 노출 여부 확인 및 클릭
        const logoutButton = page.locator('.desktop-sidebar .logout-btn');
        await expect(logoutButton).toBeVisible({ timeout: 30000 });
        await logoutButton.click();

        // 4. 로그인 화면으로 안전하게 돌아왔는지 검증
        await page.waitForURL('**/login', { timeout: 60000 });
        await expect(page).toHaveURL('http://localhost:3000/login');
    });

    test('실패 시나리오: 존재하지 않는 계정 정보 입력 시 적합한 오류 메시지가 표시되어야 한다', async ({ page }) => {
        // 1. 잘못된 이메일과 비밀번호 입력 후 로그인 시도
        await page.waitForSelector('#email', { state: 'visible', timeout: 30000 });
        await page.fill('#email', 'nonexistent@example.com');
        await page.fill('#password', 'wrongpassword');
        await page.click('button[type="submit"]');

        // 2. 에러 텍스트가 표시될 때까지 대기 및 에러 배너 내용 검증
        const errorBanner = page.locator('div[style*="color: var(--color-error)"]');
        await expect(errorBanner).toBeVisible({ timeout: 30000 });
        await expect(errorBanner).toContainText('로그인에 실패했습니다');
    });
});
