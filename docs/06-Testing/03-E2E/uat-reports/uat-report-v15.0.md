# UAT Report v15.0 — Canvas Mobile FAB Draggable

**Date:** 2026-04-08
**Label:** `TODO-mobile-canvas-add-node-fab-draggable`
**Status:** PASS

---

## UAT Scenarios

| # | Scenario | Viewport | Expected | Result |
|---|----------|----------|----------|--------|
| 1 | FAB visible on canvas | Mobile (390×844) | `aria-label="Add node"` button visible | PASS |
| 2 | Tap FAB opens Add Node sheet | Mobile (390×844) | Ant Design Drawer appears | PASS |
| 3 | Drag > 5px moves FAB, does NOT open sheet | Mobile (390×844) | No drawer after drag | PASS |
| 4 | Released position persisted in localStorage | Mobile (390×844) | `workflow_canvas_fab_pos` key set with `{x,y}` | PASS |
| 5 | FAB not visible on desktop | Desktop (1280×800) | Button absent | PASS |
| 6 | Desktop canvas unchanged (no regression) | Desktop (1280×800) | Canvas, Explain, Save all work | PASS |

## Summary

All 6 scenarios passed. The pointer-event drag implementation with edge-snap and localStorage persistence works correctly. Tap-without-drag (< 5px movement) still opens the Add Node sheet. Desktop layout is unaffected (FAB only rendered when `isMobile` is true).

**TC results:** TC-CANVAS-MOB-01 ✓, TC-CANVAS-MOB-02 ✓, TC-CANVAS-MOB-03 ✓, TC-CANVAS-MOB-04 ✓, TC-CANVAS-MOB-05 ✓
