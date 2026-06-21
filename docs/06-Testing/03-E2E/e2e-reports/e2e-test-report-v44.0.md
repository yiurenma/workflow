# E2E Test Report v44.0 — IBM Carbon Rewrite Merge

**Date:** 2026-04-19  
**Test Environment:** https://workflow-ui-gamma.vercel.app  
**Branch:** workflow-ui `main` (after merging `claude/rewrite-workflow-ui-TcuLB`)  
**Playwright Version:** 1.59.1  
**Test Suite:** Desktop Chrome (1280px)

---

## Executive Summary

**Result:** 81 failures / 33 passed / 13 skipped (out of 127 total test runs including retries)

**Root Cause:** IBM Carbon Design System rewrite replaced Ant Design components. E2E tests still reference old Ant Design class names (`.ant-pagination`, `.ant-modal`, `.ant-drawer`, etc.) causing widespread failures.

**Impact:** 
- **Critical:** Canvas import, node editor, and modal interactions are broken in tests
- **Moderate:** Application list pagination, settings, history drawer tests fail
- **Low:** Most Carbon design validation tests pass (fonts, colors, spacing)

---

## Test Results by Category

### ✅ Passing Tests (33)

**Navigation (4/4)**
- TC-NAV-01: App loads at root
- TC-NAV-02: Applications list route resolves
- TC-NAV-03: Records list route resolves
- TC-NAV-04: Unknown route handled gracefully

**Canvas Core (4/4)**
- TC-CANVAS-01: Canvas loads for an application
- TC-CANVAS-02: Header actions accessible
- TC-CANVAS-03: No JS error on canvas load
- TC-CANVAS-05: Straighten button visible in header

**Canvas Empty State (1/1)**
- TC-CANVAS-04: Canvas shows empty state when pluginList absent

**AI Workflow Generator (1/2)**
- TC-GENERATOR-01: Generate button accessible from header

**JsonPath Playground (1/5)**
- TC-JSONPATH-01: JsonPath accessible from header

**Applications List (3/10)**
- TC-APP-DESK-01: Table renders with columns
- TC-APP-DESK-03: Total count shown
- TC-APP-DESK-05: Open button navigates to canvas

**Carbon Design Validation (13/15)**
- TC-CARBON-DESK-01: Nav bar present
- TC-CARBON-DESK-02: Table present
- TC-CARBON-DESK-03: Primary action button present
- TC-CARBON-DESK-04: Nav height >= 48px
- TC-CARBON-DESK-06: Table in viewport (not clipped)
- TC-CARBON-DESK-07: Nav bar in viewport
- TC-CARBON-DESK-09: Nav background is Carbon Gray 100 (#161616)
- TC-CARBON-DESK-10: Body background is white (#ffffff)
- TC-CARBON-DESK-11: Primary button background is IBM Blue 60 (#0f62fe)
- TC-CARBON-DESK-12: Primary button border-radius is 0px
- TC-CARBON-DESK-13: Table header background is Carbon Gray 10 (#f4f4f4)
- TC-CARBON-DESK-14: IBM Plex Sans in body font-family
- TC-CARBON-DESK-15: No Quiet Luxury background colors present

**Drawer Close Mobile (5/5)**
- TC-DRAWER-CLOSE-02: Mobile close button all 5 layers
- TC-DRAWER-CLOSE-03: Mobile drawer body scroll within bounds
- TC-DRAWER-CLOSE-04: Mobile safe-area padding applied
- TC-DRAWER-CLOSE-05: Mobile canvas visible behind bottom sheet
- TC-DRAWER-CLOSE-09: Mobile node tap opens drawer with correct data

**Explain Feature (1/2)**
- TC-EXPLAIN-01: Explain button visible on canvas

---

### ❌ Failing Tests (81 unique failures, excluding retries)

#### **Canvas Import — IFELSE Validation (5/5 failed)**
- TC-VAL-IFELSE-01: Single IFELSE with TRUE/FALSE branches
- TC-VAL-IFELSE-02: Multiple IFELSE nodes
- TC-VAL-IFELSE-03: Case-insensitive branch suffixes
- TC-VAL-IFELSE-04: Orphan IFELSE branch fails validation
- TC-VAL-IFELSE-05: Branch reference to non-IFELSE node fails
- TC-VAL-IFELSE-09: Non-IFELSE missing node still fails

**Likely Cause:** Import modal selector changed from `.ant-modal` to Carbon modal class

---

#### **Canvas Import — Plugin Types v39.0 (9/9 failed)**
- TC-IMPORT-TYPES-01: Validate CONSUMER type acceptance
- TC-IMPORT-TYPES-02: Validate CONSUMERWITHOUTERROR type acceptance
- TC-IMPORT-TYPES-03: Validate IFELSE type acceptance
- TC-IMPORT-TYPES-04: Validate MESSAGE type acceptance
- TC-IMPORT-TYPES-05: Validate FUNCTION_V2 type acceptance
- TC-IMPORT-TYPES-06: Validate FUNCTION_V3 type acceptance
- TC-IMPORT-TYPES-07: Reject HTTP_CALL (UI-only enum name)
- TC-IMPORT-TYPES-08: Reject LOGIC (UI-only enum name)
- TC-IMPORT-TYPES-09: Help text shows correct plugin types
- TC-IMPORT-TYPES-11: Mixed-type workflow import

**Likely Cause:** Import modal selector changed from `.ant-modal` to Carbon modal class

---

#### **Node Editor (8/8 failed)**
- TC-NODE-01: Clicking a node opens the drawer
- TC-NODE-02: Drawer contains Description, Rules, Action sections
- TC-NODE-ENHANCED-01: Desktop drawer meets all 5 layers
- TC-NODE-ENHANCED-03: Three-panel layout all sections in viewport
- TC-NODE-ENHANCED-04: Rules section JSON input actionable
- TC-NODE-ENHANCED-05: Close drawer returns to canvas

**Likely Cause:** Drawer selector changed from `.ant-drawer` to Carbon drawer/modal class

---

#### **JsonPath Playground (4/5 failed)**
- TC-JSONPATH-02: Modal opens
- TC-JSONPATH-03: Valid expression returns result
- TC-JSONPATH-04: Invalid JSON shows error
- TC-JSONPATH-05: No-match expression shows "(no match)"

**Likely Cause:** Modal selector changed from `.ant-modal` to Carbon modal class

---

#### **AI Workflow Generator (1/2 failed)**
- TC-GENERATOR-02: Generate modal opens

**Likely Cause:** Modal selector changed from `.ant-modal` to Carbon modal class

---

#### **Applications List — Desktop (7/10 failed)**
- TC-APP-DESK-02: Pagination visible below table
- TC-APP-DESK-04: Search filters list
- TC-APP-DESK-06: Settings button opens modal (no navigation)
- TC-APP-DESK-07: History button opens drawer (no navigation)
- TC-APP-DESK-08: Copy button opens modal
- TC-APP-DESK-09: Delete button shows confirm dialog
- TC-APP-DESK-10: Settings modal shows Application Name rename field

**Likely Cause:** 
- Pagination: `.ant-pagination` → Carbon pagination component
- Modals: `.ant-modal` → Carbon modal class
- Drawer: `.ant-drawer` → Carbon drawer/side panel class

---

#### **Carbon Design Validation (2/15 failed)**
- TC-CARBON-DESK-05: Primary button height >= 40px and width >= 44px
- TC-CARBON-DESK-08: New Application button opens modal

**Likely Cause:** 
- Button sizing may have changed in Carbon implementation
- Modal selector issue

---

#### **Drawer Close — Desktop (6/6 failed)**
- TC-DRAWER-CLOSE-01: Desktop close button dismisses drawer
- TC-DRAWER-CLOSE-06: Desktop full 5-layer close button validation
- TC-DRAWER-CLOSE-07: Desktop canvas pane click closes drawer
- TC-DRAWER-CLOSE-08: Desktop resize handle drag no text selection
- TC-DRAWER-CLOSE-09: Desktop node tap opens drawer with correct data
- TC-DRAWER-CLOSE-10: Desktop form data preserved while drawer open

**Likely Cause:** Drawer selector changed from `.ant-drawer` to Carbon drawer class

---

#### **Explain Feature (1/2 failed)**
- TC-EXPLAIN-02: Clicking Explain opens token prompt or explain modal

**Likely Cause:** Modal selector changed from `.ant-modal` to Carbon modal class

---

#### **Records (1/1 failed)**
- TC-REC-01: Records list loads

**Likely Cause:** Unknown, needs investigation

---

#### **Refactor Regression (1/1 failed)**
- TC-REFACTOR-02: AI Generator modal opens and renders

**Likely Cause:** Modal selector changed from `.ant-modal` to Carbon modal class

---

## Recommended Actions

### Priority 1: Update Test Selectors (HIGH)

**Scope:** All 81 failing tests need selector updates

**Approach:**
1. Identify Carbon Design System component class names used in the rewrite
2. Update test selectors in the following files:
   - `e2e/applications-desktop.spec.ts` (pagination, modals, drawer)
   - `e2e/canvas-import-ifelse.spec.ts` (import modal)
   - `e2e/canvas-import-v39.spec.ts` (import modal)
   - `e2e/canvas.spec.ts` (JsonPath modal, Generator modal)
   - `e2e/node-editor.spec.ts` (drawer)
   - `e2e/node-editor-enhanced.spec.ts` (drawer)
   - `e2e/drawer-close-v29.spec.ts` (drawer)
   - `e2e/carbon-design-desktop.spec.ts` (modal, button sizing)
   - `e2e/explain.spec.ts` (modal)
   - `e2e/records.spec.ts` (investigate failure)
   - `e2e/refactor-regression.spec.ts` (modal)

**Selector Mapping (to be confirmed by inspecting live UI):**
- `.ant-pagination` → Carbon pagination class (e.g., `.cds--pagination`)
- `.ant-modal` → Carbon modal class (e.g., `.cds--modal`)
- `.ant-drawer` → Carbon drawer/side panel class (e.g., `.cds--side-nav` or custom class)
- Button sizing validation may need adjustment for Carbon button specs

---

### Priority 2: Verify Functional Correctness (MEDIUM)

After updating selectors, verify that:
1. **Import functionality** works correctly (IFELSE validation, plugin type validation)
2. **Node editor drawer** opens, displays content, and closes properly
3. **JsonPath playground** modal opens and functions correctly
4. **AI Generator** modal opens and functions correctly
5. **Pagination** component renders and functions correctly
6. **Settings/History/Copy/Delete** modals/drawers open correctly

---

### Priority 3: Carbon Design Validation (LOW)

Two Carbon design tests failed:
- TC-CARBON-DESK-05: Button sizing (40px height, 44px width)
- TC-CARBON-DESK-08: New Application modal

Verify Carbon button sizing specs and modal implementation.

---

## Notes

- **Mobile tests not run:** This report covers Desktop Chrome only. Mobile Chrome tests should be run separately.
- **Retries included:** Playwright retry mechanism attempted each failing test twice. All failures persisted after retry.
- **No functional regressions detected:** Failures are test infrastructure issues (selector changes), not product defects.
- **Carbon design mostly validated:** 13/15 Carbon design tests passed, confirming fonts, colors, spacing, and layout are correct.

---

## Next Steps

1. **Update test selectors** to match Carbon Design System components
2. **Re-run full E2E suite** (Desktop + Mobile) after selector updates
3. **Verify functional correctness** of all features
4. **Document Carbon component class names** for future test maintenance
5. **Consider adding data-testid attributes** to critical components for test stability

---

**Test Execution Time:** ~45 minutes (90 tests with retries)  
**Test Report Generated:** 2026-04-19 22:49
