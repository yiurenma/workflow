# Test Document — v35.0

**Document version:** 35.0  
**Date:** 2026-04-13  
**Status:** Draft  
**TODO item:** `TODO-ui-ibm-carbon-audit-residual-styling`  
**Related docs:** pm-doc-v35.0.md, arch-doc-v35.0.md

---

## 1. Test Scope

This test plan covers the audit and verification of IBM Carbon Design Language compliance across Workflow Studio UI, following the v28.0 baseline refactoring.

**In scope:**
- Manual audit of all UI surfaces for Carbon compliance
- Automated Playwright tests with Layer 5 (computed style) assertions
- Visual regression testing for critical modals
- Verification of imperative modal styling
- Verification of portal component styling (Dropdown, Tooltip, Popconfirm)

**Out of scope:**
- Functional behavior testing (covered by existing test suite)
- Performance testing
- Accessibility testing beyond Carbon baseline (covered in v24.0, v25.0)

---

## 2. Test Cases

### 2.1 Manual Audit Test Cases

#### TC-AUDIT-01: Application Delete Confirmation (Desktop)
**Priority:** P0  
**Preconditions:** At least one application exists in the list  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Click "Delete" button on any application row
3. Inspect the confirmation modal

**Expected Results:**
- Modal has 0px border-radius (rectangular)
- OK button background: Red 60 `#da1e28` (rgb(218, 30, 40))
- OK button height: 48px
- OK button border-radius: 0px
- Cancel button background: Gray 90 `#393939` (rgb(57, 57, 57))
- Cancel button height: 48px
- Modal title color: Gray 100 `#161616` (rgb(22, 22, 22))
- Modal title font: IBM Plex Sans
- Modal body color: Gray 70 `#525252` (rgb(82, 82, 82))
- Backdrop: `rgba(22, 22, 22, 0.5)`

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-02: Application Delete Confirmation (Mobile)
**Priority:** P0  
**Preconditions:** At least one application exists, viewport 390×844  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows on mobile viewport
2. Tap "⋯" on any application card
3. Tap "Delete"
4. Inspect the confirmation modal

**Expected Results:** Same as TC-AUDIT-01

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-03: Application Overflow Dropdown (Desktop)
**Priority:** P1  
**Preconditions:** At least one application exists  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Click "⋯" button on any application row
3. Inspect the dropdown menu

**Expected Results:**
- Dropdown background: Gray 10 `#f4f4f4` (rgb(244, 244, 244))
- Dropdown border: 1px solid Gray 30 `#c6c6c6` (rgb(198, 198, 198))
- Dropdown border-radius: 0px
- Dropdown shadow: `0 2px 6px rgba(0,0,0,0.3)`
- Menu item text: Gray 100 `#161616`, 14px, IBM Plex Sans
- Menu item hover background: Gray 20 `#e8e8e8` (rgb(232, 232, 232))

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-04: Application Overflow Dropdown (Mobile)
**Priority:** P1  
**Preconditions:** At least one application exists, viewport 390×844  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows on mobile viewport
2. Tap "⋯" on any application card
3. Inspect the dropdown menu

**Expected Results:** Same as TC-AUDIT-03

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-05: Canvas Toolbar Tooltips
**Priority:** P2  
**Preconditions:** Any workflow canvas open  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS
2. Hover over toolbar buttons (Run, Explain, Generate, JsonPath, etc.)
3. Inspect tooltip styling

**Expected Results:**
- Tooltip background: Gray 10 `#f4f4f4`
- Tooltip text: Gray 100 `#161616`, 14px, IBM Plex Sans
- Tooltip border: 1px solid Gray 30 `#c6c6c6`
- Tooltip border-radius: 0px
- Tooltip shadow: `0 2px 6px rgba(0,0,0,0.3)`

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-06: Settings Modal
**Priority:** P1  
**Preconditions:** At least one application exists  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Click "Settings" on any application row
3. Inspect the modal styling

**Expected Results:**
- Modal border-radius: 0px
- Modal header background: White `#ffffff`
- Modal header border-bottom: 1px solid Gray 20 `#e0e0e0`
- Modal title: Gray 100 `#161616`, 20px, IBM Plex Sans weight 400
- Primary button (Save): Blue 60 `#0f62fe`, 48px height, 0px border-radius
- Secondary button (Cancel): Gray 90 `#393939`, 48px height, 0px border-radius

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-07: History Modal
**Priority:** P1  
**Preconditions:** At least one application with history exists  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Click "History" on any application row
3. Inspect the modal styling

**Expected Results:** Same as TC-AUDIT-06 (modal structure)

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-08: Copy Modal
**Priority:** P1  
**Preconditions:** At least one application exists  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Click "Copy" on any application row
3. Inspect the modal styling

**Expected Results:** Same as TC-AUDIT-06 (modal structure)

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-09: Canvas Explain Modal
**Priority:** P1  
**Preconditions:** Workflow canvas with nodes  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS
2. Click "Explain" button
3. Inspect the modal styling

**Expected Results:** Same as TC-AUDIT-06 (modal structure)

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-10: Canvas Generate Modal
**Priority:** P1  
**Preconditions:** Workflow canvas open  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS
2. Click "Generate" button
3. Inspect the modal styling

**Expected Results:** Same as TC-AUDIT-06 (modal structure)

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-11: Canvas JsonPath Modal
**Priority:** P1  
**Preconditions:** Workflow canvas open  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS
2. Click "JsonPath" button
3. Inspect the modal styling

**Expected Results:** Same as TC-AUDIT-06 (modal structure)

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-12: Toast Notifications
**Priority:** P2  
**Preconditions:** Any action that triggers a toast  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Perform an action that triggers a success toast (e.g., save workflow)
3. Perform an action that triggers an error toast (e.g., invalid input)
4. Inspect toast styling

**Expected Results:**
- Toast border-radius: 0px
- Success toast: Green 50 `#24a148` accent
- Error toast: Red 60 `#da1e28` accent
- Toast text: Gray 100 `#161616`, 14px, IBM Plex Sans
- Toast background: White `#ffffff` with Gray 30 border

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

#### TC-AUDIT-13: Empty States
**Priority:** P2  
**Preconditions:** Empty application list or empty records list  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows (with no applications)
2. Navigate to https://workflow-ui-gamma.vercel.app/records (with no records)
3. Inspect empty state styling

**Expected Results:**
- Empty state text: Gray 70 `#525252`, 16px, IBM Plex Sans
- Empty state icon/illustration: Gray 50 `#8d8d8d`
- No rounded corners on empty state containers

**Actual Results:** [To be filled during QA]  
**Status:** [PASS/FAIL]  
**Screenshots:** [Attach if FAIL]

---

### 2.2 Automated Playwright Test Cases

#### TC-APP-DESK-11: Delete Confirmation Carbon Styling (Desktop)
**Priority:** P0  
**File:** `workflow-ui/e2e/applications-desktop.spec.ts`  
**Test Code:**
```typescript
test('TC-APP-DESK-11: Delete confirmation uses Carbon danger modal', async ({ page }) => {
  await page.goto('http://localhost:5173/workflows');
  await page.waitForLoadState('networkidle');
  
  // Trigger delete
  await page.getByRole('row').first().getByRole('button', { name: 'Delete' }).click();
  
  // Layer 1: Exists
  const modal = page.locator('.ant-modal');
  await expect(modal).toBeVisible();
  
  // Layer 2: Size
  const box = await modal.boundingBox();
  expect(box).toBeTruthy();
  expect(box!.width).toBeGreaterThan(400);
  
  // Layer 5: Effect (Carbon styles)
  const modalContent = modal.locator('.ant-modal-content');
  await expect(modalContent).toHaveCSS('border-radius', '0px');
  
  const okButton = modal.getByRole('button', { name: 'OK' });
  await expect(okButton).toHaveCSS('background-color', 'rgb(218, 30, 40)'); // Red 60
  await expect(okButton).toHaveCSS('height', '48px');
  await expect(okButton).toHaveCSS('border-radius', '0px');
  
  const title = modal.locator('.ant-modal-title');
  await expect(title).toHaveCSS('color', 'rgb(22, 22, 22)'); // Gray 100
  await expect(title).toHaveCSS('font-family', /IBM Plex Sans/);
});
```

**Expected Result:** Test passes with all assertions  
**Actual Result:** [To be filled during E2E run]  
**Status:** [PASS/FAIL]

---

#### TC-APP-MOB-11: Delete Confirmation Carbon Styling (Mobile)
**Priority:** P0  
**File:** `workflow-ui/e2e/applications-mobile.spec.ts`  
**Test Code:**
```typescript
test('TC-APP-MOB-11: Delete confirmation uses Carbon danger modal', async ({ page }) => {
  await page.goto('http://localhost:5173/workflows');
  await page.waitForLoadState('networkidle');
  
  // Trigger delete from mobile overflow
  await page.locator('.mobile-card').first().locator('[aria-label="more"]').click();
  await page.getByText('Delete').click();
  
  // Same assertions as TC-APP-DESK-11
  const modal = page.locator('.ant-modal');
  await expect(modal).toBeVisible();
  
  const modalContent = modal.locator('.ant-modal-content');
  await expect(modalContent).toHaveCSS('border-radius', '0px');
  
  const okButton = modal.getByRole('button', { name: 'OK' });
  await expect(okButton).toHaveCSS('background-color', 'rgb(218, 30, 40)');
  await expect(okButton).toHaveCSS('height', '48px');
  await expect(okButton).toHaveCSS('border-radius', '0px');
});
```

**Expected Result:** Test passes with all assertions  
**Actual Result:** [To be filled during E2E run]  
**Status:** [PASS/FAIL]

---

#### TC-APP-DESK-12: Dropdown Carbon Styling (Desktop)
**Priority:** P1  
**File:** `workflow-ui/e2e/applications-desktop.spec.ts`  
**Test Code:**
```typescript
test('TC-APP-DESK-12: Application overflow dropdown uses Carbon styling', async ({ page }) => {
  await page.goto('http://localhost:5173/workflows');
  await page.waitForLoadState('networkidle');
  
  // Open dropdown
  await page.getByRole('row').first().locator('[aria-label="more"]').click();
  
  // Layer 1: Exists
  const dropdown = page.locator('.ant-dropdown');
  await expect(dropdown).toBeVisible();
  
  // Layer 5: Effect (Carbon styles)
  const menu = dropdown.locator('.ant-dropdown-menu');
  await expect(menu).toHaveCSS('background-color', 'rgb(244, 244, 244)'); // Gray 10
  await expect(menu).toHaveCSS('border-radius', '0px');
  await expect(menu).toHaveCSS('border-width', '1px');
  
  const menuItem = menu.locator('.ant-dropdown-menu-item').first();
  await expect(menuItem).toHaveCSS('font-family', /IBM Plex Sans/);
  await expect(menuItem).toHaveCSS('font-size', '14px');
});
```

**Expected Result:** Test passes with all assertions  
**Actual Result:** [To be filled during E2E run]  
**Status:** [PASS/FAIL]

---

#### TC-APP-DESK-13: Settings Modal Visual Baseline
**Priority:** P1  
**File:** `workflow-ui/e2e/applications-desktop.spec.ts`  
**Test Code:**
```typescript
test('TC-APP-DESK-13: Settings modal visual baseline', async ({ page }) => {
  await page.goto('http://localhost:5173/workflows');
  await page.waitForLoadState('networkidle');
  
  await page.getByRole('row').first().getByRole('button', { name: 'Settings' }).click();
  
  const modal = page.locator('.ant-modal');
  await expect(modal).toBeVisible();
  
  // Visual baseline
  await expect(modal).toHaveScreenshot('settings-modal-carbon.png', {
    maxDiffPixels: 100
  });
});
```

**Expected Result:** Screenshot matches baseline  
**Actual Result:** [To be filled during E2E run]  
**Status:** [PASS/FAIL]

---

### 2.3 Test Coverage Gap Analysis

#### Gap 1: Modal Flows Not Tested in v28.0
**Description:** Delete confirmation, Settings modal, History modal, Copy modal were never opened in E2E tests  
**Root Cause:** Tests focused on happy path (create, navigate, edit) but not destructive or settings flows  
**Recommendation:** Add TC-APP-DESK-11, TC-APP-MOB-11, TC-APP-DESK-13 to cover these flows

#### Gap 2: Layer 5 Assertions Missing
**Description:** Existing tests used `toBeVisible()` but didn't assert computed styles  
**Root Cause:** v28.0 tests written before v30.0 five-layer validation framework  
**Recommendation:** Retrofit existing modal tests with Layer 5 assertions (border-radius, colors, heights)

#### Gap 3: Portal Components Not Tested
**Description:** Dropdown, Tooltip, Popconfirm styling never verified  
**Root Cause:** Portal components render outside main DOM tree, harder to test  
**Recommendation:** Add TC-APP-DESK-12 to verify dropdown styling

#### Gap 4: Visual Baselines Not Captured
**Description:** No screenshot baselines for critical modals  
**Root Cause:** v28.0 focused on functional tests, not visual regression  
**Recommendation:** Add TC-APP-DESK-13 and similar for other modals

---

## 3. Test Environment

**UAT Environment:**
- Frontend: https://workflow-ui-gamma.vercel.app
- operation-api: https://workflow-operation-api-n9sbp.ondigitalocean.app
- online-api: https://workflow-online-api-nr3e4.ondigitalocean.app

**Local Environment:**
- Frontend: http://localhost:5173
- Mock data: `workflow-ui/e2e/mocks.ts`

**Browsers:**
- Desktop: Chrome 1280px width
- Mobile: Chrome 390×844px, `isMobile: true`, `hasTouch: true`

---

## 4. Test Execution Plan

### Phase 1: Manual Audit (Day 1)
1. QA executes TC-AUDIT-01 through TC-AUDIT-13 on UAT environment
2. Document all gaps with screenshots and severity
3. Create numbered gap list in `ui-test-report-v35.0.md`

### Phase 2: Implementation (Days 2-3)
1. Frontend dev fixes P0 gaps
2. Frontend dev fixes P1 gaps
3. Frontend dev addresses P2 gaps (if time permits)

### Phase 3: Automated Testing (Day 4)
1. E2E Tester adds TC-APP-DESK-11, TC-APP-MOB-11, TC-APP-DESK-12, TC-APP-DESK-13
2. E2E Tester runs full Playwright suite locally
3. E2E Tester captures visual baselines

### Phase 4: QA Verification (Day 4)
1. QA re-executes TC-AUDIT-01 through TC-AUDIT-13 on UAT
2. QA verifies all gaps are resolved
3. QA writes `ui-test-report-v35.0.md`

### Phase 5: E2E UAT (Day 5)
1. E2E Tester runs full Playwright suite on UAT environment
2. E2E Tester writes `e2e-test-report-v35.0.md`
3. E2E Tester writes `uat-report-v35.0.md` with PASS/FAIL verdict

---

## 5. Success Criteria

- All P0 manual audit test cases PASS
- All P1 manual audit test cases PASS (or documented as acceptable)
- All automated Playwright tests PASS
- Visual baselines captured for critical modals
- Test coverage gap analysis documented
- UAT environment spot-check confirms Carbon compliance
- No functional regressions in existing test suite

---

## 6. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Audit reveals extensive gaps | Prioritize P0/P1; defer P2 to future iteration |
| Automated tests become flaky | Use stable wait strategies from v30.0 framework |
| Visual baselines fail in CI | Set reasonable `maxDiffPixels` threshold (100) |
| UAT environment differs from local | Test on both environments; document differences |

---

## 7. Test Deliverables

1. **ui-test-report-v35.0.md** — QA verification report with gap list and resolution status
2. **e2e-test-report-v35.0.md** — E2E Tester report with Playwright run results
3. **uat-report-v35.0.md** — E2E Tester UAT report with PASS/FAIL verdict
4. **Updated Playwright specs** — TC-APP-DESK-11, TC-APP-MOB-11, TC-APP-DESK-12, TC-APP-DESK-13
5. **Visual baseline screenshots** — `settings-modal-carbon.png`, etc.

---

**End of Test Document v35.0**
