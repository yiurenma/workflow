# Test Cases — Workflow Studio Unified Program Part 3: Node Editor Enhancements (v32.0)

**Document version:** 32.0 Part 3  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Node editor rule key validation + resizable drawer + read-first mode  
**Test environment:** UAT (https://workflow-ui-gamma.vercel.app)

---

## 1. Test Strategy

### 1.1 Scope

- **Rule key validation:** Client-side JSONPath validation on blur + before save
- **Resizable drawer:** Desktop drag handle + width persistence
- **Read-only view:** Default view mode with Edit toggle
- **Long content:** Scrolling + reflow validation

### 1.2 Test Levels

- **Unit:** JSONPath validation function
- **Integration:** Not applicable
- **E2E:** Playwright tests for drawer interactions

### 1.3 Test Data

- **Valid JSONPath:** `$.data.userId`, `$.items[0].name`, `$..price`
- **Invalid JSONPath:** `$.data.userId, $.data.userName` (comma), `not a path`, `$.data.[invalid`
- **Long content:** JSON with >100 lines, descriptions with >500 characters

---

## 2. Test Cases

### 2.1 Rule Key Validation

#### TC-RULE-KEY-01: Valid JSONPath accepted

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor for any node

**Steps:**
1. Click Edit button
2. Enter valid JSONPath in rule key field: `$.data.userId`
3. Blur field (click outside)
4. Verify no error message
5. Click Save button
6. Verify save succeeds

**Expected Results:**
- No red border on field
- No error message below field
- Save button enabled
- Node saved successfully

---

#### TC-RULE-KEY-02: Invalid JSONPath rejected (comma-separated)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor for any node

**Steps:**
1. Click Edit button
2. Enter invalid JSONPath in rule key field: `$.data.userId, $.data.userName`
3. Blur field (click outside)
4. Verify error message appears

**Expected Results:**
- Red border on field
- Error message: "Rule key must be a single JSONPath expression (no comma-separated paths)"
- Save button disabled

---

#### TC-RULE-KEY-03: Invalid JSONPath rejected (syntax error)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor for any node

**Steps:**
1. Click Edit button
2. Enter invalid JSONPath in rule key field: `$.data.[invalid`
3. Blur field (click outside)
4. Verify error message appears

**Expected Results:**
- Red border on field
- Error message: "Invalid JSONPath syntax: [error details]"
- Save button disabled

---

#### TC-RULE-KEY-04: Empty rule key allowed

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor for any node

**Steps:**
1. Click Edit button
2. Clear rule key field (leave empty)
3. Blur field (click outside)
4. Verify no error message
5. Click Save button
6. Verify save succeeds

**Expected Results:**
- No red border on field
- No error message
- Save button enabled
- Node saved successfully with empty rule key

---

#### TC-RULE-KEY-05: Validation on save attempt

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor for any node

**Steps:**
1. Click Edit button
2. Enter invalid JSONPath in rule key field: `not a path`
3. Click Save button without blurring field

**Expected Results:**
- Validation triggered on save attempt
- Error message appears
- Save blocked
- Field highlighted with red border

---

### 2.2 Resizable Drawer

#### TC-RESIZE-01: Drag handle widens drawer

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor

**Steps:**
1. Locate drag handle (left edge of drawer)
2. Verify cursor changes to `col-resize` on hover
3. Drag handle left (toward canvas) by 200px
4. Release mouse
5. Measure drawer width

**Expected Results:**
- Drawer width increases by ~200px
- Content reflows to fill new width
- Canvas shrinks accordingly
- Drag handle remains visible

**Playwright assertion example:**
```typescript
const drawer = page.locator('.ant-drawer');
const initialBox = await drawer.boundingBox();

const handle = drawer.locator('.resize-handle');
await handle.hover();
await expect(handle).toHaveCSS('cursor', 'col-resize');

await handle.dragTo(handle, { targetPosition: { x: -200, y: 0 } });

const newBox = await drawer.boundingBox();
expect(newBox.width).toBeGreaterThan(initialBox.width + 150); // Allow tolerance
```

---

#### TC-RESIZE-02: Min/max width constraints

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor

**Steps:**
1. Drag handle far left (attempt to exceed max width)
2. Verify drawer stops at max width (e.g., 80vw)
3. Drag handle far right (attempt to go below min width)
4. Verify drawer stops at min width (e.g., 400px)

**Expected Results:**
- Drawer width clamped to max 80vw (~1024px on 1280px viewport)
- Drawer width clamped to min 400px
- No visual glitches or overflow

---

#### TC-RESIZE-03: Width persists across sessions

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor

**Steps:**
1. Resize drawer to 600px width
2. Close drawer
3. Refresh page
4. Open node editor again
5. Measure drawer width

**Expected Results:**
- Drawer opens at 600px width (persisted from step 1)
- Width stored in `localStorage` key `workflow_drawer_width`

---

#### TC-RESIZE-04: Mobile drawer (deferred or touch drag)

**Priority:** LOW  
**Type:** Manual  
**Viewport:** Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor (bottom drawer on mobile)

**Steps:**
1. Attempt to resize drawer (if touch drag implemented)
2. OR verify drawer uses fixed height (if deferred)

**Expected Results:**
- If implemented: Touch drag handle works, height adjusts
- If deferred: Drawer uses fixed height (e.g., 60vh), no resize handle

---

### 2.3 Read-Only View Mode

#### TC-VIEW-MODE-01: Drawer opens in read-only view

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`

**Steps:**
1. Click on any node
2. Wait for drawer to open
3. Verify drawer shows read-only view (no input fields)

**Expected Results:**
- **Description:** Formatted text (not textarea)
- **Rules:** Pretty-printed JSON or structured list (not input fields)
- **Action:** Pretty-printed JSON or structured display (not input fields)
- **Edit button:** Visible at top of drawer

**Playwright assertion example:**
```typescript
await page.locator('.react-flow__node').first().click();
const drawer = page.locator('.ant-drawer');
await expect(drawer).toBeVisible();

// Verify read-only mode (no textareas/inputs)
await expect(drawer.locator('textarea')).toHaveCount(0);
await expect(drawer.locator('input[type="text"]')).toHaveCount(0);

// Verify Edit button present
await expect(drawer.locator('button:has-text("Edit")')).toBeVisible();
```

---

#### TC-VIEW-MODE-02: Switch to edit mode

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor (read-only view)

**Steps:**
1. Click Edit button
2. Verify drawer switches to edit mode

**Expected Results:**
- **Description:** Textarea visible with current value
- **Rules:** Input fields visible for rule configuration
- **Action:** Input fields visible for action configuration
- **Done button:** Visible at top of drawer
- **Cancel button:** Visible at top of drawer

**Playwright assertion example:**
```typescript
await page.locator('button:has-text("Edit")').click();

const drawer = page.locator('.ant-drawer');
await expect(drawer.locator('textarea')).toBeVisible(); // Description field
await expect(drawer.locator('button:has-text("Done")')).toBeVisible();
await expect(drawer.locator('button:has-text("Cancel")')).toBeVisible();
```

---

#### TC-VIEW-MODE-03: Save changes (Done button)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor, switch to edit mode

**Steps:**
1. Modify description field: "Updated description"
2. Click Done button
3. Verify drawer returns to read-only view
4. Verify changes saved

**Expected Results:**
- Drawer switches back to read-only view
- Description shows "Updated description"
- Node on canvas reflects changes (if description shown on node)

---

#### TC-VIEW-MODE-04: Discard changes (Cancel button)

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node editor, switch to edit mode

**Steps:**
1. Modify description field: "Temporary change"
2. Click Cancel button
3. Verify drawer returns to read-only view
4. Verify changes discarded

**Expected Results:**
- Drawer switches back to read-only view
- Description shows original value (not "Temporary change")
- No changes saved to node

---

### 2.4 Long Content Handling

#### TC-LONG-CONTENT-01: Scrolling in read-only view

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node with long JSON action (>100 lines)

**Steps:**
1. Verify drawer body scrollable
2. Scroll to bottom of content
3. Verify all content visible (no truncation)

**Expected Results:**
- Drawer body has `overflow-y: auto`
- Scrollbar visible when content exceeds viewport
- All content accessible via scroll

---

#### TC-LONG-CONTENT-02: Reflow on resize

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Open node with long description or JSON

**Steps:**
1. Resize drawer to narrow width (400px)
2. Verify content reflows (no horizontal scroll)
3. Resize drawer to wide width (800px)
4. Verify content reflows to fill width

**Expected Results:**
- Content uses `word-break: break-word` or `white-space: pre-wrap`
- No horizontal scrollbar
- Content readable at all widths

---

### 2.5 Regression Tests

#### TC-NODE-REGRESSION-01: Existing drawer tests still pass

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- All node editor changes merged

**Steps:**
1. Run full Playwright suite: `npx playwright test`
2. Review results

**Expected Results:**
- Pass rate ≥82/84 (baseline from v28.0)
- No new failures introduced by node editor changes
- Existing drawer tests (open, close, save) still pass

---

## 3. Test Execution Plan

### 3.1 Phase 1: Rule Key Validation

1. Implement validation function
2. Manual test TC-RULE-KEY-01/02/03/04/05
3. Implement Playwright tests
4. Run full suite; verify no regressions

### 3.2 Phase 2: Resizable Drawer

1. Implement drag handle + resize logic
2. Manual test TC-RESIZE-01/02/03
3. Implement Playwright tests (TC-RESIZE-01/02/03)
4. Run full suite; verify no regressions

### 3.3 Phase 3: Read-Only View Mode

1. Implement view/edit mode toggle
2. Manual test TC-VIEW-MODE-01/02/03/04
3. Implement Playwright tests
4. Run full suite; verify no regressions

### 3.4 Phase 4: Integration Testing

1. Test all features together (resize + view mode + validation)
2. Test long content (TC-LONG-CONTENT-01/02)
3. Run full E2E suite (TC-NODE-REGRESSION-01)
4. Document results in `ui-test-report-v32.0-part3.md`

---

## 4. Exit Criteria

- TC-RULE-KEY-01/02/03/04/05 pass on Desktop
- TC-RESIZE-01/02/03 pass on Desktop
- TC-VIEW-MODE-01/02/03/04 pass on Desktop + Mobile
- TC-LONG-CONTENT-01/02 pass on Desktop
- Full E2E suite pass rate ≥82/84 (no regressions)
- `ui-test-report-v32.0-part3.md` documents coverage

---

## 5. References

- JSONPath spec: https://goessner.net/articles/JsonPath/
- `jsonpath-plus`: https://www.npmjs.com/package/jsonpath-plus
- `arch-doc-v32.0-part3-node-editor.md` — Technical design
- `pm-doc-v32.0-part3-node-editor.md` — Requirements
