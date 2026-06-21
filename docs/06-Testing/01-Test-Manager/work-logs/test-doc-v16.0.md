# Test Doc v16.0 — Canvas: JsonPath Playground Modal

**Label:** `TODO-canvas-json-path-verification-modal`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Suite | Description | Viewport | Expected | Pass Criterion |
|-------|-------|-------------|----------|----------|----------------|
| TC-JSONPATH-01 | `canvas.spec.ts` (extend) | JsonPath button visible in toolbar | Desktop | Button with text "JsonPath" visible | PASS |
| TC-JSONPATH-02 | `canvas.spec.ts` (extend) | Clicking JsonPath opens modal | Desktop | Modal with title "JsonPath Playground" visible | PASS |
| TC-JSONPATH-03 | `canvas.spec.ts` (extend) | Valid expression + valid JSON returns match | Desktop | Result `pre` contains matched value | PASS |
| TC-JSONPATH-04 | `canvas.spec.ts` (extend) | Invalid JSON shows error | Desktop | Error text starting with "Invalid JSON:" visible | PASS |
| TC-JSONPATH-05 | `canvas.spec.ts` (extend) | No-match expression shows "(no match)" | Desktop | Text "(no match)" visible | PASS |

---

## TC Implementations

```ts
test.describe('JsonPath Playground (TC-JSONPATH)', () => {
  test('TC-JSONPATH-01 JsonPath button visible', async ({ page }) => {
    await page.waitForSelector('.react-flow, [data-testid="rf__wrapper"]', { timeout: 15_000 });
    await expect(page.getByRole('button', { name: /jsonpath/i })).toBeVisible({ timeout: 5000 });
  });

  test('TC-JSONPATH-02 modal opens', async ({ page }) => {
    await page.waitForSelector('.react-flow, [data-testid="rf__wrapper"]', { timeout: 15_000 });
    await page.getByRole('button', { name: /jsonpath/i }).click();
    await expect(page.locator('.ant-modal').filter({ hasText: 'JsonPath Playground' })).toBeVisible({ timeout: 5000 });
  });

  test('TC-JSONPATH-03 valid expression returns result', async ({ page }) => {
    await page.waitForSelector('.react-flow, [data-testid="rf__wrapper"]', { timeout: 15_000 });
    await page.getByRole('button', { name: /jsonpath/i }).click();
    await page.locator('.ant-modal').getByPlaceholder('$.customer.id').fill('$.customer.id');
    await page.locator('.ant-modal').locator('textarea').fill('{"customer":{"id":"C001"}}');
    await page.locator('.ant-modal').getByRole('button', { name: 'Validate' }).click();
    await expect(page.locator('.ant-modal pre')).toContainText('C001', { timeout: 3000 });
  });

  test('TC-JSONPATH-04 invalid JSON shows error', async ({ page }) => {
    await page.waitForSelector('.react-flow, [data-testid="rf__wrapper"]', { timeout: 15_000 });
    await page.getByRole('button', { name: /jsonpath/i }).click();
    await page.locator('.ant-modal').locator('textarea').fill('{ invalid json');
    await page.locator('.ant-modal').getByRole('button', { name: 'Validate' }).click();
    await expect(page.locator('.ant-modal').getByText(/Invalid JSON:/i)).toBeVisible({ timeout: 3000 });
  });

  test('TC-JSONPATH-05 no-match expression shows "(no match)"', async ({ page }) => {
    await page.waitForSelector('.react-flow, [data-testid="rf__wrapper"]', { timeout: 15_000 });
    await page.getByRole('button', { name: /jsonpath/i }).click();
    await page.locator('.ant-modal').getByPlaceholder('$.customer.id').fill('$.nonexistent.field');
    await page.locator('.ant-modal').locator('textarea').fill('{"customer":{"id":"C001"}}');
    await page.locator('.ant-modal').getByRole('button', { name: 'Validate' }).click();
    await expect(page.locator('.ant-modal').getByText('(no match)')).toBeVisible({ timeout: 3000 });
  });
});
```

---

## Regression Check

- TC-CANVAS-01, TC-CANVAS-02, TC-CANVAS-03 must pass unchanged.
- Existing Explain / Run / Save buttons must remain visible alongside new JsonPath button.
