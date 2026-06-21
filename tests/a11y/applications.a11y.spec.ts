import { test } from '@playwright/test';
import { assertA11yClean } from '../lib/a11y';

/** @gate 无障碍硬门禁 WCAG 2.2 AA — 应用列表 /workflows */
test('@gate /workflows 无 serious/critical WCAG AA 违规', async ({ page }) => {
  await page.goto('/workflows');
  await page.waitForLoadState('networkidle');
  await assertA11yClean(page);
});
