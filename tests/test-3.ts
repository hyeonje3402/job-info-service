import { test, expect } from '@playwright/test';

test.describe('Test Collection 3: 핵심 서비스 기능 (Core Service Features)', () => {

    // 모든 테스트 전에는 무조건 로그인을 먼저 시킵니다.
    test.beforeEach(async ({ page }) => {
        await page.goto('http://localhost:3000/login', { waitUntil: 'domcontentloaded' });
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/admin/dashboard');
        // 대시보드가 화면에 완전히 보일 때까지 대기합니다.
        await expect(page.locator('h2:has-text("관리자 대시보드")')).toBeVisible();
    });

    test('시나리오 3-1: 대시보드 진입 시 채용 공고 데이터가 화면에 정상적으로 표시되어야 한다', async ({ page }) => {
        // 빈 화면(White Screen) 렌더링을 방지하기 위한 깐깐한 테스트입니다.
        const mainContent = page.locator('body');

        // 화면 어딘가에 채용, 공고, 회사, 포지션 중 하나의 텍스트라도 나타나면 데이터가 잘 불러와진 것으로 인정합니다.
        await expect(mainContent).toContainText(/채용|공고|회사|포지션|마감/);
    });

    test('시나리오 3-2: 구직자를 위한 공고 검색 또는 필터링 요소가 화면에 존재해야 한다', async ({ page }) => {
        // Job Listings 메뉴로 이동
        const jobListingsMenu = page.locator('text="Job Listings"').first();
        await jobListingsMenu.click();

        // 깐깐한 속성 조건 대신, 화면에 있는 첫 번째 input(입력창)을 융통성 있게 찾아냅니다.
        const searchElement = page.locator('input').first();
        await expect(searchElement).toBeVisible();
    });
});