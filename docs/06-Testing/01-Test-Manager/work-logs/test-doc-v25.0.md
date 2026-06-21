# Test Document v25.0

**Document version:** 25.0  
**Date:** 2026-04-11  
**Status:** Draft  
**Related docs:** pm-doc-v25.0.md, arch-doc-v25.0.md  
**TODO label:** `TODO-node-editor-form-descriptions-color-contrast`

## Test Scope

Verify that form field description text in HttpCallForm and LogicForm components meets WCAG 2.1 Level AA color contrast requirements (minimum 4.5:1 ratio).

## Test Environment

- **Browser:** Chrome 120+, Firefox 120+, Safari 17+
- **Viewports:** Desktop (1280px), Mobile (390px)
- **Tools:** 
  - Axe DevTools browser extension
  - Chrome DevTools Accessibility panel
  - WebAIM Contrast Checker (https://webaim.org/resources/contrastchecker/)

## Test Cases

### TC-CONTRAST-01: HttpCallForm node name description contrast (white background)

**Preconditions:**
- Navigate to workflow canvas
- Open HttpCallForm node editor (HTTP Call node type)

**Steps:**
1. Locate the description text under the node name field: "The name shown on the canvas..."
2. Inspect the element and verify CSS class is `text-zinc-600`
3. Use Chrome DevTools Accessibility panel to measure contrast ratio
4. Verify computed color is #52525b (RGB: 82, 82, 91)

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on white background
- Text color is #52525b
- CSS class is `text-zinc-600`

**Priority:** HIGH

---

### TC-CONTRAST-02: HttpCallForm rules description contrast (white background)

**Preconditions:**
- Navigate to workflow canvas
- Open HttpCallForm node editor

**Steps:**
1. Locate the description text in the Rules section: "Run only when..."
2. Inspect the element and verify CSS class is `text-zinc-600`
3. Measure contrast ratio using DevTools
4. Verify computed color is #52525b

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on white background
- Text color is #52525b
- CSS class is `text-zinc-600`

**Priority:** HIGH

---

### TC-CONTRAST-03: HttpCallForm action description contrast (white background)

**Preconditions:**
- Navigate to workflow canvas
- Open HttpCallForm node editor

**Steps:**
1. Locate the description text in the Action section: "What the system does..."
2. Inspect the element and verify CSS class is `text-zinc-600`
3. Measure contrast ratio using DevTools
4. Verify computed color is #52525b

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on white background
- Text color is #52525b
- CSS class is `text-zinc-600`

**Priority:** HIGH

---

### TC-CONTRAST-04: LogicForm node name description contrast (white background)

**Preconditions:**
- Navigate to workflow canvas
- Open LogicForm node editor (Logic node type)

**Steps:**
1. Locate the description text under the node name field: "The name shown on the canvas..."
2. Inspect the element and verify CSS class is `text-zinc-600`
3. Measure contrast ratio using DevTools
4. Verify computed color is #52525b

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on white background
- Text color is #52525b
- CSS class is `text-zinc-600`

**Priority:** HIGH

---

### TC-CONTRAST-05: LogicForm rules description contrast (white background)

**Preconditions:**
- Navigate to workflow canvas
- Open LogicForm node editor

**Steps:**
1. Locate the description text in the Rules section: "Run only when..."
2. Inspect the element and verify CSS class is `text-zinc-600`
3. Measure contrast ratio using DevTools
4. Verify computed color is #52525b

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on white background
- Text color is #52525b
- CSS class is `text-zinc-600`

**Priority:** HIGH

---

### TC-CONTRAST-06: LogicForm action description contrast (white background)

**Preconditions:**
- Navigate to workflow canvas
- Open LogicForm node editor

**Steps:**
1. Locate the description text in the Action section: "What the system does..."
2. Inspect the element and verify CSS class is `text-zinc-600`
3. Measure contrast ratio using DevTools
4. Verify computed color is #52525b

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on white background
- Text color is #52525b
- CSS class is `text-zinc-600`

**Priority:** HIGH

---

### TC-CONTRAST-07: Contrast on light gray background (#fafafa)

**Preconditions:**
- Navigate to workflow canvas
- Open any node editor (HttpCallForm or LogicForm)

**Steps:**
1. Verify if any description text appears on #fafafa background
2. If yes, measure contrast ratio for text-zinc-600 (#52525b) on #fafafa
3. Use WebAIM Contrast Checker to verify: https://webaim.org/resources/contrastchecker/?fcolor=52525B&bcolor=FAFAFA

**Expected Result:**
- Contrast ratio ≥ 4.5:1 on #fafafa background
- Calculated ratio should be approximately 6.7:1

**Priority:** HIGH

---

### TC-CONTRAST-08: Axe DevTools accessibility scan

**Preconditions:**
- Install Axe DevTools browser extension
- Navigate to workflow canvas

**Steps:**
1. Open HttpCallForm node editor
2. Run Axe DevTools scan on the drawer element
3. Filter results for "color-contrast" violations
4. Verify zero violations for description text elements
5. Repeat for LogicForm node editor

**Expected Result:**
- Zero color contrast violations reported by Axe DevTools
- All description text elements pass WCAG 2.1 Level AA

**Priority:** HIGH

---

### TC-CONTRAST-09: Visual consistency check (desktop)

**Preconditions:**
- Desktop viewport (1280px width)
- Navigate to workflow canvas

**Steps:**
1. Open HttpCallForm node editor
2. Visually compare description text with other secondary text in the UI
3. Verify text still appears as helper/secondary content (not primary)
4. Check that text is readable but not overly prominent
5. Repeat for LogicForm node editor

**Expected Result:**
- Description text maintains visual hierarchy as secondary content
- Text is clearly readable
- No visual regression compared to other secondary text in the application

**Priority:** MEDIUM

---

### TC-CONTRAST-10: Visual consistency check (mobile)

**Preconditions:**
- Mobile viewport (390px width)
- Navigate to workflow canvas

**Steps:**
1. Open HttpCallForm node editor on mobile
2. Verify description text is readable on smaller screen
3. Check that contrast improvement doesn't make text too prominent
4. Verify text wrapping and spacing remain correct
5. Repeat for LogicForm node editor

**Expected Result:**
- Description text is readable on mobile viewport
- Visual hierarchy maintained
- No layout issues or text overflow

**Priority:** MEDIUM

---

### TC-CONTRAST-11: No regression in other node editor elements

**Preconditions:**
- Navigate to workflow canvas
- Open any node editor

**Steps:**
1. Verify that only description text color has changed
2. Check that labels, input fields, buttons remain unchanged
3. Verify section headers (Description, Rules, Action) are unchanged
4. Check that other text elements (placeholders, error messages) are unchanged

**Expected Result:**
- Only the 3 description paragraphs have changed color
- All other elements maintain their original styling
- No unintended style changes

**Priority:** HIGH

---

### TC-CONTRAST-12: Cross-browser compatibility

**Preconditions:**
- Test in Chrome, Firefox, and Safari
- Navigate to workflow canvas

**Steps:**
1. Open HttpCallForm node editor in each browser
2. Verify text-zinc-600 renders consistently as #52525b
3. Measure contrast ratio in each browser's DevTools
4. Verify visual appearance is consistent

**Expected Result:**
- Color renders identically across all browsers
- Contrast ratio ≥ 4.5:1 in all browsers
- No browser-specific rendering issues

**Priority:** MEDIUM

---

## Test Data

**Test Application:** Any existing workflow application  
**Test Nodes:** 
- HTTP Call node (uses HttpCallForm)
- Logic node (uses LogicForm)

## Acceptance Criteria Mapping

| Test Case | Acceptance Criteria |
|-----------|---------------------|
| TC-CONTRAST-01, TC-CONTRAST-04 | AC-CV-25.1 (node name description) |
| TC-CONTRAST-02, TC-CONTRAST-05 | AC-CV-25.1 (rules description) |
| TC-CONTRAST-03, TC-CONTRAST-06 | AC-CV-25.1 (action description) |
| TC-CONTRAST-07 | AC-CV-25.2 (contrast on both backgrounds) |
| TC-CONTRAST-09, TC-CONTRAST-10 | AC-CV-25.3 (visual consistency) |
| TC-CONTRAST-11 | AC-CV-25.4 (no other changes) |
| TC-CONTRAST-08 | All AC (automated validation) |

## Test Execution Notes

- All contrast measurements should be performed using browser DevTools or WebAIM Contrast Checker
- Axe DevTools scan is mandatory for final validation
- Visual consistency checks require manual review by QA
- Test on both desktop and mobile viewports

## Known Limitations

- Automated tools may not catch all visual consistency issues
- Manual review required for subjective visual hierarchy assessment
- Contrast ratios may vary slightly between measurement tools (acceptable variance: ±0.1)

## References

- WCAG 2.1 Level AA: https://www.w3.org/WAI/WCAG21/quickref/#contrast-minimum
- WebAIM Contrast Checker: https://webaim.org/resources/contrastchecker/
- Axe DevTools: https://www.deque.com/axe/devtools/
