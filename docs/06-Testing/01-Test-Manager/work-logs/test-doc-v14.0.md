# Test Doc v14.0 — Mobile Records: Ant Design Pagination on narrow viewport

**Label:** `TODO-mobile-records-pagination-invisible`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Suite | Description | Viewport | Expected | Pass Criterion |
|-------|-------|-------------|----------|----------|----------------|
| TC-REC-01 | `records.spec.ts` | Records page loads without crash | Both | Body non-empty, no Uncaught error | PASS |
| TC-REC-02 | `records.spec.ts` | Table or card view visible | Both | `.ant-spin-container` attached | PASS |
| TC-REC-03 | `records.spec.ts` | Pagination visible | Both (Desktop + Mobile) | `.ant-pagination` visible | PASS |

---

## TC-REC-03 Simplification

After this fix, `.ant-pagination` renders on both Desktop and Mobile viewports.
The dual-check (`hasPagination || hasMobilePrev`) can be simplified to:

```ts
test('TC-REC-03 pagination visible', async ({ page }) => {
  const pagination = page.locator('.ant-pagination').first();
  await expect(pagination).toBeVisible({ timeout: 8000 });
});
```

However, keeping the dual-check is also acceptable and does not cause regressions.
For simplicity, update to the single `.ant-pagination` check.

---

## Regression Check

- Desktop Records: `.ant-pagination` still rendered in desktop branch → TC-REC-03 Desktop passes.
- Mobile Records: `.ant-pagination` now rendered → TC-REC-03 Mobile passes.
- No other Records tests exist that test the custom Prev/Next buttons specifically.
