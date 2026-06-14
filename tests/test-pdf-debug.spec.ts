import { test, expect } from '@playwright/test';

test('debug pdf upload network request', async ({ page }) => {
    test.setTimeout(60000);
    // 콘솔 로그 수집
    page.on('console', msg => {
        console.log(`[BROWSER CONSOLE] ${msg.type()}: ${msg.text()}`);
    });

    // 다이얼로그(alert) 수락 처리
    page.on('dialog', async dialog => {
        console.log(`[BROWSER DIALOG] ${dialog.message()}`);
        await dialog.accept();
    });

    // 네트워크 응답 수집
    page.on('request', request => {
        if (request.url().includes('/api/v1/sources/pdf')) {
            console.log(`[API REQUEST] URL: ${request.url()}`);
            console.log(`[API REQUEST] Headers: ${JSON.stringify(request.headers(), null, 2)}`);
        }
    });

    page.on('response', async response => {
        if (response.url().includes('/api/v1/sources/pdf')) {
            console.log(`[API RESPONSE] Status: ${response.status()}`);
            console.log(`[API RESPONSE] Headers: ${JSON.stringify(response.headers(), null, 2)}`);
            try {
                const text = await response.text();
                console.log(`[API RESPONSE] Body: ${text}`);
            } catch (e: any) {
                console.log(`[API RESPONSE] Body: (failed to read body: ${e.message})`);
            }
        }
    });

    // 1. 로그인
    console.log('Navigating to login page...');
    await page.goto('http://localhost:3000/login');
    await page.fill('#email', 'admin@example.com');
    await page.fill('#password', '1234');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/admin/dashboard');
    console.log('Logged in successfully!');

    // 2. Job Listings 메뉴 클릭 후 취업정보 추가 페이지 이동
    console.log('Navigating to job creation page...');
    const jobListingsMenu = page.locator('text="Job Listings"').first();
    await jobListingsMenu.click();
    const addButton = page.locator('a:has-text("취업정보 추가")').first();
    await addButton.click();
    // 모달이 열리도록 대기 (이동이 아닌 오버레이 모달이 뜨므로 waitForURL 제거)
    await page.waitForSelector('.modal-container', { state: 'visible' });

    // 3. 파일 입력 탭이 활성화되어 있는지 확인
    console.log('Uploading dummy.pdf...');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles('tests/dummy.pdf');

    // 4. 등록 버튼 클릭
    console.log('Submitting the form...');
    const submitBtn = page.locator('button[type="submit"]').first();
    await submitBtn.click();

    // 에러 상태 또는 성공 상태 메시지가 나타날 때까지 잠시 대기
    await page.waitForTimeout(5000);

    // 에러 메시지 텍스트 캡처
    const errorMsg = page.locator('.addjob-card .error-message, .addjob-card [style*="color: var(--color-error)"]');
    if (await errorMsg.count() > 0) {
        const text = await errorMsg.first().innerText();
        console.log(`[PAGE ERROR DISPLAYED]: ${text}`);
    } else {
        console.log('[PAGE ERROR DISPLAYED]: None found');
    }
});
