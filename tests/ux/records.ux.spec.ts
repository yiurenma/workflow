import { test, expect } from '@playwright/test';
import { assertNoHorizontalClip, assertTouchTargetSize } from '../lib/ux';

/** 可用性样板 — 记录页（ISO25010）。@gate 为客观硬约束。 */
test.describe('记录页 可用性', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/records');
    await page.waitForLoadState('networkidle');
  });

  test('@gate 响应式：记录页无横向裁切', async ({ page }) => {
    await assertNoHorizontalClip(page);
  });

  test('@gate Operability：页面标题可见', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /Execution Records|记录/i })).toBeVisible();
  });
});

/** 可用性样板 — 导航触控目标（移动端尤为关键）。 */
test.describe('导航 可用性', () => {
  test('@gate Operability：导航项可见且为有效点击区', async ({ page }) => {
    await page.goto('/workflows');
    await page.waitForLoadState('networkidle');
    const navLink = page.getByRole('link', { name: /records|workflows|记录|应用/i }).filter({ visible: true } as any).first();
    await assertTouchTargetSize(navLink, 24); // 链接最小高度（图标+文字）
  });
});
