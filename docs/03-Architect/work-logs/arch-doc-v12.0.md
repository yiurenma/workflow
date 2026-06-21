# Arch Doc v12.0 — E2E Playwright Scaffold

**Status:** Draft  
**Label:** `TODO-e2e-playwright-pass1-author-full-run`  
**Date:** 2026-04-07

---

## 1. Technology Decision

| Choice | Rationale |
|--------|-----------|
| **`@playwright/test`** | Native test runner + assertions; no separate Jest/Vitest config needed; built-in mobile device emulation |
| **Chromium only (pass 1)** | Fast feedback; cover Firefox/WebKit in a future pass |
| **UAT URL as base** | No need to build locally; tests the deployed artefact agents have been validating |
| **`e2e/` directory** | Separate from `src/` — no Vite bundling confusion; clear ownership |

---

## 2. Directory Layout

```
workflow-ui/
  e2e/
    navigation.spec.ts        # page load, route resolution
    applications-desktop.spec.ts
    applications-mobile.spec.ts
    records.spec.ts
    canvas.spec.ts
    node-editor.spec.ts
    explain.spec.ts
  playwright.config.ts
  package.json                # add test:e2e script + devDependency
```

---

## 3. `playwright.config.ts` Shape

```ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: 1,
  reporter: [['list'], ['html', { outputFolder: 'playwright-report', open: 'never' }]],
  use: {
    baseURL: 'https://workflow-ui-gamma.vercel.app',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'Desktop Chrome', use: { ...devices['Desktop Chrome'] } },
    { name: 'Mobile Safari', use: { ...devices['iPhone 12'] } },
  ],
});
```

---

## 4. Test-Case Architecture

Each spec file follows:
```ts
import { test, expect } from '@playwright/test';

test.describe('Feature Area', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); });

  test('TC description', async ({ page }) => {
    // arrange → act → assert
  });
});
```

**Selector strategy (priority order):**
1. `getByRole` (accessibility role + name)
2. `getByText` / `getByLabel`
3. `locator('[data-testid="..."]')` — add `data-testid` attributes to key elements if needed
4. CSS class selectors as last resort (fragile)

---

## 5. Key Challenges & Mitigations

| Challenge | Mitigation |
|-----------|-----------|
| Auth-gated routes | Check if UAT is open; document as blocker in report if login required |
| Dynamic data (application names) | Use `page.locator('table tbody tr').first()` patterns; don't hardcode names |
| Canvas interactions (pan/zoom) | Use `page.mouse.move/down/up` sequences; assert canvas element exists + is visible |
| Mobile viewport tests | Use Playwright `devices['iPhone 12']` project — emulates touch + viewport |
| Flaky timing | Use `await expect(locator).toBeVisible()` with built-in retry over arbitrary `waitForTimeout` |

---

## 6. Package Changes

```json
// package.json additions
"scripts": {
  "test:e2e": "playwright test"
},
"devDependencies": {
  "@playwright/test": "^1.52.0"
}
```

Install command: `npm install --save-dev @playwright/test && npx playwright install chromium`

---

## 7. CI / Future Integration

Pass 1 is a manual run only. A future CI item can add a GitHub Actions workflow that runs `npm run test:e2e` on PR. This is out of scope for pass 1.

---

## 8. Risk

| Risk | Impact | Mitigation |
|------|--------|-----------|
| UAT env login wall | Blocks most tests | Document failures; note auth requirement in report |
| Canvas spec flakiness | False failures | Mark canvas tests with `test.slow()`; increase timeout |
| E2E adds devDep weight | Minimal (dev-only) | `@playwright/test` is devDependency; not bundled |
