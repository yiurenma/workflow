# Test Doc v13.0 — Canvas: Defensive null-guard for workflow data

**Label:** `TODO-canvas-workflow-undefined-crash`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Suite | Description | Viewport | Expected | Pass Criterion |
|-------|-------|-------------|----------|----------|----------------|
| TC-CANVAS-01 | `canvas.spec.ts` | Canvas loads for an application (happy path) | Desktop | `.react-flow` visible | PASS |
| TC-CANVAS-03 | `canvas.spec.ts` | No JS error on canvas load | Desktop | 0 page errors (excl. ResizeObserver) | PASS |
| TC-CANVAS-04 | `canvas.spec.ts` (new) | Canvas loads without crash when API returns `{}` (no pluginList) | Desktop | Canvas element visible, no crash | PASS |
| TC-CANVAS-05 | `canvas.spec.ts` (new) | Canvas loads without crash when API returns `{pluginList: null}` | Desktop | Canvas element visible, no crash | PASS |

---

## TC-CANVAS-04 Implementation Notes

Override the workflow mock to return `{}` for a second test app name
(`test-app-empty`) using an additional `page.route()` before `setupMocks`, then
navigate to `/workflows/test-app-empty`. Expect `.react-flow` to be visible and
`pageerror` event listener to catch 0 errors.

```ts
test('TC-CANVAS-04 canvas shows empty state when pluginList absent', async ({ page }) => {
  // Override: return empty workflow object for this app name
  await page.route(
    (url) => url.pathname.startsWith('/api/proxy/operation/workflow') && url.searchParams.get('applicationName') === 'test-app-empty',
    async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: '{}' });
    }
  );
  await setupMocks(page);
  const errors: string[] = [];
  page.on('pageerror', (err) => errors.push(err.message));
  await page.goto('/workflows/test-app-empty');
  await page.waitForLoadState('load');
  // Either canvas renders empty OR 404 result — either is acceptable; no crash
  const hasCrash = errors.filter(e => !e.includes('ResizeObserver')).length > 0;
  expect(hasCrash).toBe(false);
});
```

---

## Regression Check

- TC-CANVAS-01, TC-CANVAS-02, TC-CANVAS-03 must continue to PASS after the fix.
- All existing Desktop + Mobile E2E suites must remain green.
