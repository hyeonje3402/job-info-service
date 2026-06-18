import { test, expect } from '@playwright/test';

test.describe('Test Collection 14: 회원가입 유효성 검사 (Sign-up Validation)', () => {

    test('시나리오 14-1: [중복 가입 방어] 이미 가입된 이메일로 다시 회원가입을 시도하면 차단되어야 한다', async ({ page }) => {
        // 1. 회원가입 페이지 접속 (실제 주소인 /register로 이동)
        await page.goto('http://localhost:3000/register');

        // 2. 이미 DB에 존재하는 이메일과 회원 정보를 입력합니다.
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', 'newpassword123');
        await page.fill('#name', '중복가입자'); // 필수값 입력

        // 3. 회원가입(Submit) 버튼 클릭
        await page.click('button[type="submit"]');

        // 4. [핵심 검증 1] 회원가입에 실패하므로 페이지가 이동하지 않고 여전히 가입 페이지(/register)에 머물러야 합니다.
        await expect(page).toHaveURL(/.*register/);

        // 5. [핵심 검증 2] 화면에 중복된 이메일이라는 경고 메시지가 표시되어야 합니다.
        // 백엔드 반환 메시지("이미 사용 중인 이메일입니다.") 또는 프론트 폴백 문구를 유연하게 탐지합니다.
        const errorMessage = page.getByText('이미 사용 중인')
            .or(page.getByText('이메일이 이미 존재'))
            .or(page.getByText('Already exists'));
        await expect(errorMessage).toBeVisible({ timeout: 10000 });
    });

});
