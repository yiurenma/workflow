import { expect, type Locator, type Page } from '@playwright/test';

/**
 * 可用性断言库 —— 把"操作便捷性/合理性"变成可执行断言。
 * 每个断言标注其映射的 ISO/IEC 25010 可用性子特性 + Nielsen 启发式。
 * 选择器一律用可访问语义（role/label/text），不依赖内部 class/testid。
 */

/** ISO25010 响应式：内容无横向裁切（视口外溢出 ≤ 2px）。 */
export async function assertNoHorizontalClip(page: Page) {
  const overflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(overflow, '无横向裁切').toBeLessThanOrEqual(2);
}

/** ISO25010 Operability / Nielsen 触控目标：可点元素 ≥ 44×44 px（WCAG 2.5.5 / 2.5.8）。 */
export async function assertTouchTargetSize(locator: Locator, min = 44) {
  await expect(locator).toBeVisible();
  const box = await locator.boundingBox();
  expect(box, '元素有尺寸').toBeTruthy();
  expect(Math.min(box!.width, box!.height), `触控目标 ≥${min}px`).toBeGreaterThanOrEqual(min);
}

/** ISO25010 Operability：主操作首屏可见（无需滚动即在视口内）。 */
export async function assertVisibleWithoutScroll(locator: Locator, page: Page) {
  await expect(locator).toBeVisible();
  const box = await locator.boundingBox();
  const vp = page.viewportSize();
  expect(box && vp && box.y >= 0 && box.y < vp.height, '首屏可见、无需滚动').toBeTruthy();
}

/**
 * ISO25010 User control & freedom / Nielsen #3：弹窗有退路（底线 @gate）。
 * 验证存在显式关闭控件（× / Cancel / 关闭）且能关闭弹窗。
 * open: 每次重新打开弹窗的动作；modal: 弹窗定位器。
 */
export async function assertClosable(page: Page, open: () => Promise<void>, modal: Locator) {
  await open();
  await expect(modal).toBeVisible();
  const closeBtn = modal.getByRole('button', { name: /close|×|✕|关闭|cancel|取消/i }).first();
  expect(await closeBtn.count(), '弹窗应有显式关闭控件（× / Cancel）').toBeGreaterThan(0);
  await closeBtn.click();
  await expect(modal, '关闭控件应关闭弹窗').toBeHidden();
}

/**
 * WAI-ARIA 对话框模式 / Nielsen #3（理想 @advisory）：模态支持 Esc 关闭。
 * 当前产品多数模态未实现，调用方可用 test.fixme 标记为已知差距。
 */
export async function assertEscapeCloses(page: Page, open: () => Promise<void>, modal: Locator) {
  await open();
  await expect(modal).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(modal, 'Esc 应关闭模态（WAI-ARIA）').toBeHidden();
}

/**
 * ISO25010 User error protection / Nielsen #5：非法输入时主操作被禁用（防错）。
 * fillInvalid: 填入非法值的动作；submit: 提交按钮定位器。
 */
export async function assertSubmitDisabledWhenInvalid(fillInvalid: () => Promise<void>, submit: Locator) {
  await fillInvalid();
  await expect(submit, '非法输入时提交按钮应禁用').toBeDisabled();
}

/**
 * ISO25010 Visibility of system status / Nielsen #1：操作后及时反馈（无静默失败）。
 * action: 触发动作；feedback: 期望出现的反馈定位器；ms: 预算。
 */
export async function assertFeedbackWithin(action: () => Promise<void>, feedback: Locator, ms = 2000) {
  const t0 = Date.now();
  await action();
  await expect(feedback, `应在 ${ms}ms 内给出反馈`).toBeVisible({ timeout: ms });
  return Date.now() - t0;
}

/**
 * ISO25010 Appropriateness recognizability：空状态有引导文案/下一步入口。
 * container: 空状态容器；至少包含一段说明文本或一个可点入口。
 */
export async function assertEmptyStateGuides(container: Locator) {
  await expect(container).toBeVisible();
  const text = (await container.innerText()).trim();
  const hasGuidance = text.length > 0;
  const hasAction = (await container.getByRole('button').count()) > 0 ||
    (await container.getByRole('link').count()) > 0;
  expect(hasGuidance || hasAction, '空状态应有引导文案或下一步入口').toBeTruthy();
}

/**
 * ISO25010 Learnability / Operability：关键任务在 N 步内可达。
 * steps: 依次执行的步骤数组；target: 完成后应可见的定位器。
 * 步数即 steps.length，由调用方按"应在几步内完成"组织。
 */
export async function assertReachableWithinSteps(steps: Array<() => Promise<void>>, target: Locator, maxSteps: number) {
  expect(steps.length, `任务应在 ≤${maxSteps} 步内`).toBeLessThanOrEqual(maxSteps);
  for (const s of steps) await s();
  await expect(target, '按既定步骤后目标可达').toBeVisible();
}
