# PM Doc v15.0 — Canvas Mobile: Draggable Add-Node FAB

**Status:** Awaiting approval
**Label:** `TODO-mobile-canvas-add-node-fab-draggable`
**Date:** 2026-04-08

---

## Problem Statement

On the workflow editor canvas in mobile/phone layout, the floating **+** ("Add node") button
in `MobileAddNodeSheet.tsx` is fixed to `bottom-20 left-4`. Users cannot move it if it
overlaps nodes or controls they need to access. The applications list already has a draggable
FAB with edge-snap and localStorage persistence (`workflow_fab_pos`); the canvas FAB should
have the same interaction model.

---

## User Story

As a **mobile user editing a workflow canvas**,
I want to drag the + FAB to a comfortable position (with it snapping to the nearest edge),
So that I can always reach the canvas controls without the button obscuring my work.

---

## Acceptance Criteria

1. The + FAB on the canvas mobile layout is **draggable** via pointer events.
2. On **release**, the button **snaps to the nearest screen edge** (left or right) at the dragged Y position, with a minimum margin from screen edges.
3. The position is **persisted** in `localStorage` under key `workflow_canvas_fab_pos` and **restored** on remount.
4. A **tap without meaningful drag** (pointer moved < 5 px) still **opens the Add Node sheet** — navigation is not broken.
5. The behaviour is **desktop-unchanged**: the FAB is only rendered on mobile (already gated by `isMobile` in `WorkflowEditor`).
6. No changes to the Add Node sheet functionality or the node insertion logic.
