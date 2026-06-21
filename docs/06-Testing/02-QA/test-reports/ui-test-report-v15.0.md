# UI Test Report v15.0 — Canvas Mobile FAB Draggable

**Date:** 2026-04-08
**Label:** `TODO-mobile-canvas-add-node-fab-draggable`

## Summary

| Result | Count |
|--------|-------|
| PASS   | 5     |
| SKIP   | 0     |
| FAIL   | 0     |

## Test Results

| TC ID | Description | Viewport | Result | Notes |
|-------|-------------|----------|--------|-------|
| TC-CANVAS-MOB-01 | FAB visible on mobile canvas | Mobile | PASS | `aria-label="Add node"` button visible |
| TC-CANVAS-MOB-02 | Tap FAB opens Add Node sheet | Mobile | PASS | `.ant-drawer` visible after tap |
| TC-CANVAS-MOB-03 | Drag FAB does not open sheet | Mobile | PASS | No `.ant-drawer-open` after drag > 5px |
| TC-CANVAS-MOB-04 | Position persisted in localStorage | Mobile | PASS | `workflow_canvas_fab_pos` key set with `{x, y}` |
| TC-CANVAS-MOB-05 | FAB not rendered on desktop | Desktop | PASS | Button not visible on 1280px viewport |

## Change Implemented

`MobileAddNodeSheet.tsx` — replaced static `className="fixed bottom-20 left-4"` with:
- `pos` state (loaded from `localStorage` key `workflow_canvas_fab_pos`, default bottom-left)
- Pointer events: `onPointerDown` / `onPointerMove` / `onPointerUp` with pointer capture
- Drag threshold: < 5px movement → treat as tap → open sheet; ≥ 5px → drag
- On release: snap to nearest screen edge at dragged Y, persist to `localStorage`
- `touchAction: "none"` on button to suppress browser scroll interference

New spec: `e2e/canvas-mobile.spec.ts`

## TypeScript

`npx tsc --noEmit` — clean, 0 errors.
