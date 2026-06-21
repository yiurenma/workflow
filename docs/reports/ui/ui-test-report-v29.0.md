# UI Test Report v29.0 — Node Editor Drawer Close Button Fix

**Document version:** 29.0  
**Date:** 2026-04-12  
**Status:** PASS  
**TODO label:** `TODO-uat-e2e-node-drawer-close-button-broken`

## Test Summary

All test cases passed. The drawer close button now works correctly on both desktop and mobile viewports.

## Test Environment

- **Local:** http://localhost:5173
- **Browser:** Chrome 131
- **Viewports:** Desktop (1280px), Mobile (390×844)

## Test Results

### TC-DRAWER-CLOSE-01: Desktop - Close button closes drawer ✅ PASS

**Steps:**
1. Navigated to workflow canvas with test nodes
2. Clicked on a node to open drawer
3. Verified drawer visible
4. Clicked close button (`.ant-drawer-close`)

**Result:** Drawer closed immediately, canvas remained interactive

---

### TC-DRAWER-CLOSE-02: Mobile - Close button closes drawer ✅ PASS

**Steps:**
1. Resized viewport to 390×844
2. Tapped on a node to open drawer
3. Verified drawer visible (bottom placement)
4. Tapped close button

**Result:** Drawer closed immediately, canvas remained interactive

---

### TC-DRAWER-CLOSE-03: Close button removes drawer from DOM ✅ PASS

**Steps:**
1. Opened drawer by clicking node
2. Clicked close button
3. Inspected DOM for drawer element

**Result:** `.ant-drawer-content-wrapper` has `display: none` after close

---

### TC-DRAWER-CLOSE-04: Canvas remains interactive after close ✅ PASS

**Steps:**
1. Clicked node A to open drawer
2. Clicked close button
3. Clicked node B

**Result:** Node B's drawer opened successfully, no console errors

---

### TC-DRAWER-CLOSE-05: Close button effect (Layer 5 validation) ✅ PASS

**Steps:**
1. Opened drawer
2. Got computed style of drawer
3. Clicked close button
4. Waited for animation
5. Checked visibility state

**Result:** Drawer opacity transitions to 0, element hidden from viewport

---

## Regression Testing

Verified existing drawer behaviors still work:

- ✅ ESC key closes drawer (desktop)
- ✅ Click outside drawer closes drawer
- ✅ Drawer opens when node selected
- ✅ Drawer content displays correctly
- ✅ Form fields editable
- ✅ Save button works

## Console Errors

No errors or warnings in console.

## Accessibility

- ✅ Close button has `aria-label="Close"`
- ✅ Close button meets 44×44px touch target size
- ✅ Keyboard navigation works (Tab to button, Enter to close)

## Code Changes

**File:** `workflow-ui/src/routes/workflows/-components/workflow-drawer/index.tsx`

**Changes:**
1. Removed `e.stopPropagation()` from close button onClick handler
2. Added `className="ant-drawer-close"` to custom close button for test selector compatibility
3. Simplified onClick to call `onClose()` directly

**Root Cause:** The `e.stopPropagation()` was preventing the click event from properly triggering the drawer close behavior.

## Conclusion

All test cases passed. The drawer close button fix is ready for UAT on the gamma environment.
