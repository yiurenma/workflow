# Test Doc v10.0 — Mobile: Add-Application FAB Draggable

**Feature:** `TODO-mobile-add-application-fab-draggable`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Source Checks

| TC-ID | Check | Expected |
|-------|-------|----------|
| TC-40-L01 | `onPointerDown`, `onPointerMove`, `onPointerUp` handlers on FAB button | All three present |
| TC-40-L02 | `setPointerCapture` called in `onPointerDown` | `e.currentTarget.setPointerCapture(e.pointerId)` present |
| TC-40-L03 | `isDragging` ref used to distinguish tap from drag | `useRef(false)` + threshold check |
| TC-40-L04 | FAB position stored in `localStorage` on drag end | `localStorage.setItem(FAB_POS_KEY, ...)` present |
| TC-40-L05 | FAB position loaded from `localStorage` on init | `useState(loadFabPos)` present |
| TC-40-L06 | FAB has `touch-none` CSS class | `touch-none` in className |
| TC-40-L07 | Desktop layout unchanged | `style` position only applied inside `{isMobile && ...}` block |

---

## Browser UAT (human executes on phone)

| TC-ID | Step | What to check |
|-------|------|---------------|
| TC-40-B01 | Open `/workflows` on phone | FAB appears at bottom-right |
| TC-40-B02 | Tap FAB (short tap, no drag) | "New application" dialog opens |
| TC-40-B03 | Cancel dialog; drag FAB to upper-left area | FAB follows finger; snaps to left edge when released |
| TC-40-B04 | Reload page | FAB reappears at the saved position |
| TC-40-B05 | Drag FAB to right side | FAB snaps to right edge |
| TC-40-B06 | Tap FAB from new position | Dialog still opens normally |
| TC-40-B07 | Open canvas | Canvas pan/zoom/drag unaffected |

---

## Pass Criteria

All TC-40-L01..L07 and TC-40-B01..B07 PASS.

---

*Status: Draft*
