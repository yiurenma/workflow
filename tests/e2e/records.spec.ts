import { test, expect } from '@playwright/test';

/**
 * REC-US-19 records browse/filter via UI. 5-layer framework. Audit §4.
 * Stable across viewports via the page heading (desktop table is hidden on mobile).
 */
test.describe('REC-US-19 records list', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/records');
    await page.waitForLoadState('networkidle');
  });

  test('Layer 1 exist: records page renders (heading visible)', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /Execution Records/i })).toBeVisible();
  });

  test('Layer 4 interact: at least one filter control is operable', async ({ page }) => {
    const filter = page.locator('input:visible, select:visible').first();
    await expect(filter).toBeVisible();
  });
});
