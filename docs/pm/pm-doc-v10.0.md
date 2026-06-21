# PM Doc v10.0 — Mobile: Add-Application FAB Draggable

**Feature:** `TODO-mobile-add-application-fab-draggable`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Problem Statement

On mobile, the "New application" (+) button is a Fixed Action Button (FAB) pinned to `bottom: 24px, right: 24px`. When a user is on a paginated list and the Prev/Next buttons or other controls are near the bottom-right of the screen, the FAB overlaps them. The user cannot move the FAB out of the way.

---

## User Stories

### US-40 — Draggable FAB on Mobile Applications List

**As a** mobile user on the Applications page,  
**I want** to drag the + FAB to a different corner of the screen,  
**So that** it does not cover Pagination, Next, or other controls I need to tap.

**Acceptance Criteria:**
- AC-40-1: On mobile, the FAB can be dragged (touch/pointer) to reposition it anywhere within the safe area of the viewport.
- AC-40-2: A short tap (no or minimal drag) still opens the "New application" dialog.
- AC-40-3: The FAB snaps to the nearest edge (left or right) when released, at the vertical position where it was dropped.
- AC-40-4: The last position is stored in `localStorage` and restored on next visit.
- AC-40-5: Desktop layout is NOT affected (FAB is not shown on desktop).
- AC-40-6: Canvas page is NOT affected.

---

*Status: Draft*
