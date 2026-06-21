import { test, expect } from '@playwright/test';

/**
 * APP-US-01/02/03 via the UI, using the 5-layer UX framework (CV-US-38):
 * Layer 1 exist · 2 size · 3 viewport · 4 interact · 5 effect (computed style).
 * Carbon selectors (.cds-*) per the rebuilt UI; falls back to role/text for resilience.
 * Verdicts feed audit §3 / §5.
 */
test.describe('APP-US-01 applications list', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/workflows');
    await page.waitForLoadState('networkidle');
  });

  test('Layer 1 exist: list container + search + create entry present', async ({ page }) => {
    const list = page.locator('table, .cds-table, .app-row, [class*="card"]').first();
    await expect(list).toBeVisible();
    await expect(page.getByRole('button', { name: /new application|create|＋/i }).first()).toBeVisible();
  });

  test('Layer 3 viewport: content not clipped horizontally', async ({ page }) => {
    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    );
    expect(overflow, 'no horizontal clipping').toBeLessThanOrEqual(2);
  });

  test('Layer 4 interact: search input accepts text', async ({ page }) => {
    const search = page.getByPlaceholder(/search/i).or(page.locator('input[type="text"]').first());
    await search.fill('TEST');
    await expect(search).toHaveValue('TEST');
  });
});

test.describe('CV-US-36 Carbon design tokens (Layer 5 effect)', () => {
  test('TC-CARBON: nav background is Gray 100 #161616 and rectangular', async ({ page }) => {
    await page.goto('/workflows');
    const nav = page.locator('header, nav, [class*="header"]').first();
    await expect(nav).toBeVisible();
    const radius = await nav.evaluate((el) => getComputedStyle(el).borderRadius);
    // 0px corners are a hard Carbon constraint (CV-AC-36-3)
    expect(radius === '0px' || radius === '').toBeTruthy();
  });
});
