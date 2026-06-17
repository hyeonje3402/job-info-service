import { test, expect } from '@playwright/test';

test('Debug User Creation Flow', async ({ page }) => {
    test.setTimeout(60000);

    // Capture browser console logs
    page.on('console', msg => {
        console.log(`[CONSOLE] [${msg.type()}] ${msg.text()}`);
    });

    // Capture network requests and responses
    page.on('request', request => {
        console.log(`[REQ] ${request.method()} ${request.url()}`);
    });

    page.on('response', async response => {
        console.log(`[RES] ${response.status()} ${response.url()}`);
        if (response.url().includes('/api/v1/auth/register') || response.url().includes('/api/v1/users')) {
            try {
                const body = await response.text();
                console.log(`[RES BODY] ${response.url()}: ${body}`);
            } catch (e: any) {
                console.log(`[RES BODY ERROR] Failed to read response body: ${e.message}`);
            }
        }
    });

    page.on('requestfailed', request => {
        console.log(`[REQ FAIL] ${request.url()}: ${request.failure()?.errorText}`);
    });

    // 1. Log in as admin
    console.log('Navigating to login page...');
    await page.goto('http://localhost:3000/login');
    await page.fill('#email', 'admin@example.com');
    await page.fill('#password', '1234');
    await page.click('button[type="submit"]');

    // Wait for Dashboard redirection
    console.log('Waiting for admin dashboard...');
    await page.waitForURL('**/admin/dashboard');

    // 2. Go to user management add page
    console.log('Navigating to user add page...');
    await page.goto('http://localhost:3000/users-management/add');

    // 3. Fill user details
    console.log('Filling out user form with empty optional fields...');
    await page.fill('input[placeholder="이름 입력"]', '테스트유저');
    
    // We will generate a unique email to avoid conflicts with existing database records
    const uniqueEmail = `testuser_empty_${Date.now()}@example.com`;
    await page.fill('input[placeholder="example@domain.com"]', uniqueEmail);
    await page.fill('input[placeholder="••••••••"]', 'testpass1234');
    // Leaving optional fields blank
    await page.fill('input[placeholder="010-XXXX-XXXX"]', '');
    await page.fill('input[placeholder="https://example.com/avatar.jpg"]', '');

    // Submit form
    console.log('Submitting the registration form...');
    await page.click('.submit-btn');

    // Wait for a few seconds to see the outcome
    console.log('Waiting for response and redirection...');
    await page.waitForTimeout(5000);
});
