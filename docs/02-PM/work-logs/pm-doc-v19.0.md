# PM Doc v19.0 — Canvas: Straight-Line Layout Recovery

**Status:** Awaiting approval
**Label:** `TODO-canvas-straight-line-workflow-recovery`
**Date:** 2026-04-08

---

## Problem Statement

After editing a complex workflow, nodes become scattered or overlapping on the canvas. Users feel "lost" and have no quick way to restore a readable view. There is currently no layout-reset button.

## User Story

As a **workflow author** on the canvas,
I want a "Straighten" or "Reset Layout" button that lines all nodes up in a vertical column (top → bottom),
So that I can instantly recover a readable linear view without manually repositioning every node.

## Acceptance Criteria

1. A **"Straighten" button** (or equivalent icon button) is accessible from the canvas — placed inside the React Flow `<Controls>` area or in the canvas toolbar.
2. Clicking it repositions all nodes in a **single vertical column**, spaced evenly, ordered by their current top-to-bottom Y position (preserving relative order).
3. After layout, the canvas calls **`fitView()`** to zoom/pan so all nodes are visible.
4. The action is **undo-able** via the existing React Flow undo mechanism (if present) — or at minimum does not corrupt saved data (only updates in-memory positions; user must still click Save).
5. Works on **both desktop and mobile** viewports.
6. No backend call — purely client-side node repositioning.
