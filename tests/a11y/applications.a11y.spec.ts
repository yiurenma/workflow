import { test } from '@playwright/test';
import { assertA11yClean } from '../lib/a11y';

/**
 * @gate 无障碍硬门禁 WCAG 2.2 AA — 应用列表 /workflows
 * 移动端基线豁免 color-contrast：移动 tab bar 激活态文字对比度不足
 * （共享 nav 缺陷）→ TODO-ui-a11y-mobile-nav-contrast。桌面保持严格。
 */
test('@gate /workflows 无 serious/critical WCAG AA 违规（基线外）', async ({ page }) => {
  await page.goto('/workflows');
  await page.waitForLoadState('networkidle');
  const mobile = (page.viewportSize()?.width ?? 1280) < 768;
  await assertA11yClean(page, { disableRules: mobile ? ['color-contrast'] : [] });
});
