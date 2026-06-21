import { test } from '@playwright/test';
import { assertWebVitalsWithinBudget } from '../lib/webvitals';

/**
 * @gate 性能体验硬门禁 — Core Web Vitals（LCP ≤2.5s / CLS ≤0.1 / INP ≤200ms）。
 * 注：本地 mock 环境的数值仅作冒烟基线；UAT/生产跑才有真实意义。
 */
test('@gate /workflows Core Web Vitals 在预算内', async ({ page }) => {
  await page.goto('/workflows');
  await page.waitForLoadState('networkidle');
  await assertWebVitalsWithinBudget(page);
});

test('@gate /records Core Web Vitals 在预算内', async ({ page }) => {
  await page.goto('/records');
  await page.waitForLoadState('networkidle');
  await assertWebVitalsWithinBudget(page);
});
