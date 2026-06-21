# Test Doc v22.0 — Mobile: Node Editor Blank Space Below Content

**Label:** `TODO-mobile-node-editor-blank-space-below`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Viewport | Description | Steps | Expected |
|-------|----------|-------------|-------|----------|
| TC-DRAWER-01 | Mobile (390×844) | Drawer opens as bottom sheet | Tap a node on canvas | Drawer slides up from bottom; no right-side drawer |
| TC-DRAWER-02 | Mobile (390×844) | Drawer content visible without scrolling | Open drawer | Description / Rules / Action fields visible without scrolling |
| TC-DRAWER-03 | Mobile (390×844) | Blank space below content ≤ normal padding | Open drawer | No large blank region (> 40 px) below the last field |
| TC-DRAWER-04 | Mobile (390×844) | Drawer scrollable when content is tall | Open a node with many rules | Body scrolls; content not clipped |
| TC-DRAWER-05 | Mobile (390×844) | Drawer does not cover full screen | Open drawer | At least 15% of canvas visible above drawer |
| TC-DRAWER-06 | Desktop (1280×800) | Desktop drawer unchanged (right-side) | Click a node | Drawer opens on the right, 420 px wide |

---

## E2E Automation (Playwright)

Add to `canvas-mobile.spec.ts`:

```ts
test('TC-DRAWER-01 node editor opens as bottom sheet on mobile', async ({ page }) => {
  const width = page.viewportSize()?.width ?? 1280;
  if (width >= 768) test.skip();
  // Click a node
  await page.locator('.react-flow__node').first().click();
  const drawer = page.locator('.ant-drawer-bottom');
  await expect(drawer).toBeVisible({ timeout: 5000 });
});

test('TC-DRAWER-02 drawer content visible without scroll', async ({ page }) => {
  const width = page.viewportSize()?.width ?? 1280;
  if (width >= 768) test.skip();
  await page.locator('.react-flow__node').first().click();
  await expect(page.locator('.ant-drawer-body').getByText(/description/i).first()).toBeVisible({ timeout: 5000 });
});
```
