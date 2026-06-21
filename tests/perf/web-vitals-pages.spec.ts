import { test } from '@playwright/test';
import { assertWebVitalsWithinBudget } from '../lib/webvitals';

/** @gate Core Web Vitals — 其余关键页（主页 / 画布）。 */
const CANVAS_APP = process.env.CANVAS_APP ?? 'DEMO_APP';

test('@gate 主页 / Core Web Vitals 在预算内', async ({ page }) => {
  await page.goto('/');
  await page.waitForLoadState('networkidle');
  await assertWebVitalsWithinBudget(page);
});

test('@gate 画布 Core Web Vitals 在预算内', async ({ page }) => {
  await page.goto(`/workflows/${CANVAS_APP}`);
  await page.waitForLoadState('networkidle');
  await assertWebVitalsWithinBudget(page);
});
