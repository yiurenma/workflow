# Test Document v31.0

**Document version:** 31.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-node-editor-draggable-width-readonly-default`

## Test Scope

Test resizable drawer width, read-first mode, and edit gate functionality in node editor.

## Test Cases

### TC-NODE-WIDTH-01: Drawer width is resizable on desktop

**Preconditions:**
- Desktop Chrome 1280px viewport
- Navigate to workflow canvas with at least one node

**Steps:**
1. Click on a node to open drawer
2. Locate resize handle on left edge of drawer
3. Drag handle left (to widen drawer)
4. Release mouse

**Expected Result:**
- Drawer width increases
- Minimum width: 320px
- Maximum width: 80% viewport width
- Content reflows to fit new width

**Acceptance Criteria:** AC-31.1

---

### TC-NODE-WIDTH-02: Drawer width persists in localStorage

**Preconditions:**
- Desktop Chrome 1280px viewport

**Steps:**
1. Open node drawer and resize to 600px
2. Close drawer
3. Refresh page
4. Open node drawer again

**Expected Result:**
- Drawer opens at 600px width (persisted value)

**Acceptance Criteria:** AC-31.1

---

### TC-NODE-VIEW-01: Drawer opens in read-only mode by default

**Preconditions:**
- Any viewport
- Navigate to workflow canvas with at least one node

**Steps:**
1. Click on a node to open drawer

**Expected Result:**
- Drawer displays read-only view
- Description shown as formatted text (not textarea)
- Rules shown as pretty-printed JSON (not textarea)
- Action shown as pretty-printed JSON (not textarea)
- "Edit" button visible at top

**Acceptance Criteria:** AC-31.2

---

### TC-NODE-VIEW-02: Edit button switches to edit mode

**Preconditions:**
- Drawer open in read-only mode

**Steps:**
1. Click "Edit" button

**Expected Result:**
- Drawer switches to edit mode
- Description shown as textarea
- Rules shown as editable textarea
- Action shown as editable form fields
- "Done" and "Cancel" buttons visible

**Acceptance Criteria:** AC-31.3

---

### TC-NODE-EDIT-01: Done button saves changes

**Preconditions:**
- Drawer open in edit mode

**Steps:**
1. Edit description field to "Updated description"
2. Click "Done" button

**Expected Result:**
- Changes saved to node
- Drawer switches back to read-only mode
- Read-only view shows "Updated description"

**Acceptance Criteria:** AC-31.3

---

### TC-NODE-EDIT-02: Cancel button discards changes

**Preconditions:**
- Drawer open in edit mode

**Steps:**
1. Edit description field to "Temporary change"
2. Click "Cancel" button

**Expected Result:**
- Changes discarded
- Drawer switches back to read-only mode
- Read-only view shows original description (not "Temporary change")

**Acceptance Criteria:** AC-31.3

---

### TC-NODE-REFLOW-01: Content reflows on drawer resize

**Preconditions:**
- Desktop Chrome 1280px viewport
- Node with long description text

**Steps:**
1. Open node drawer
2. Resize drawer to narrow width (320px)
3. Observe description text
4. Resize drawer to wide width (800px)
5. Observe description text

**Expected Result:**
- At 320px: text wraps to multiple lines
- At 800px: text uses available width, fewer lines
- No horizontal scrolling required
- No text truncation

**Acceptance Criteria:** AC-31.4

---

## Playwright E2E Test Specification

```typescript
test('TC-NODE-WIDTH-01: Drawer width is resizable', async ({ page }) => {
  await page.goto('/workflows/E2E_TEST_CANVAS');
  
  const node = page.locator('.react-flow__node').first();
  await node.click();
  
  const drawer = page.locator('.ant-drawer');
  await expect(drawer).toBeVisible();
  
  const initialBox = await drawer.boundingBox();
  const initialWidth = initialBox!.width;
  
  // Find resize handle and drag
  const handle = drawer.locator('.resize-handle');
  await handle.hover();
  await page.mouse.down();
  await page.mouse.move(initialBox!.x - 100, initialBox!.y + 100);
  await page.mouse.up();
  
  const newBox = await drawer.boundingBox();
  expect(newBox!.width).toBeGreaterThan(initialWidth);
});

test('TC-NODE-VIEW-01: Drawer opens in read-only mode', async ({ page }) => {
  await page.goto('/workflows/E2E_TEST_CANVAS');
  
  const node = page.locator('.react-flow__node').first();
  await node.click();
  
  const drawer = page.locator('.ant-drawer');
  await expect(drawer).toBeVisible();
  
  // Verify read-only mode
  await expect(drawer.getByRole('button', { name: 'Edit' })).toBeVisible();
  await expect(drawer.locator('textarea')).toHaveCount(0);
});

test('TC-NODE-EDIT-01: Edit button switches to edit mode', async ({ page }) => {
  await page.goto('/workflows/E2E_TEST_CANVAS');
  
  const node = page.locator('.react-flow__node').first();
  await node.click();
  
  const drawer = page.locator('.ant-drawer');
  await drawer.getByRole('button', { name: 'Edit' }).click();
  
  // Verify edit mode
  await expect(drawer.getByRole('button', { name: 'Done' })).toBeVisible();
  await expect(drawer.getByRole('button', { name: 'Cancel' })).toBeVisible();
  await expect(drawer.locator('textarea').first()).toBeVisible();
});
```

## Manual Testing Checklist

- [ ] Desktop: resize handle visible and draggable
- [ ] Desktop: drawer width persists after refresh
- [ ] Desktop: minimum width enforced (320px)
- [ ] Desktop: maximum width enforced (80% viewport)
- [ ] Mobile: drawer behavior unchanged (or touch drag works)
- [ ] Read-only mode: no textareas visible
- [ ] Read-only mode: JSON pretty-printed
- [ ] Edit button: switches to edit mode
- [ ] Done button: saves changes
- [ ] Cancel button: discards changes
- [ ] Content reflows on resize
- [ ] No horizontal scrolling in narrow drawer

## Success Criteria

- ✅ All test cases pass
- ✅ No regressions in existing node editor functionality
- ✅ Drawer width adjustable and persistent
- ✅ Read-only mode displays formatted content
- ✅ Edit/Done/Cancel workflow works correctly
