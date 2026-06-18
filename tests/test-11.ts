import { test, expect } from '@playwright/test';

test.describe('사용자 관리 페이지 (사용자 추가)', () => {

  test.beforeEach(async ({ page }) => {
    test.setTimeout(120000);
    await page.setViewportSize({ width: 1280, height: 1000 });

    // alert 자동 수락 핸들러 등록
    page.on('dialog', async dialog => {
      await dialog.accept();
    });

    // 1. 관리자로 로그인
    await page.goto('http://localhost:3000/login', { waitUntil: 'load', timeout: 60000 });
    await page.waitForSelector('#email', { state: 'visible', timeout: 30000 });
    await page.fill('#email', 'admin@example.com');
    await page.fill('#password', '1234');
    await page.click('button[type="submit"]');

    // 2. 대시보드로 이동 대기
    await page.waitForURL('**/admin/dashboard', { timeout: 60000 });
  });

  test('테스트11: 중복된 전화번호로 등록 시도 시 실패해야 한다', async ({ page }) => {
    // 1. 사용자 추가 페이지 진입
    await page.goto('http://localhost:3000/users-management/add');
    await expect(page.getByText('사용자 정보 추가', { exact: true })).toBeVisible();

    // 2. 폼 데이터 세팅
    await page.getByPlaceholder('이름 입력').fill('테스트유저');
    await page.getByPlaceholder('example@domain.com').fill('duplicate_test@example.com');
    await page.getByPlaceholder('••••••••').fill('password123!');
    await page.getByPlaceholder('010-XXXX-XXXX').fill('010-1234-5678');

    // 3. 백엔드 API 요청 가로채기
    await page.route('**/api/v1/auth/register', async route => {
      const json = { message: '이미 등록된 전화번호입니다.' };
      await route.fulfill({ status: 400, json });
    });

    // 4. '사용자 정보 등록' 버튼 클릭
    await page.getByRole('button', { name: '사용자 정보 등록' }).click();

    // 5. 실패 시 화면에 에러 메시지가 정상적으로 노출되는지 검증
    await expect(page.getByText('이미 등록된 전화번호입니다.')).toBeVisible();
  });

});