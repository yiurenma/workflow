# Architecture Document v29.0 — Node Editor Drawer Close Button Fix

**Document version:** 29.0  
**Date:** 2026-04-12  
**Status:** Draft  
**TODO label:** `TODO-uat-e2e-node-drawer-close-button-broken`

## Technical Context

The node editor drawer is implemented using Ant Design's `<Drawer>` component in `workflow-ui`. The drawer displays node configuration details when a user selects a node on the canvas.

## Current Implementation

Location: `workflow-ui/src/components/WorkflowDrawer.tsx` (or similar)

The drawer likely uses:
```tsx
<Drawer
  open={isOpen}
  onClose={handleClose}
  closable={true}
  // other props
>
  {/* drawer content */}
</Drawer>
```

## Root Cause Analysis

The close button click is not triggering the `onClose` handler. Possible causes:

1. **Event handler not properly bound:** The `onClose` prop may not be connected to the state update function
2. **Event propagation issue:** A parent element may be calling `stopPropagation()` on click events
3. **State management issue:** The `open` state may not be updating correctly
4. **Portal/z-index issue:** An overlay may be blocking the close button's hit area
5. **ReactFlow interference:** The canvas library may be capturing pointer events globally

## Proposed Solution

### Step 1: Verify Event Handler Binding

Ensure `onClose` prop is properly connected:
```tsx
const [drawerOpen, setDrawerOpen] = useState(false);

<Drawer
  open={drawerOpen}
  onClose={() => setDrawerOpen(false)}
  closable={true}
>
```

### Step 2: Check Event Propagation

Review any click handlers on drawer content or parent elements that might call `stopPropagation()`.

### Step 3: Verify Ant Design Props

Ensure correct Ant Design Drawer configuration:
- `closable={true}` (default, but explicit is better)
- `maskClosable={true}` for click-outside behavior
- `keyboard={true}` for ESC key support

### Step 4: Test Across Viewports

Verify fix works on:
- Desktop Chrome 1280px
- Mobile Chrome 390×844

## API Changes

None required. This is a frontend-only fix.

## Testing Strategy

1. Manual testing: Click close button on desktop and mobile
2. Playwright E2E: Update or create test case to verify close button effect
3. Verify drawer is removed from DOM or hidden after close
4. Verify canvas remains interactive after close

## Risks

- Low risk change, isolated to drawer component
- May need to adjust event handling if ReactFlow is interfering
- Should not affect other drawer behaviors (ESC, click outside)
