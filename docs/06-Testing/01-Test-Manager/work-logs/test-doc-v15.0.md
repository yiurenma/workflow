# Test Doc v15.0 — Canvas Mobile: Draggable Add-Node FAB

**Label:** `TODO-mobile-canvas-add-node-fab-draggable`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Suite | Description | Viewport | Expected | Pass Criterion |
|-------|-------|-------------|----------|----------|----------------|
| TC-CANVAS-MOB-01 | `canvas-mobile.spec.ts` (new) | + FAB visible on mobile canvas | 390×844 Mobile | Button with `aria-label="Add node"` visible | PASS |
| TC-CANVAS-MOB-02 | `canvas-mobile.spec.ts` (new) | Tap FAB opens Add Node sheet | 390×844 Mobile | Ant Design Drawer visible after tap | PASS |
| TC-CANVAS-MOB-03 | `canvas-mobile.spec.ts` (new) | Drag FAB to new position (pointer simulation) | 390×844 Mobile | Button moves; sheet does not open | PASS |
| TC-CANVAS-MOB-04 | `canvas-mobile.spec.ts` (new) | Position persists in localStorage | 390×844 Mobile | `workflow_canvas_fab_pos` key set in localStorage after drag | PASS |
| TC-CANVAS-MOB-05 | `canvas-mobile.spec.ts` (new) | FAB not rendered on desktop | 1280×800 Desktop | No element with `aria-label="Add node"` visible | PASS |

---

## New Spec: `e2e/canvas-mobile.spec.ts`

```ts
import { test, expect } from '@playwright/test';
import { setupMocks } from './mocks';

test.describe('Canvas — Mobile Add-Node FAB (TC-CANVAS-MOB)', () => {
  test.beforeEach(async ({ page }) => {
    await setupMocks(page);
    await page.goto('/workflows/test-app-01');
    await page.waitForLoadState('load');
    await page.waitForSelector('.react-flow, [data-testid="rf__wrapper"]', { timeout: 15_000 });
  });

  test('TC-CANVAS-MOB-01 FAB visible on mobile canvas', async ({ page }) => {
    const width = page.viewportSize()?.width ?? 1280;
    if (width >= 768) test.skip();
    const fab = page.getByRole('button', { name: 'Add node' });
    await expect(fab).toBeVisible({ timeout: 5000 });
  });

  test('TC-CANVAS-MOB-02 tap FAB opens Add Node sheet', async ({ page }) => {
    const width = page.viewportSize()?.width ?? 1280;
    if (width >= 768) test.skip();
    const fab = page.getByRole('button', { name: 'Add node' });
    await fab.tap();
    await expect(page.locator('.ant-drawer')).toBeVisible({ timeout: 5000 });
  });

  test('TC-CANVAS-MOB-03 drag FAB does not open sheet', async ({ page }) => {
    const width = page.viewportSize()?.width ?? 1280;
    if (width >= 768) test.skip();
    const fab = page.getByRole('button', { name: 'Add node' });
    const box = await fab.boundingBox();
    if (!box) throw new Error('FAB not found');
    // Simulate drag > 5px
    await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
    await page.mouse.down();
    await page.mouse.move(box.x + box.width / 2 + 80, box.y + box.height / 2 + 20, { steps: 10 });
    await page.mouse.up();
    // Sheet should NOT open
    const drawerVisible = await page.locator('.ant-drawer-open').first()
      .waitFor({ state: 'visible', timeout: 1000 }).then(() => true).catch(() => false);
    expect(drawerVisible).toBe(false);
  });

  test('TC-CANVAS-MOB-04 position persisted in localStorage after drag', async ({ page }) => {
    const width = page.viewportSize()?.width ?? 1280;
    if (width >= 768) test.skip();
    const fab = page.getByRole('button', { name: 'Add node' });
    const box = await fab.boundingBox();
    if (!box) throw new Error('FAB not found');
    await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
    await page.mouse.down();
    await page.mouse.move(box.x + box.width / 2 + 80, box.y + box.height / 2 + 20, { steps: 10 });
    await page.mouse.up();
    const stored = await page.evaluate(() => localStorage.getItem('workflow_canvas_fab_pos'));
    expect(stored).not.toBeNull();
    const pos = JSON.parse(stored!);
    expect(typeof pos.x).toBe('number');
    expect(typeof pos.y).toBe('number');
  });

  test('TC-CANVAS-MOB-05 FAB not rendered on desktop', async ({ page }) => {
    const width = page.viewportSize()?.width ?? 1280;
    if (width < 768) test.skip();
    const fab = page.getByRole('button', { name: 'Add node' });
    await expect(fab).not.toBeVisible({ timeout: 3000 });
  });
});
```

---

## Playwright Project

Uses both `Desktop Chrome` (1280×800) and `Mobile Chrome` (390×844, `isMobile: true`, `hasTouch: true`) projects. TC-CANVAS-MOB-01..04 skip on Desktop; TC-CANVAS-MOB-05 skips on Mobile.
