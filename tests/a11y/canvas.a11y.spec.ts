import { test } from '@playwright/test';
import { assertA11yClean } from '../lib/a11y';

/** @gate 无障碍硬门禁 WCAG 2.2 AA — 画布 /workflows/DEMO_APP */
const CANVAS_APP = process.env.CANVAS_APP ?? 'DEMO_APP';

// 基线豁免（baseline allowlist）：记录当前已知违规，门禁仍抓"新"回归。
// 待修复后移除豁免 —— 见 TODO-ui-a11y-canvas-color-contrast。
const CANVAS_A11Y_BASELINE = ['color-contrast'];

test('@gate 画布无 serious/critical WCAG AA 违规（基线外）', async ({ page }) => {
  await page.goto(`/workflows/${CANVAS_APP}`);
  await page.waitForLoadState('networkidle');
  await assertA11yClean(page, { disableRules: CANVAS_A11Y_BASELINE });
});
