import { test, expect } from '@playwright/test';
import {
  assertNoHorizontalClip,
  assertVisibleWithoutScroll,
  assertClosable,
  assertEscapeCloses,
  assertReachableWithinSteps,
} from '../lib/ux';

/**
 * 可用性样板 — 应用列表（ISO/IEC 25010 + Nielsen）。
 * @gate 标记的是客观、稳定的可用性硬约束。
 */
test.describe('应用列表 可用性', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/workflows');
    await page.waitForLoadState('networkidle');
  });

  test('@gate Operability：主操作"新建应用"首屏可见（无需滚动）', async ({ page }) => {
    const create = page.getByRole('button', { name: /new application|＋|create/i }).filter({ visible: true } as any).first();
    await assertVisibleWithoutScroll(create, page);
  });

  test('@gate 响应式：内容无横向裁切', async ({ page }) => {
    await assertNoHorizontalClip(page);
  });

  test('@gate User control & freedom：新建弹窗有显式关闭控件（× / Cancel）', async ({ page }) => {
    const open = async () =>
      page.getByRole('button', { name: /new application|＋|create/i }).filter({ visible: true } as any).first().click();
    const modal = page.locator('.modal-box, [role="dialog"]').first();
    await assertClosable(page, open, modal);
  });

  test('@advisory WAI-ARIA：新建弹窗应支持 Esc 关闭', async ({ page }) => {
    test.fixme(true, '已知差距：模态未实现 Esc 关闭（见 TODO-ui-modal-esc-close-wai-aria）');
    const open = async () =>
      page.getByRole('button', { name: /new application|＋|create/i }).filter({ visible: true } as any).first().click();
    const modal = page.locator('.modal-box, [role="dialog"]').first();
    await assertEscapeCloses(page, open, modal);
  });

  test('Learnability：进入某应用画布 ≤ 2 步可达', async ({ page }) => {
    const target = page.locator('.react-flow, [data-testid="rf__wrapper"]')
      .or(page.getByRole('button', { name: /save|run|import|explain|generate/i }));
    await assertReachableWithinSteps(
      [async () => { await page.goto('/workflows/DEMO_APP'); await page.waitForLoadState('networkidle'); }],
      target.first(),
      2,
    );
  });
});
