# Test Doc v23.0 — Canvas AI: Workflow Generator

**Label:** `TODO-canvas-ai-workflow-json-copilot-replace`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Description | Steps | Expected |
|-------|-------------|-------|----------|
| TC-GEN-01 | Happy path — valid minimal WorkFlow applies after confirm | Enter requirements → Generate → Confirm | Canvas replaced with generated nodes; no crash |
| TC-GEN-02 | Invalid JSON response — error shown, canvas unchanged | Mock API returns `{ not json }` | Error message shown; canvas unchanged |
| TC-GEN-03 | Truncated JSON — error shown, no apply | Mock returns partial JSON string | Error "incomplete or empty"; canvas unchanged |
| TC-GEN-04 | Valid JSON, wrong shape (missing `uiMap.id`) | Mock returns `{"pluginList":[{"id":1}]}` | Validation error; canvas unchanged |
| TC-GEN-05 | Model wraps output in markdown fences | Mock returns ` ```json\n{...}\n``` ` | Fences stripped; JSON parsed; applies if valid |
| TC-GEN-06 | API error (5xx) | Mock returns 500 | "API error" message; canvas unchanged |
| TC-GEN-07 | User cancels confirm dialog | Generate → get preview → Cancel | Canvas unchanged |
| TC-GEN-08 | Generate button visible in toolbar (desktop) | Navigate to canvas | Button "Generate" visible |
| TC-GEN-09 | Generate accessible from overflow menu (mobile) | Mobile canvas | "Generate" in ⋯ dropdown menu |

---

## E2E Automation (Playwright)

Add to `canvas.spec.ts` describe block `TC-GEN`:

```ts
test('TC-GEN-08 Generate button visible on desktop', async ({ page }) => {
  const isMobile = (page.viewportSize()?.width ?? 1280) < 768;
  if (isMobile) test.skip();
  await page.waitForSelector('.react-flow', { timeout: 15_000 });
  await expect(page.getByRole('button', { name: /generate/i })).toBeVisible({ timeout: 5000 });
});

test('TC-GEN-09 Generate in overflow menu on mobile', async ({ page }) => {
  const isMobile = (page.viewportSize()?.width ?? 1280) < 768;
  if (!isMobile) test.skip();
  await page.getByRole('button', { name: /more actions/i }).click();
  await expect(page.locator('.ant-dropdown-menu').getByText('Generate')).toBeVisible({ timeout: 3000 });
});
```

TC-GEN-01..07 require mocking `callAI`; implement as unit tests or Playwright with route-level API mocking.
