import { test, expect } from '@playwright/test';

test.describe('채용 공고 관리 시나리오 테스트', () => {
    test.beforeEach(async ({ page }) => {
        // 가상 환경 지연 고려
        test.setTimeout(120000);

        // 데스크톱 뷰포트 강제 설정
        await page.setViewportSize({ width: 1280, height: 1000 });

        // alert 자동 수락 핸들러 등록
        page.on('dialog', async dialog => {
            await dialog.accept();
        });
    });

    test('성공 시나리오: 메인 페이지에서 키워드 검색 시 채용 공고 목록이 올바르게 필터링되어야 한다', async ({ page }) => {
        // 1. 메인JobList 페이지로 이동
        await page.goto('http://localhost:3000/', { waitUntil: 'load', timeout: 60000 });

        // 2. 검색창 대기 및 실제 DB에 초기화되는 키워드인 'SK키파운드리' 입력
        await page.waitForSelector('.search-input-field', { state: 'visible', timeout: 30000 });
        const searchInput = page.locator('.search-input-field');
        await searchInput.fill('SK키파운드리');

        // 3. 필터링된 결과 카드 검증 (초기 데이터 상 1개만 매칭되어야 함)
        const cards = page.locator('.job-posting-card');
        await expect(cards).toHaveCount(1, { timeout: 20000 });
        await expect(cards.first().locator('.card-company-name')).toContainText('SK키파운드리');
    });

    test('성공 시나리오: 로그인 후 신규 취업 데이터 소스(URL) 추가 요청 흐름이 정상 작동해야 한다', async ({ page }) => {
        // 1. 로그인 진행
        await page.goto('http://localhost:3000/login', { waitUntil: 'load', timeout: 60000 });
        await page.waitForSelector('#email', { state: 'visible', timeout: 30000 });
        await page.fill('#email', 'admin@example.com');
        await page.fill('#password', '1234');
        await page.click('button[type="submit"]');

        // 2. 대시보드로 리다이렉트 대기
        await page.waitForURL('**/admin/dashboard', { timeout: 60000 });

        // 3. 사이드바 메뉴를 통해 데이터 추가 페이지(/jobs/new)로 이동
        await page.goto('http://localhost:3000/jobs/new', { waitUntil: 'load', timeout: 60000 });

        // 4. URL 입력 탭 활성화
        await page.waitForSelector('.segmented-tab-btn', { state: 'visible', timeout: 30000 });
        const urlTabButton = page.locator('.segmented-tab-btn').filter({ hasText: 'URL 입력' });
        await urlTabButton.click();

        // 5. URL 폼 입력 및 등록 제출
        await page.waitForSelector('#url-input', { state: 'visible', timeout: 30000 });
        await page.fill('#url-input', 'https://recruit.navercorp.com/naver/job/list/developer');
        await page.click('button[type="submit"]');

        // 6. 성공 얼럿 수락 후 메인 리스트로 정상 복귀되는지 확인
        await page.waitForURL('http://localhost:3000/', { timeout: 60000 });
        await expect(page).toHaveURL('http://localhost:3000/');
    });
});
