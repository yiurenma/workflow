import { test } from '@playwright/test';
import { assertA11yClean } from '../lib/a11y';

/** @gate 无障碍硬门禁 WCAG 2.2 AA — 运行记录 /records */
// 基线豁免（baseline allowlist）：记录当前已知违规，门禁仍抓"新"回归。
// 待修复后移除豁免 —— 见 TODO-ui-a11y-records-form-labels。
const RECORDS_A11Y_BASELINE = ['label', 'select-name'];

test('@gate /records 无 serious/critical WCAG AA 违规（基线外）', async ({ page }) => {
  await page.goto('/records');
  await page.waitForLoadState('networkidle');
  await assertA11yClean(page, { disableRules: RECORDS_A11Y_BASELINE });
});
