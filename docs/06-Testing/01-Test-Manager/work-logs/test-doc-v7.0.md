# Test Doc v7.0 — Q2 Mobile: App Card Actions + Desktop View Entry

**Feature:** `TODO-q2-mobile-apps-actions-desktop-entry-constraints`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Source Checks

| TC-ID | Check | Expected |
|-------|-------|----------|
| TC-37-L01 | `EllipsisOutlined` Dropdown rendered on mobile cards | `<Dropdown>` + `EllipsisOutlined` in card JSX |
| TC-37-L02 | Dropdown menu items include Copy and Delete | `items` array has `key: "copy"` and `key: "delete"` |
| TC-37-L03 | Copy opens existing copy modal (`setCopySource`) | onClick calls `setCopySource` |
| TC-37-L04 | Delete shows existing confirm modal | onClick calls `Modal.confirm` |
| TC-37-L05 | `desktopOverride` state + localStorage key present | `localStorage.getItem("workflow_list_desktop_view")` in code |
| TC-37-L06 | "Desktop view" / "Mobile view" toggle button rendered on mobile | toggle button JSX present inside `{isMobile && ...}` |
| TC-37-L07 | `showDesktop` derived from `!isMobile || desktopOverride` | `const showDesktop = ...` expression present |
| TC-37-L08 | All layout branches use `showDesktop` not `isMobile` | `isMobile ?` replaced with `!showDesktop ?` (or equivalent) |
| TC-37-L09 | Canvas route file not modified | git diff shows no changes to `$applicationName.tsx` |

---

## Browser UAT (human executes on mobile or narrow viewport)

| TC-ID | Step | What to check |
|-------|------|---------------|
| TC-37-B01 | Open `/workflows` on phone | Cards render; each card has a ⋯ (ellipsis) button |
| TC-37-B02 | Tap ⋯ on any card | Dropdown appears with Copy, History, Delete options |
| TC-37-B03 | Tap Copy | "Copy workflow" modal opens with source name shown; type a target name and confirm → success message |
| TC-37-B04 | Tap ⋯ → Delete | Confirm dialog appears; confirm → application deleted; card disappears |
| TC-37-B05 | Tap card body (not ⋯) | Canvas opens for that application |
| TC-37-B06 | See "Desktop view" button on mobile | Button visible in the page header area |
| TC-37-B07 | Tap "Desktop view" | Layout switches to full table with all columns and action buttons |
| TC-37-B08 | Reload page | Desktop view persists (localStorage) |
| TC-37-B09 | Tap "Mobile view" | Returns to card layout |
| TC-37-B10 | Open canvas (any app) | Drag nodes, pan, zoom — no regression |

---

## Pass Criteria

All TC-37-L01..L09 and TC-37-B01..B10 PASS.

---

*Status: Draft*
