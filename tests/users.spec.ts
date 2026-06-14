import { test, expect } from '@playwright/test';

test.describe('사용자 관리 시나리오 테스트', () => {
    test.beforeEach(async ({ page }) => {
        // 가상 환경 지연 고려
        test.setTimeout(120000);

        // 데스크톱 뷰포트 강제 설정
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

        // 3. 사용자 관리 페이지로 이동
        await page.goto('http://localhost:3000/users-management', { waitUntil: 'load', timeout: 60000 });
    });

    test('성공 시나리오: 사용자 검색이 정상 동작해야 한다', async ({ page }) => {
        // 1. 검색창 필드 대기 및 검색 키워드 입력
        await page.waitForSelector('.search-input-field', { state: 'visible', timeout: 30000 });
        await page.fill('.search-input-field', '김지훈');

        // 2. 필터링된 테이블 행 검증
        const tableRows = page.locator('.user-data-table tbody tr');
        await expect(tableRows).toHaveCount(1, { timeout: 15000 });
        await expect(tableRows.first()).toContainText('jihoon.kim@example.com');
    });

    test('성공 시나리오: 신규 사용자를 추가할 수 있어야 한다', async ({ page }) => {
        // 1. '사용자정보추가' 버튼 클릭
        await page.waitForSelector('.userlist-add-btn', { state: 'visible', timeout: 30000 });
        await page.click('.userlist-add-btn');

        // 2. 사용자 추가 페이지(/users-management/add) 이동 확인
        await page.waitForURL('**/users-management/add', { timeout: 60000 });

        // 3. 폼 입력값 채우기 (병렬 테스트 수행 시 데이터 중복 충돌을 방지하기 위해 랜덤 이메일 사용)
        const uniqueSuffix = `${Date.now()}_${Math.floor(Math.random() * 1000)}`;
        const email = `gildong.hong_${uniqueSuffix}@example.com`;

        await page.waitForSelector('input[placeholder="이름 입력"]', { state: 'visible', timeout: 30000 });
        await page.fill('input[placeholder="이름 입력"]', '홍길동');
        await page.fill('input[placeholder="example@domain.com"]', email);
        await page.fill('input[placeholder="••••••••"]', 'testpassword123');
        await page.fill('input[placeholder="010-XXXX-XXXX"]', '010-9999-8888');
        
        // 4. 등록 완료 제출
        await page.click('.submit-btn');

        // 5. 성공 메시지 노출 및 목록으로 자동 이동되는지 검증
        const successMessage = page.locator('.success');
        await expect(successMessage).toBeVisible({ timeout: 20000 });
        await page.waitForURL('**/users-management', { timeout: 60000 });
    });
});
