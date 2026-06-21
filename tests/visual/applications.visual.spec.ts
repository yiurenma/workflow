import { test, expect } from '@playwright/test';

/**
 * @advisory 视觉回归（软门禁）— 首跑生成基线，后续 diff 防"不小心改坏样子"。
 * 失败不阻断合并，转人工/AI 复核。
 */
test('@advisory /workflows 视觉基线', async ({ page }) => {
  await page.goto('/workflows');
  await page.waitForLoadState('networkidle');
  // 关闭动画、容忍极小像素差，降低 flaky
  await expect(page).toHaveScreenshot('workflows-list.png', { maxDiffPixelRatio: 0.02, animations: 'disabled' });
});
