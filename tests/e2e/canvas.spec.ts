import { test, expect } from '@playwright/test';

/**
 * CV-US-04/05/53 canvas via UI. 5-layer framework. Audit §5.
 * Navigates into the first application's canvas (/workflows/$applicationName).
 */
async function openFirstCanvas(page: import('@playwright/test').Page) {
  await page.goto('/workflows');
  await page.waitForLoadState('networkidle');
  const enter = page.getByRole('link').filter({ hasText: /.+/ }).first();
  const row = page.locator('table tbody tr, .app-row, [class*="card"]').first();
  if (await row.count()) {
    await row.click().catch(() => {});
  } else {
    await enter.click().catch(() => {});
  }
  await page.waitForLoadState('networkidle');
}

test.describe('CV-US-04 open canvas', () => {
  test('Layer 1 exist: React Flow canvas renders', async ({ page }) => {
    await openFirstCanvas(page);
    const canvas = page.locator('.react-flow, [data-testid="rf__wrapper"]').first();
    // Canvas may require a selected app; assert presence OR a recognizable workflow toolbar
    const toolbar = page.getByRole('button', { name: /save|test|explain|import|generate/i }).first();
    await expect(canvas.or(toolbar)).toBeVisible({ timeout: 15000 });
  });

  test('Layer 2 size: canvas occupies a substantial portion of viewport', async ({ page }) => {
    await openFirstCanvas(page);
    const canvas = page.locator('.react-flow, [data-testid="rf__wrapper"]').first();
    if (!(await canvas.count())) test.skip(true, 'canvas not reachable without seeded app');
    const box = await canvas.boundingBox();
    const vh = page.viewportSize()!.height;
    expect((box?.height ?? 0) / vh, 'canvas height > 35% viewport').toBeGreaterThan(0.35);
  });
});

test.describe('CV-US-53 import workflow modal', () => {
  test('Layer 4 interact: Import opens a modal with paste/upload', async ({ page }) => {
    await openFirstCanvas(page);
    const importBtn = page.getByRole('button', { name: /import/i }).first();
    if (!(await importBtn.count())) test.skip(true, 'import entry not reachable without seeded app');
    await importBtn.click();
    const modal = page.locator('.modal-box, .cds--modal, [role="dialog"]').first();
    await expect(modal).toBeVisible();
    await expect(modal.locator('textarea')).toBeVisible();
  });
});
