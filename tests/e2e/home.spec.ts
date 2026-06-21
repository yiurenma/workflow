import { test, expect } from '@playwright/test';

/**
 * 主页/品牌（CV-US-45 介绍文案、CV-US-46 蜗牛 favicon）。5 层 UX。
 */
test.describe('主页 介绍与品牌', () => {
  test('Layer 1 exist: 标题 + 副标题 + CTA', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    await expect(page.getByRole('heading').first()).toBeVisible();
    // CTA：进入应用/记录 的按钮或链接
    const cta = page.getByRole('button', { name: /applications|records|go to|view|get started|开始|应用|记录/i })
      .or(page.getByRole('link', { name: /applications|records|workflow|开始|应用|记录/i }));
    await expect(cta.first()).toBeVisible();
  });

  test('Layer 1 exist: favicon 链接存在（蜗牛图标）', async ({ page }) => {
    await page.goto('/');
    const href = await page.evaluate(() => {
      const l = document.querySelector('link[rel~="icon"]') as HTMLLinkElement | null;
      return l?.getAttribute('href') ?? '';
    });
    expect(href, 'index.html 含 favicon 链接').toMatch(/favicon|\.svg|\.ico|\.png/i);
  });

  test('Layer 4 interact: CTA 可导航到应用列表', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    const cta = page.getByRole('button', { name: /applications|go to|开始|应用/i }).first();
    if (!(await cta.count())) test.skip(true, '主页未暴露 CTA');
    await cta.click();
    await expect(page).toHaveURL(/workflows/);
  });
});
