import { test, expect } from '@playwright/test';

/**
 * IBM Carbon 设计 token 合规（CV-AC-36 / CV-AC-48）—— Layer 5 计算样式断言。
 * 颜色：导航 Gray 100 #161616 = rgb(22,22,22)；主色 Blue 60 #0f62fe = rgb(15,98,254)。
 * 形状：0px 圆角（直角，Carbon 硬约束）。
 */
const rgb = (r: number, g: number, b: number) => `rgb(${r}, ${g}, ${b})`;

test.describe('Carbon 设计 token', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/workflows');
    await page.waitForLoadState('networkidle');
  });

  test('@gate 导航栏背景 Gray 100 #161616 且 0px 圆角', async ({ page }) => {
    const nav = page.locator('header, nav, [class*="header"]').first();
    await expect(nav).toBeVisible();
    const { bg, radius } = await nav.evaluate((el) => {
      const s = getComputedStyle(el);
      return { bg: s.backgroundColor, radius: s.borderRadius };
    });
    expect(bg, '导航背景 Gray 100').toBe(rgb(22, 22, 22));
    expect(radius === '0px' || radius === '', '导航 0px 圆角').toBeTruthy();
  });

  test('@gate 标准按钮均为 0px 圆角（矩形；FAB 圆形为设计例外，不在 .btn 内）', async ({ page }) => {
    const btns = page.locator('.btn:visible');
    const n = await btns.count();
    test.skip(n === 0, '当前视口无可见标准 .btn');
    for (let i = 0; i < n; i++) {
      const radius = await btns.nth(i).evaluate((el) => getComputedStyle(el).borderRadius);
      expect(radius === '0px' || radius === '', `第 ${i} 个 .btn 应为 0px 圆角`).toBeTruthy();
    }
  });

  test('@advisory 主按钮强调色 Blue 60 #0f62fe', async ({ page }) => {
    const primary = page.locator('button.btn-primary').filter({ visible: true } as any).first();
    test.skip(!(await primary.count()), '当前视口无 .btn-primary 可见');
    const bg = await primary.evaluate((el) => getComputedStyle(el).backgroundColor);
    expect(bg, '主色 Blue 60').toBe(rgb(15, 98, 254));
  });
});
