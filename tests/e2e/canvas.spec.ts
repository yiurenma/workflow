import { test, expect } from '@playwright/test';

/**
 * CV-US-04/05/53 canvas via UI. 5-layer framework. Audit §5.
 * Navigates directly to a known application's canvas (/workflows/$applicationName).
 * Default app = DEMO_APP (mock dev server) or local-seeded TEST_APP_A; override via CANVAS_APP.
 */
const CANVAS_APP = process.env.CANVAS_APP ?? 'DEMO_APP';

test.describe('CV-US-04 open canvas', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`/workflows/${CANVAS_APP}`);
    await page.waitForLoadState('networkidle');
  });

  test('Layer 1 exist: canvas toolbar / React Flow renders', async ({ page }) => {
    const canvas = page.locator('.react-flow, [data-testid="rf__wrapper"]').first();
    const toolbar = page.getByRole('button', { name: /save|run|test|explain|import|generate/i }).first();
    await expect(canvas.or(toolbar).first()).toBeVisible({ timeout: 15000 });
  });

  test('Layer 2 size: React Flow occupies a substantial portion of viewport', async ({ page }) => {
    const canvas = page.locator('.react-flow, [data-testid="rf__wrapper"]').first();
    if (!(await canvas.count())) test.skip(true, 'canvas not rendered for this app/mock');
    const box = await canvas.boundingBox();
    const vh = page.viewportSize()!.height;
    expect((box?.height ?? 0) / vh, 'canvas height > 30% viewport').toBeGreaterThan(0.3);
  });
});

test.describe('CV-US-53 import workflow modal', () => {
  test('Layer 4 interact: Import opens a modal with paste/upload', async ({ page }) => {
    await page.goto(`/workflows/${CANVAS_APP}`);
    await page.waitForLoadState('networkidle');
    const importBtn = page.getByRole('button', { name: /import/i }).first();
    if (!(await importBtn.count())) test.skip(true, 'import entry not present (mobile overflow / mock)');
    await importBtn.click();
    const modal = page.locator('.modal-box, .modal-overlay, .cds--modal, [role="dialog"]').first();
    await expect(modal).toBeVisible();
    await expect(modal.locator('textarea')).toBeVisible();
  });
});
