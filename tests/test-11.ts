import { test, expect } from '@playwright/test';

test.describe('사용자 관리 페이지 (사용자 추가)', () => {

  test('테스트11: 중복된 전화번호로 등록 시도 시 실패해야 한다', async ({ page }) => {
    // 1. 사용자 추가 페이지 진입
    await page.goto('http://localhost:3000/users-management/add');
    await expect(page.getByText('사용자 정보 추가', { exact: true })).toBeVisible();

    // 2. 폼 데이터 세팅 (실패했던 'getByLabel' 부분을 'getByPlaceholder'로 수정)
    // await page.getByLabel('이름').fill('테스트유저'); // <-- 기존 실패 코드
    await page.getByPlaceholder('이름 입력').fill('테스트유저'); // 플레이스홀더로 이름 필드 찾기

    await page.getByLabel('이메일 주소').fill('duplicate_test@example.com');
    await page.getByLabel('초기 비밀번호').fill('password123!');
    await page.getByLabel('연락처 (선택)').fill('010-1234-5678');

    // 3. 백엔드 API 요청 가로채기
    await page.route('**/api/users', async route => {
      const json = { message: '이미 등록된 전화번호입니다.' };
      await route.fulfill({ status: 400, json });
    });

    // 4. '사용자 정보 등록' 버튼 클릭
    await page.getByRole('button', { name: '사용자 정보 등록' }).click();

    // 5. 실패 시 화면에 에러 메시지가 정상적으로 노출되는지 검증
    await expect(page.getByText('이미 등록된 전화번호입니다.')).toBeVisible();
  });

});