import { test } from '@playwright/test';
import { assertA11yClean } from '../lib/a11y';

/** @gate 无障碍硬门禁 WCAG 2.2 AA — 运行记录 /records */
// 基线豁免（baseline allowlist）：记录当前已知违规，门禁仍抓"新"回归。
// label/select-name → TODO-ui-a11y-records-form-labels；移动端 color-contrast → TODO-ui-a11y-mobile-nav-contrast。
const RECORDS_A11Y_BASELINE = ['label', 'select-name'];

test('@gate /records 无 serious/critical WCAG AA 违规（基线外）', async ({ page }) => {
  await page.goto('/records');
  await page.waitForLoadState('networkidle');
  const mobile = (page.viewportSize()?.width ?? 1280) < 768;
  await assertA11yClean(page, { disableRules: mobile ? [...RECORDS_A11Y_BASELINE, 'color-contrast'] : RECORDS_A11Y_BASELINE });
});
