# UI Test Report — v35.0

**Document version:** 35.0  
**Date:** 2026-04-13  
**Status:** In Progress  
**TODO item:** `TODO-ui-ibm-carbon-audit-residual-styling`  
**Related docs:** pm-doc-v35.0.md, arch-doc-v35.0.md, test-doc-v35.0.md

---

## 1. Audit Summary

This report documents the audit and fixes for IBM Carbon Design Language compliance gaps in Workflow Studio UI.

---

## 2. Audit Results

### 2.1 Gap List

#### Gap #1: Application Overflow Dropdown (Mobile) - Missing Carbon Styling
**Priority:** P1  
**Location:** `src/routes/workflows/index.tsx:372`  
**Component:** `<Dropdown>` in mobile application card overflow menu  
**Issue:** Dropdown missing `overlayClassName="carbon-dropdown"`  
**Root Cause:** Post-v28 addition without Carbon review  
**Status:** ✅ FIXED  
**Fix:** Added `overlayClassName="carbon-dropdown"` to Dropdown component

---

#### Gap #2: Canvas Toolbar Dropdown (Mobile) - Missing Carbon Styling
**Priority:** P1  
**Location:** `src/routes/workflows/-components/workflow-header/index.tsx:544`  
**Component:** `<Dropdown>` in mobile canvas toolbar overflow menu  
**Issue:** Dropdown missing `overlayClassName="carbon-dropdown"`  
**Root Cause:** Post-v28 addition without Carbon review  
**Status:** ✅ FIXED  
**Fix:** Added `overlayClassName="carbon-dropdown"` to Dropdown component

---

#### Gap #3: Node Editor Tooltip - Missing Carbon Styling
**Priority:** P2  
**Location:** `src/routes/workflows/-components/workflow-drawer/NodeSection.tsx:43`  
**Component:** `<Tooltip>` for developer field mapping hints  
**Issue:** Tooltip missing `overlayClassName="carbon-tooltip"`  
**Root Cause:** Component created before Carbon tooltip class was defined  
**Status:** ✅ FIXED  
**Fix:** Added `overlayClassName="carbon-tooltip"` to Tooltip component

---

#### Gap #4: Canvas Palette Tooltip - Missing Carbon Styling
**Priority:** P2  
**Location:** `src/routes/workflows/-components/workflow-sider/index.tsx:73`  
**Component:** `<Tooltip>` for collapsed palette node descriptions  
**Issue:** Tooltip missing `overlayClassName="carbon-tooltip"`  
**Root Cause:** Component created before Carbon tooltip class was defined  
**Status:** ✅ FIXED  
**Fix:** Added `overlayClassName="carbon-tooltip"` to Tooltip component

---

### 2.2 CSS Enhancements

#### Enhancement #1: Carbon Dropdown Class
**File:** `src/styles/carbon-overrides.css`  
**Status:** ✅ COMPLETE  
**Changes:**
- Added `.carbon-dropdown` class with Carbon-specific styling
- Background: Gray 10 `#f4f4f4`
- Border: 1px solid Gray 30 `#c6c6c6`
- Border-radius: 0px
- Shadow: `0 2px 6px rgba(0,0,0,0.3)`
- Menu item text: Gray 100 `#161616`, 14px, IBM Plex Sans
- Menu item hover: Gray 20 `#e8e8e8`

---

#### Enhancement #2: Carbon Tooltip Class
**File:** `src/styles/carbon-overrides.css`  
**Status:** ✅ COMPLETE  
**Changes:**
- Added `.carbon-tooltip` class with Carbon-specific styling
- Background: Gray 10 `#f4f4f4`
- Text: Gray 100 `#161616`, 14px, IBM Plex Sans
- Border: 1px solid Gray 30 `#c6c6c6`
- Border-radius: 0px
- Shadow: `0 2px 6px rgba(0,0,0,0.3)`

---

## 3. Test Coverage Gap Analysis

### 3.1 Why Existing Tests Missed These Gaps

**Gap Type:** Layer 5 (computed style) assertions missing

**Root Cause:**
- v28.0 E2E tests focused on functional behavior (`toBeVisible()`, `toHaveText()`)
- No assertions on computed CSS properties (border-radius, colors, box-shadow)
- Portal components (Dropdown, Tooltip) render outside main DOM tree, harder to test

**Example:**
```typescript
// v28.0 test (insufficient)
await expect(dropdown).toBeVisible();

// v35.0 test (Layer 5 assertion)
await expect(dropdown).toBeVisible();
const menu = dropdown.locator('.ant-dropdown-menu');
await expect(menu).toHaveCSS('background-color', 'rgb(244, 244, 244)'); // Gray 10
await expect(menu).toHaveCSS('border-radius', '0px');
```

---

### 3.2 Flows Not Tested in v28.0

**Untested Flows:**
1. Application delete confirmation modal (desktop + mobile)
2. Application overflow dropdown (mobile)
3. Canvas toolbar overflow dropdown (mobile)
4. Node editor tooltips
5. Canvas palette tooltips

**Why Missed:**
- Tests focused on happy path (create, navigate, edit)
- Destructive actions (delete) not covered
- Mobile overflow menus not opened in tests
- Tooltips require hover interaction, not tested

---

## 4. Files Modified

### Modified Files
1. `src/styles/carbon-overrides.css` — Added `.carbon-dropdown` and `.carbon-tooltip` classes
2. `src/routes/workflows/index.tsx` — Added `overlayClassName="carbon-dropdown"` to mobile overflow Dropdown
3. `src/routes/workflows/-components/workflow-header/index.tsx` — Added `overlayClassName="carbon-dropdown"` to canvas toolbar Dropdown
4. `src/routes/workflows/-components/workflow-drawer/NodeSection.tsx` — Added `overlayClassName="carbon-tooltip"` to developer hint Tooltip
5. `src/routes/workflows/-components/workflow-sider/index.tsx` — Added `overlayClassName="carbon-tooltip"` to palette Tooltip

---

## 5. Verification Status

### 5.1 Manual Verification (Pending)
- [ ] TC-AUDIT-01: Application Delete Confirmation (Desktop)
- [ ] TC-AUDIT-02: Application Delete Confirmation (Mobile)
- [ ] TC-AUDIT-03: Application Overflow Dropdown (Desktop)
- [ ] TC-AUDIT-04: Application Overflow Dropdown (Mobile)
- [ ] TC-AUDIT-05: Canvas Toolbar Tooltips
- [ ] TC-AUDIT-06: Settings Modal
- [ ] TC-AUDIT-07: History Modal
- [ ] TC-AUDIT-08: Copy Modal
- [ ] TC-AUDIT-09: Canvas Explain Modal
- [ ] TC-AUDIT-10: Canvas Generate Modal
- [ ] TC-AUDIT-11: Canvas JsonPath Modal
- [ ] TC-AUDIT-12: Toast Notifications
- [ ] TC-AUDIT-13: Empty States

### 5.2 Automated Tests (Pending)
- [ ] TC-APP-DESK-11: Delete Confirmation Carbon Styling (Desktop)
- [ ] TC-APP-MOB-11: Delete Confirmation Carbon Styling (Mobile)
- [ ] TC-APP-DESK-12: Dropdown Carbon Styling (Desktop)
- [ ] TC-APP-DESK-13: Settings Modal Visual Baseline

---

## 6. Next Steps

1. **QA Verification:** QA team to execute manual audit test cases (TC-AUDIT-01 through TC-AUDIT-13) on UAT environment
2. **Automated Tests:** E2E Tester to add Playwright test cases with Layer 5 assertions
3. **UAT:** E2E Tester to run full Playwright suite on UAT environment and write `uat-report-v35.0.md`

---

## 7. Summary

**Gaps Found:** 4 (2 P1, 2 P2)  
**Gaps Fixed:** 4 (100%)  
**CSS Enhancements:** 2 (carbon-dropdown, carbon-tooltip)  
**Files Modified:** 5  
**Test Coverage Improvements:** Documented gaps and recommendations for Layer 5 assertions

**Status:** Implementation complete, pending QA verification and automated test additions.

---

**End of UI Test Report v35.0**
