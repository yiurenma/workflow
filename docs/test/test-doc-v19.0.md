# Test Doc v19.0 — Canvas: Straight-Line Layout Recovery

**Label:** `TODO-canvas-straight-line-workflow-recovery`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Description | Viewport | Steps | Expected |
|-------|-------------|----------|-------|----------|
| TC-LAYOUT-01 | Straighten button visible on canvas | Desktop | Navigate to canvas | "Straighten" / ⇕ button visible in React Flow panel |
| TC-LAYOUT-02 | Click Straighten repositions nodes to vertical line | Desktop | Have ≥ 2 nodes at random positions; click Straighten | All nodes aligned to same X; Y positions evenly spaced; `fitView` called |
| TC-LAYOUT-03 | Relative top-to-bottom order preserved | Desktop | Place node A above node B; click Straighten | A remains above B in the new layout |
| TC-LAYOUT-04 | Straighten works on mobile | Mobile (390×844) | Navigate to canvas; click Straighten | Same repositioning as desktop |
| TC-LAYOUT-05 | Edges remain intact after Straighten | Desktop | Canvas with edges; click Straighten | Edge connections unchanged (source/target node IDs same) |
| TC-LAYOUT-06 | Straighten on empty canvas is no-op | Desktop | Open canvas with 0 nodes; click Straighten | No error; canvas remains empty |

---

## E2E Automation (Playwright)

Add to `canvas.spec.ts` describe block `TC-LAYOUT`:

```ts
test('TC-LAYOUT-01 Straighten button visible', async ({ page }) => {
  await page.waitForSelector('.react-flow', { timeout: 15_000 });
  await expect(page.getByRole('button', { name: /straighten/i })).toBeVisible({ timeout: 5000 });
});

test('TC-LAYOUT-02 clicking Straighten does not crash', async ({ page }) => {
  await page.waitForSelector('.react-flow', { timeout: 15_000 });
  const errors: string[] = [];
  page.on('pageerror', e => errors.push(e.message));
  await page.getByRole('button', { name: /straighten/i }).click();
  await page.waitForTimeout(600);
  expect(errors.filter(e => !e.includes('ResizeObserver'))).toHaveLength(0);
});
```
