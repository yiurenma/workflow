# UAT Script v25.0

**Document version:** 25.0  
**Date:** 2026-04-11  
**Status:** Ready for Execution  
**Related docs:** pm-doc-v25.0.md, arch-doc-v25.0.md, test-doc-v25.0.md, ui-test-report-v25.0.md  
**TODO label:** `TODO-node-editor-form-descriptions-color-contrast`  
**UAT Environment:** https://workflow-ui-gamma.vercel.app

## Overview

This UAT script verifies that form field description text in the node editor drawer meets WCAG 2.1 Level AA color contrast requirements. The fix changes the text color from light gray to a darker gray for better readability.

## Prerequisites

- Access to UAT environment: https://workflow-ui-gamma.vercel.app
- Browser: Chrome 120+, Firefox 120+, or Safari 17+
- Browser extensions (recommended):
  - Axe DevTools (for accessibility scanning)
  - Chrome DevTools (built-in, for contrast checking)

## Test Cases

---

### UAT-01: Visual Inspection - HttpCallForm Description Text

**Objective:** Verify that description text in HttpCallForm is readable and has improved contrast.

**Steps:**

1. Navigate to https://workflow-ui-gamma.vercel.app
2. Log in if required
3. Click on any existing application (or create a new one if none exist)
4. On the workflow canvas, add or click on an **HTTP Call** node
5. The node editor drawer will open on the right side (desktop) or bottom (mobile)
6. Locate the three description text paragraphs:
   - Under "NODE DESCRIPTION" section: "The name shown on the canvas. What is this step called?"
   - Under "RULES" section: "Run only when… — conditions that must all match before this step executes."
   - Under "ACTION" section: "What the system does when this step runs."

**Expected Result:**
- All three description texts should be clearly readable
- Text should appear in a medium gray color (darker than before)
- Text should still look like secondary/helper text (not as dark as labels or headings)
- No visual glitches or layout issues

**Pass Criteria:** ✅ All description texts are clearly readable with improved contrast

---

### UAT-02: Visual Inspection - LogicForm Description Text

**Objective:** Verify that description text in LogicForm has the same improved contrast.

**Steps:**

1. On the same workflow canvas from UAT-01
2. Add or click on a **Logic** node (IF_ELSE, FUNCTION_V2, or FUNCTION_V3 type)
3. The node editor drawer will open
4. Locate the same three description text paragraphs as in UAT-01

**Expected Result:**
- Same as UAT-01: all three description texts should be clearly readable
- Consistent appearance with HttpCallForm descriptions

**Pass Criteria:** ✅ All description texts are clearly readable with improved contrast

---

### UAT-03: Contrast Measurement - Desktop

**Objective:** Measure actual contrast ratio using browser DevTools.

**Steps:**

1. Open Chrome DevTools (F12 or right-click → Inspect)
2. With the node editor drawer open (from UAT-01 or UAT-02)
3. Right-click on any description text paragraph → Inspect
4. In the Elements panel, find the `<p>` element with class `text-xs text-zinc-600 mb-3`
5. In the Styles panel, click on the color swatch next to the computed color
6. DevTools will show the contrast ratio

**Expected Result:**
- Contrast ratio should be **≥ 4.5:1** (WCAG AA minimum)
- DevTools should show a green checkmark ✓ next to the ratio
- Actual ratio should be approximately **7.0:1** on white background

**Pass Criteria:** ✅ Contrast ratio ≥ 4.5:1 with green checkmark in DevTools

---

### UAT-04: Contrast Measurement - Mobile Viewport

**Objective:** Verify contrast on mobile viewport.

**Steps:**

1. In Chrome DevTools, toggle device toolbar (Ctrl+Shift+M or Cmd+Shift+M)
2. Select a mobile device (e.g., iPhone 12 Pro, 390px width)
3. Refresh the page if needed
4. Navigate to workflow canvas and open node editor (drawer will appear at bottom)
5. Repeat contrast measurement from UAT-03

**Expected Result:**
- Same contrast ratio as desktop (≥ 4.5:1)
- Text remains readable on smaller screen
- No layout issues or text overflow

**Pass Criteria:** ✅ Contrast ratio ≥ 4.5:1 on mobile viewport

---

### UAT-05: Axe DevTools Accessibility Scan

**Objective:** Verify zero color contrast violations using automated accessibility tool.

**Steps:**

1. Install Axe DevTools browser extension if not already installed:
   - Chrome: https://chrome.google.com/webstore (search "Axe DevTools")
   - Firefox: https://addons.mozilla.org/firefox/ (search "Axe DevTools")
2. Open node editor drawer (HttpCallForm or LogicForm)
3. Open Axe DevTools panel in browser DevTools
4. Click "Scan ALL of my page" button
5. Wait for scan to complete
6. Filter results by "color-contrast" issue type
7. Check if any violations are reported for the description text elements

**Expected Result:**
- Zero color contrast violations for description text elements
- If any violations exist, they should NOT be related to the `text-zinc-600` description paragraphs
- Previous violations (if any) for these elements should now be resolved

**Pass Criteria:** ✅ Zero color contrast violations for node editor description text

---

### UAT-06: Visual Consistency Check

**Objective:** Ensure the fix doesn't negatively impact visual design.

**Steps:**

1. Open node editor drawer (any node type)
2. Compare the description text with other text elements:
   - Section headers (e.g., "NODE DESCRIPTION", "RULES", "ACTION")
   - Form field labels (e.g., "Step Name", "Provider Name")
   - Input placeholder text
   - Tooltip text
3. Verify visual hierarchy is maintained

**Expected Result:**
- Description text should be darker than before but still lighter than labels/headers
- Text should clearly appear as secondary/helper content
- Visual hierarchy: Headers (darkest) > Labels > Description text > Placeholders (lightest)
- No elements should look out of place or inconsistent

**Pass Criteria:** ✅ Visual hierarchy maintained, description text appropriately styled as secondary content

---

### UAT-07: No Regression - Other Node Editor Elements

**Objective:** Verify that only description text changed, nothing else.

**Steps:**

1. Open node editor drawer
2. Check the following elements have NOT changed:
   - Section header text color (should still be uppercase, small, light gray)
   - Form field labels (should be standard label color)
   - Input fields and their borders
   - Button colors and styles
   - Tooltip icon color
   - Background colors of sections
3. Try interacting with the form (type in fields, add rules, etc.)

**Expected Result:**
- Only the three description paragraphs have changed color
- All other elements maintain their original styling
- Form functionality works normally (no JavaScript errors)

**Pass Criteria:** ✅ No unintended style changes, form functions normally

---

### UAT-08: Cross-Browser Compatibility

**Objective:** Verify fix works consistently across browsers.

**Steps:**

1. Repeat UAT-01 and UAT-03 in at least two different browsers:
   - Chrome/Edge (Chromium-based)
   - Firefox
   - Safari (if on macOS)
2. Compare visual appearance and contrast measurements

**Expected Result:**
- Description text appears identical across all browsers
- Contrast ratio ≥ 4.5:1 in all browsers
- No browser-specific rendering issues

**Pass Criteria:** ✅ Consistent appearance and contrast across tested browsers

---

## Summary Checklist

Before marking this TODO as Done, verify all UAT cases pass:

- [ ] UAT-01: HttpCallForm description text is readable ✅
- [ ] UAT-02: LogicForm description text is readable ✅
- [ ] UAT-03: Desktop contrast ratio ≥ 4.5:1 ✅
- [ ] UAT-04: Mobile contrast ratio ≥ 4.5:1 ✅
- [ ] UAT-05: Zero Axe DevTools violations ✅
- [ ] UAT-06: Visual consistency maintained ✅
- [ ] UAT-07: No regression in other elements ✅
- [ ] UAT-08: Cross-browser compatibility ✅

## How to Report Results

After completing all UAT cases, please report:

1. **Overall Status:** PASS / FAIL / BLOCKED
2. **Individual Case Results:** For each UAT case, report PASS or FAIL
3. **Screenshots:** If any issues found, provide screenshots
4. **Browser/Device Info:** Which browsers and devices you tested on
5. **Any Observations:** Anything unexpected or noteworthy

## Expected Outcome

**All cases should PASS.** If any case fails:
- Document the failure with screenshots
- Note which browser/device/viewport
- Test Manager will create a new TODO item for the defect
- This TODO will remain open until all failures are resolved

## Notes

- This is a visual/accessibility fix with no functional changes
- No backend APIs are involved
- No database changes required
- Safe to test in UAT environment without risk of data corruption
