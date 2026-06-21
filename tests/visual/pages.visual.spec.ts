import { test, expect } from '@playwright/test';

/** @advisory 视觉回归基线 — 其余关键页（首跑写基线）。 */
const CANVAS_APP = process.env.CANVAS_APP ?? 'DEMO_APP';
const opt = { maxDiffPixelRatio: 0.02, animations: 'disabled' as const };

test('@advisory 主页 / 视觉基线', async ({ page }) => {
  await page.goto('/');
  await page.waitForLoadState('networkidle');
  await expect(page).toHaveScreenshot('home.png', opt);
});

test('@advisory 记录页 /records 视觉基线', async ({ page }) => {
  await page.goto('/records');
  await page.waitForLoadState('networkidle');
  await expect(page).toHaveScreenshot('records.png', opt);
});

test('@advisory 画布 视觉基线', async ({ page }) => {
  await page.goto(`/workflows/${CANVAS_APP}`);
  await page.waitForLoadState('networkidle');
  await expect(page).toHaveScreenshot('canvas.png', opt);
});
