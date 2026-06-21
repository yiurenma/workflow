# Test Doc v18.0 — Canvas Mobile Header: Save button inaccessible (overflow)

**Label:** `TODO-canvas-mobile-header-save-overflow`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Suite | Description | Viewport | Expected | Pass Criterion |
|-------|-------|-------------|----------|----------|----------------|
| TC-CANVAS-MOB-06 | `canvas-mobile.spec.ts` (extend) | Save button visible on mobile canvas | 390×844 Mobile | Button with text "Save" visible | PASS |
| TC-CANVAS-MOB-07 | `canvas-mobile.spec.ts` (extend) | `⋯` overflow menu visible on mobile | 390×844 Mobile | Dropdown trigger button visible | PASS |
| TC-CANVAS-MOB-08 | `canvas-mobile.spec.ts` (extend) | Overflow menu contains Explain, JsonPath, Run | 390×844 Mobile | All 3 menu items visible on click | PASS |
| TC-CANVAS-02 | `canvas.spec.ts` (existing) | Explain button visible (Desktop) | Desktop | Explain button visible | PASS (regression check) |
| TC-JSONPATH-01 | `canvas.spec.ts` (existing) | JsonPath button visible (Desktop) | Desktop | JsonPath button visible | PASS (regression check) |

---

## New TC Implementations (add to `canvas-mobile.spec.ts`)

```ts
test('TC-CANVAS-MOB-06 Save button visible on mobile canvas', async ({ page }) => {
  const width = page.viewportSize()?.width ?? 1280;
  if (width >= 768) test.skip();
  await expect(page.getByRole('button', { name: /save/i })).toBeVisible({ timeout: 5000 });
});

test('TC-CANVAS-MOB-07 overflow menu trigger visible on mobile', async ({ page }) => {
  const width = page.viewportSize()?.width ?? 1280;
  if (width >= 768) test.skip();
  // The "..." button should be visible
  const moreBtn = page.locator('button').filter({ has: page.locator('[aria-label="ellipsis"]') }).first();
  await expect(moreBtn).toBeVisible({ timeout: 5000 });
});

test('TC-CANVAS-MOB-08 overflow menu contains secondary actions', async ({ page }) => {
  const width = page.viewportSize()?.width ?? 1280;
  if (width >= 768) test.skip();
  const moreBtn = page.locator('button').filter({ has: page.locator('[aria-label="ellipsis"]') }).first();
  await moreBtn.tap();
  await expect(page.locator('.ant-dropdown').getByText('Explain')).toBeVisible({ timeout: 3000 });
  await expect(page.locator('.ant-dropdown').getByText('JsonPath')).toBeVisible({ timeout: 3000 });
  await expect(page.locator('.ant-dropdown').getByText('Run')).toBeVisible({ timeout: 3000 });
});
```

---

## Regression Gap Identified

The existing test suite had **no assertion that Save is visible on mobile**. TC-CANVAS-MOB-06 fills this gap. Going forward, any new toolbar button added to `WorkflowHeader` must be paired with a mobile visibility test.
