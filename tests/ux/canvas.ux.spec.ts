import { test } from '@playwright/test';
import { assertNoHorizontalClip, assertClosable, assertEscapeCloses } from '../lib/ux';

/** 可用性样板 — 画布。@gate 为客观硬约束。 */
const CANVAS_APP = process.env.CANVAS_APP ?? 'DEMO_APP';

test.describe('画布 可用性', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(`/workflows/${CANVAS_APP}`);
    await page.waitForLoadState('networkidle');
  });

  test('@gate 响应式：画布页无横向裁切', async ({ page }) => {
    await assertNoHorizontalClip(page);
  });

  test('@gate User control & freedom：导入弹窗有显式关闭控件（× / Cancel）', async ({ page }) => {
    const importBtn = page.getByRole('button', { name: /import/i }).first();
    test.skip(!(await importBtn.count()), '当前视口未直接暴露 Import（移动溢出菜单）');
    const open = async () => page.getByRole('button', { name: /import/i }).first().click();
    const modal = page.locator('.modal-box, [role="dialog"]').first();
    await assertClosable(page, open, modal);
  });

  test('@advisory WAI-ARIA：导入弹窗应支持 Esc 关闭', async ({ page }) => {
    test.fixme(true, '已知差距：模态未实现 Esc 关闭（见 TODO-ui-modal-esc-close-wai-aria）');
    const open = async () => page.getByRole('button', { name: /import/i }).first().click();
    const modal = page.locator('.modal-box, [role="dialog"]').first();
    await assertEscapeCloses(page, open, modal);
  });
});
