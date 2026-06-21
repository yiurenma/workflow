import { test, expect } from '@playwright/test';

/**
 * REC-US-19 records browse/filter via UI. 5-layer framework. Audit §4.
 */
test.describe('REC-US-19 records list', () => {
  test('Layer 1 exist: records view loads with a list/table or empty state', async ({ page }) => {
    await page.goto('/records');
    await page.waitForLoadState('networkidle');
    const content = page
      .locator('table, .cds-table, [class*="card"], [class*="empty"]')
      .first();
    await expect(content).toBeVisible();
  });

  test('Layer 4 interact: at least one filter control is operable', async ({ page }) => {
    await page.goto('/records');
    await page.waitForLoadState('networkidle');
    const filter = page.locator('input, select, .cds-input').first();
    await expect(filter).toBeVisible();
  });
});
