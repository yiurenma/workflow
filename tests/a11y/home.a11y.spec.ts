import { test } from '@playwright/test';
import { assertA11yClean } from '../lib/a11y';

/** @gate 无障碍 WCAG 2.2 AA — 主页与关于页（移动端基线豁免共享 nav 对比度） */
const mobileBaseline = (page: import('@playwright/test').Page) =>
  ((page.viewportSize()?.width ?? 1280) < 768 ? ['color-contrast'] : []);

test('@gate 主页 / 无 serious/critical WCAG AA 违规（基线外）', async ({ page }) => {
  await page.goto('/');
  await page.waitForLoadState('networkidle');
  await assertA11yClean(page, { disableRules: mobileBaseline(page) });
});

test('@gate 关于页 /about 无 serious/critical WCAG AA 违规（基线外）', async ({ page }) => {
  await page.goto('/about');
  await page.waitForLoadState('networkidle');
  await assertA11yClean(page, { disableRules: mobileBaseline(page) });
});
