# Test Doc v24.0 — Node Editor Drawer: Accessibility Violations

**Label:** `TODO-uat-e2e-node-drawer-accessibility-violations`
**Date:** 2026-04-11
**Status:** Draft — awaiting human approval

---

## 1. Scope

This Test Doc covers accessibility compliance fixes for the node editor drawer (`.ant-drawer`) identified in UAT E2E Pass 3. Two WCAG violations must be resolved:

1. **Missing ARIA dialog name** (WCAG 2.1 Level A, `aria-dialog-name` rule)
2. **Insufficient color contrast** (WCAG 2.1 Level AA, `color-contrast` rule)

**Source:** `specs/uat-report-e2e-pass3.md` — TC-NODE-ENHANCED-01

---

## 2. Test Cases

### 2.1 ARIA Label Tests

| TC ID | Description | Steps | Expected |
|-------|-------------|-------|----------|
| TC-A11Y-01 | Drawer has aria-label attribute | Open node editor drawer → Inspect `.ant-drawer-content[role="dialog"]` | `aria-label="Node Configuration"` present |
| TC-A11Y-02 | Screen reader announces drawer purpose (VoiceOver) | Enable VoiceOver → Click a node | VoiceOver announces "Node Configuration, dialog" |
| TC-A11Y-03 | Screen reader announces drawer purpose (NVDA) | Enable NVDA → Click a node | NVDA announces "Node Configuration dialog" |
| TC-A11Y-04 | Axe scan passes aria-dialog-name rule | Open drawer → Run Axe | Zero violations for `aria-dialog-name` rule |

### 2.2 Color Contrast Tests

| TC ID | Description | Steps | Expected |
|-------|-------------|-------|----------|
| TC-A11Y-05 | Section headers meet 4.5:1 contrast ratio | Open drawer → Inspect "NODE CONFIGURATION" header text | Color is `#6b6b75` or darker (contrast ≥ 4.5:1 on `#fafafa` background) |
| TC-A11Y-06 | All three section headers have sufficient contrast | Open drawer → Check Description / Rules / Action headers | All headers pass 4.5:1 minimum |
| TC-A11Y-07 | Axe scan passes color-contrast rule | Open drawer → Run Axe | Zero violations for `color-contrast` rule |
| TC-A11Y-08 | Visual regression — headers still readable | Open drawer → Compare screenshot | Headers visible and legible (darker than before) |

### 2.3 Comprehensive Accessibility Scan

| TC ID | Description | Steps | Expected |
|-------|-------------|-------|----------|
| TC-A11Y-09 | Full Axe scan on drawer (all rules) | Open drawer → Run `@axe-core/playwright` with all rules | Zero violations (WCAG 2.1 Level A + AA) |
| TC-A11Y-10 | Keyboard navigation unaffected | Tab through drawer fields | Focus order logical; all inputs reachable |
| TC-A11Y-11 | ESC key closes drawer | Open drawer → Press ESC | Drawer closes; focus returns to canvas |

### 2.4 Regression Tests

| TC ID | Description | Steps | Expected |
|-------|-------------|-------|----------|
| TC-A11Y-12 | Drawer still opens on node click | Click any node on canvas | Drawer slides in from right (desktop) or bottom (mobile) |
| TC-A11Y-13 | Three-panel layout intact | Open drawer | Description / Rules / Action sections visible |
| TC-A11Y-14 | Form inputs functional | Open drawer → Edit Description → Save | Value persists after save |
| TC-A11Y-15 | JSON formatting works | Open drawer → Edit Rules JSON → Blur | JSON auto-formats on blur |
| TC-A11Y-16 | Mobile drawer placement unchanged | Open drawer on mobile (390×844) | Drawer opens from bottom; height ≤ 80vh |

---

## 3. UAT Test Script (Manual Screen Reader Testing)

**Environment:** https://workflow-ui-gamma.vercel.app  
**Browsers:** Chrome (latest), Safari (latest)  
**Screen Readers:** VoiceOver (macOS/iOS), NVDA (Windows)

### 3.1 VoiceOver Test (macOS)

| Step | Action | Expected Announcement |
|------|--------|----------------------|
| 1 | Enable VoiceOver (Cmd+F5) | VoiceOver starts |
| 2 | Navigate to canvas with workflow | Canvas loads |
| 3 | Tab to first node or click with mouse | Node selected |
| 4 | Click node to open drawer | "Node Configuration, dialog" announced |
| 5 | Tab through drawer fields | Each field label announced clearly |
| 6 | Press ESC to close | "Dialog closed" or focus returns to canvas |

### 3.2 NVDA Test (Windows)

| Step | Action | Expected Announcement |
|------|--------|----------------------|
| 1 | Enable NVDA (Ctrl+Alt+N) | NVDA starts |
| 2 | Navigate to canvas with workflow | Canvas loads |
| 3 | Click a node | Node selected |
| 4 | Drawer opens | "Node Configuration dialog" announced |
| 5 | Tab through fields | Field labels announced |
| 6 | Press ESC | Drawer closes |

### 3.3 Low Vision Test (Color Contrast)

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Open drawer on desktop | Drawer visible |
| 2 | Inspect section headers (Description / Rules / Action) | Headers clearly readable; not washed out |
| 3 | Use browser DevTools color picker on header text | Computed color is `#6b6b75` or darker |
| 4 | Calculate contrast ratio (WebAIM tool) | Ratio ≥ 4.5:1 against `#fafafa` background |

---

## 4. Entry Criteria

- PM doc and Arch doc for accessibility fixes approved by human
- `@axe-core/playwright` installed in `workflow-ui` (`npm install --save-dev @axe-core/playwright`)
- UAT environment accessible at https://workflow-ui-gamma.vercel.app
- Screen reader software available for manual testing (VoiceOver or NVDA)

---

## 5. Exit Criteria

- All TC-A11Y-01 through TC-A11Y-16 pass
- Zero Axe violations for `aria-dialog-name` and `color-contrast` rules
- Manual screen reader testing confirms drawer purpose is announced
- Visual regression baseline created and approved
- No severity-1 defects open

---

## 6. Must-Pass Tests (Deployment Gate)

| TC | Reason |
|----|--------|
| TC-A11Y-01 | WCAG 2.1 Level A compliance — blocking for accessibility certification |
| TC-A11Y-04 | Axe validation of aria-dialog-name fix |
| TC-A11Y-05 | WCAG 2.1 Level AA compliance — blocking for accessibility certification |
| TC-A11Y-07 | Axe validation of color-contrast fix |
| TC-A11Y-09 | Full WCAG 2.1 A+AA scan — no regressions introduced |
| TC-A11Y-12 | Core functionality — drawer must still open |

---

*Status: Draft — awaiting human approval before implementation*
