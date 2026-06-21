# Test Cases — Workflow Studio Unified Program Part 2: IBM Carbon Audit (v32.0)

**Document version:** 32.0 Part 2  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** IBM Carbon residual styling audit + Playwright blind spots  
**Test environment:** UAT (https://workflow-ui-gamma.vercel.app)

---

## 1. Test Strategy

### 1.1 Scope

- **Audit:** Manual inspection of all user-facing surfaces for Carbon compliance
- **Remediation:** Manual + Playwright validation after fixes applied

### 1.2 Test Levels

- **Unit:** Not applicable (styling defect)
- **Integration:** Not applicable
- **E2E:** Playwright tests with Layer 5 (computed styles) assertions

### 1.3 Test Data

- **Applications:** Existing test apps on UAT
- **Records:** Existing execution records on UAT

---

## 2. Test Cases

### 2.1 Audit Test Cases

#### TC-CARBON-AUDIT-01: Applications Delete confirm Carbon compliance

**Priority:** HIGH  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows`
- At least one application in list

**Steps:**
1. Click/tap ⋯ on application row/card
2. Click/tap "Delete"
3. Inspect Delete confirm dialog with DevTools

**Expected Results:**
- **Border-radius:** `0px` (Carbon sharp corners)
- **Background color:** `var(--cds-layer-01)` or `#ffffff`
- **Primary button:** `background-color: #da1e28` (Red 60 for danger action)
- **Font family:** `IBM Plex Sans`
- **Modal header:** `border-bottom: 1px solid var(--cds-border-subtle-01)`

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Imperative Modal.confirm / Portal without Carbon class / Residual Tailwind / etc.]

---

#### TC-CARBON-AUDIT-02: Settings modal Carbon compliance

**Priority:** HIGH  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows`
- At least one application in list

**Steps:**
1. Click/tap ⋯ on application row/card
2. Click/tap "Settings"
3. Inspect Settings modal with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Background color:** `var(--cds-layer-01)`
- **Header font:** `IBM Plex Sans`
- **Input fields:** Carbon text input styles (border, focus state)
- **Save button:** `background-color: #0f62fe` (Blue 60)

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

#### TC-CARBON-AUDIT-03: History modal Carbon compliance

**Priority:** MEDIUM  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows`
- At least one application with history

**Steps:**
1. Click/tap ⋯ on application row/card
2. Click/tap "History"
3. Inspect History modal with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Background color:** `var(--cds-layer-01)`
- **List items:** Carbon list styles
- **Rollback button:** Carbon secondary button style

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

#### TC-CARBON-AUDIT-04: JsonPath modal Carbon compliance

**Priority:** HIGH  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`

**Steps:**
1. Click "JsonPath" button in toolbar
2. Inspect JsonPath modal with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Background color:** `var(--cds-layer-01)`
- **Input fields:** Carbon text input / textarea styles
- **Validate button:** `background-color: #0f62fe` (Blue 60)
- **Success/error messages:** Carbon notification colors

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

#### TC-CARBON-AUDIT-05: Explain modal Carbon compliance

**Priority:** HIGH  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`

**Steps:**
1. Click "Explain" button in toolbar
2. Inspect Explain modal with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Background color:** `var(--cds-layer-01)`
- **Textarea:** Carbon textarea styles
- **Explain button:** `background-color: #0f62fe` (Blue 60)
- **Response area:** Carbon code block or text styles

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

#### TC-CARBON-AUDIT-06: Generate modal Carbon compliance

**Priority:** HIGH  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`

**Steps:**
1. Click "Generate" button in toolbar
2. Inspect Generate modal with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Background color:** `var(--cds-layer-01)`
- **Textarea:** Carbon textarea styles
- **Generate button:** `background-color: #0f62fe` (Blue 60)

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

#### TC-CARBON-AUDIT-07: Application overflow menu Carbon compliance

**Priority:** MEDIUM  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows`

**Steps:**
1. Click/tap ⋯ on application row/card
2. Inspect dropdown menu with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Background color:** `var(--cds-layer-01)`
- **Menu items:** `color: var(--cds-text-primary)`
- **Hover state:** `background-color: var(--cds-layer-hover-01)`

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

#### TC-CARBON-AUDIT-08: Toast notifications Carbon compliance

**Priority:** LOW  
**Type:** Manual inspection  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Trigger success/error toast (e.g., save application, delete application)

**Steps:**
1. Perform action that triggers toast
2. Inspect toast with DevTools

**Expected Results:**
- **Border-radius:** `0px`
- **Success toast:** `background-color: #24a148` (Green 50)
- **Error toast:** `background-color: #da1e28` (Red 60)
- **Font family:** `IBM Plex Sans`

**Actual Results:** [To be filled during audit]

**Severity:** [HIGH / MEDIUM / LOW]

**Root Cause:** [Category]

---

### 2.2 Remediation Validation Test Cases

#### TC-CARBON-FIX-01: Delete confirm Carbon compliance (post-fix)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Fixes applied for TC-CARBON-AUDIT-01

**Steps:**
1. Navigate to `https://workflow-ui-gamma.vercel.app/workflows`
2. Click/tap ⋯ on application
3. Click/tap "Delete"
4. Validate Layer 5 (computed styles)

**Expected Results:**

**Layer 5 (Effect):**
```typescript
const modal = page.locator('.ant-modal');
await expect(modal).toBeVisible();

// Border-radius
const borderRadius = await modal.evaluate(el => getComputedStyle(el).borderRadius);
expect(borderRadius).toBe('0px');

// Background color
const bgColor = await modal.locator('.ant-modal-content').evaluate(
  el => getComputedStyle(el).backgroundColor
);
expect(bgColor).toMatch(/rgb\(255, 255, 255\)|var\(--cds-layer-01\)/);

// Danger button
const deleteBtn = modal.locator('button.ant-btn-dangerous');
const btnBgColor = await deleteBtn.evaluate(el => getComputedStyle(el).backgroundColor);
expect(btnBgColor).toBe('rgb(218, 30, 40)'); // Red 60 #da1e28
```

---

#### TC-CARBON-FIX-02: Settings modal Carbon compliance (post-fix)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Fixes applied for TC-CARBON-AUDIT-02

**Steps:**
1. Navigate to `https://workflow-ui-gamma.vercel.app/workflows`
2. Click/tap ⋯ on application
3. Click/tap "Settings"
4. Validate Layer 5 (computed styles)

**Expected Results:**

**Layer 5 (Effect):**
```typescript
const modal = page.locator('.ant-modal');
await expect(modal).toBeVisible();

// Border-radius
const borderRadius = await modal.evaluate(el => getComputedStyle(el).borderRadius);
expect(borderRadius).toBe('0px');

// Font family
const fontFamily = await modal.locator('.ant-modal-title').evaluate(
  el => getComputedStyle(el).fontFamily
);
expect(fontFamily).toContain('IBM Plex Sans');

// Primary button
const saveBtn = modal.locator('button.ant-btn-primary');
const btnBgColor = await saveBtn.evaluate(el => getComputedStyle(el).backgroundColor);
expect(btnBgColor).toBe('rgb(15, 98, 254)'); // Blue 60 #0f62fe
```

---

#### TC-CARBON-FIX-03: JsonPath modal Carbon compliance (post-fix)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Fixes applied for TC-CARBON-AUDIT-04

**Steps:**
1. Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
2. Click "JsonPath" button
3. Validate Layer 5 (computed styles)

**Expected Results:**

**Layer 5 (Effect):**
```typescript
const modal = page.locator('.jsonpath-modal');
await expect(modal).toBeVisible();

// Border-radius
const borderRadius = await modal.evaluate(el => getComputedStyle(el).borderRadius);
expect(borderRadius).toBe('0px');

// Validate button
const validateBtn = modal.locator('button:has-text("Validate")');
const btnBgColor = await validateBtn.evaluate(el => getComputedStyle(el).backgroundColor);
expect(btnBgColor).toBe('rgb(15, 98, 254)'); // Blue 60 #0f62fe
```

---

### 2.3 Regression Tests

#### TC-CARBON-REGRESSION-01: Existing E2E suite still passes

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- All Carbon fixes merged

**Steps:**
1. Run full Playwright suite: `npx playwright test`
2. Review results

**Expected Results:**
- Pass rate ≥82/84 (baseline from v28.0)
- No new failures introduced by Carbon fixes

---

## 3. Test Execution Plan

### 3.1 Phase 1: Audit

1. Execute TC-CARBON-AUDIT-01 through TC-CARBON-AUDIT-08
2. Document actual results, severity, root cause
3. Produce gap list in `docs/carbon-audit-gaps-v32.0.md`

### 3.2 Phase 2: Remediation

1. Fix HIGH severity gaps first
2. Fix MEDIUM/LOW severity gaps
3. Execute TC-CARBON-FIX-01 through TC-CARBON-FIX-03
4. Run full E2E suite (TC-CARBON-REGRESSION-01)

### 3.3 Phase 3: Documentation

1. Document results in `ui-test-report-v32.0-part2.md`
2. List which v28-era tests lacked Carbon assertions
3. Document Carbon validation coverage

---

## 4. Exit Criteria

- Gap list produced with ≥10 identified non-Carbon surfaces
- All HIGH severity gaps fixed and validated
- TC-CARBON-FIX-01/02/03 pass on Desktop + Mobile
- Full E2E suite pass rate ≥82/84 (no regressions)
- `ui-test-report-v32.0-part2.md` documents coverage

---

## 5. References

- IBM Carbon Design System: https://carbondesignsystem.com/
- `arch-doc-v28.0.md` — v28 IBM Carbon refactor baseline
- `test-doc-v28.0.md` — v28 E2E baseline
