# Test Document v29.0 — Node Editor Drawer Close Button Fix

**Document version:** 29.0  
**Date:** 2026-04-12  
**Status:** Draft  
**TODO label:** `TODO-uat-e2e-node-drawer-close-button-broken`

## Test Cases

### TC-DRAWER-CLOSE-01: Desktop - Close button closes drawer

**Preconditions:**
- Navigate to workflow canvas with at least one node
- Desktop Chrome 1280px viewport

**Steps:**
1. Click on a node to open the node editor drawer
2. Verify drawer is visible
3. Click the close button (`.ant-drawer-close`)

**Expected Result:**
- Drawer closes immediately
- Drawer is no longer visible in viewport
- Canvas is interactive (can select other nodes)

**Acceptance Criteria:** AC-29.1, AC-29.3

---

### TC-DRAWER-CLOSE-02: Mobile - Close button closes drawer

**Preconditions:**
- Navigate to workflow canvas with at least one node
- Mobile Chrome 390×844 viewport

**Steps:**
1. Tap on a node to open the node editor drawer
2. Verify drawer is visible
3. Tap the close button (`.ant-drawer-close`)

**Expected Result:**
- Drawer closes immediately
- Drawer is no longer visible in viewport
- Canvas is interactive (can select other nodes)

**Acceptance Criteria:** AC-29.2, AC-29.3

---

### TC-DRAWER-CLOSE-03: Close button removes drawer from DOM

**Preconditions:**
- Navigate to workflow canvas with at least one node
- Any viewport

**Steps:**
1. Click/tap on a node to open the node editor drawer
2. Click/tap the close button (`.ant-drawer-close`)
3. Inspect DOM for drawer element

**Expected Result:**
- Drawer element is either removed from DOM or has `display: none` / `visibility: hidden`
- No drawer overlay remains visible

**Acceptance Criteria:** AC-29.3

---

### TC-DRAWER-CLOSE-04: Canvas remains interactive after close

**Preconditions:**
- Navigate to workflow canvas with multiple nodes
- Any viewport

**Steps:**
1. Click/tap on node A to open drawer
2. Click/tap close button
3. Click/tap on node B

**Expected Result:**
- Node B's drawer opens successfully
- No errors in console
- Canvas responds to interactions

**Acceptance Criteria:** AC-29.3

---

### TC-DRAWER-CLOSE-05: Close button effect (Layer 5 validation)

**Preconditions:**
- Navigate to workflow canvas with at least one node
- Desktop Chrome 1280px viewport

**Steps:**
1. Click on a node to open drawer
2. Get computed style of drawer element
3. Click close button
4. Wait for animation to complete
5. Check drawer visibility state

**Expected Result:**
- Drawer element has `opacity: 0` or is removed from DOM
- No drawer-related elements remain visible
- Canvas z-index is higher than any drawer remnants

**Acceptance Criteria:** AC-29.1, AC-29.3

---

## Playwright E2E Test Specification

Update `canvas.spec.ts` or create new spec file:

```typescript
test('TC-DRAWER-CLOSE-01: Desktop close button closes drawer', async ({ page }) => {
  await page.goto('/workflows/E2E_TEST_CANVAS');
  
  // Open drawer by clicking a node
  await page.locator('.react-flow__node').first().click();
  await expect(page.locator('.ant-drawer')).toBeVisible();
  
  // Click close button
  await page.locator('.ant-drawer-close').click();
  
  // Verify drawer is closed
  await expect(page.locator('.ant-drawer')).not.toBeVisible();
  
  // Verify canvas is interactive
  await page.locator('.react-flow__node').nth(1).click();
  await expect(page.locator('.ant-drawer')).toBeVisible();
});
```

## Manual Testing Checklist

- [ ] Desktop Chrome 1280px: close button works
- [ ] Mobile Chrome 390×844: close button works
- [ ] Drawer removed from DOM or hidden after close
- [ ] Canvas interactive after close
- [ ] No console errors
- [ ] No visual glitches or animation issues
- [ ] ESC key still works (regression check)
- [ ] Click outside still works (regression check)

## Regression Testing

Verify existing drawer behaviors still work:
- ESC key closes drawer
- Click outside drawer closes drawer
- Drawer opens when node is selected
- Drawer content displays correctly
