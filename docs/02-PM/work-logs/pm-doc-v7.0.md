# PM Doc v7.0 — Q2 Mobile: App Card Actions + Desktop View Entry

**Feature:** `TODO-q2-mobile-apps-actions-desktop-entry-constraints`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Problem Statement

The mobile Applications list is missing two capabilities that exist on desktop:

1. **Missing card actions**: Desktop shows Open / Settings / History / Copy / Delete per row. Mobile cards only show a Settings button and an arrow. Users on phones cannot copy or delete applications without switching to a desktop browser.

2. **No desktop-layout entry point**: Power users on phones (e.g. tablet-width phones, landscape mode) have no way to opt in to the full desktop table view. Adding a single "Desktop view" toggle lets them access richer columns and actions on demand.

**Constraint:** Changes must be additive and must not regress the canvas (pan, zoom, node drag).

---

## User Stories

### US-36 — Copy & Delete Actions on Mobile Application Cards

**As a** workflow administrator on a phone,  
**I want** to copy or delete an application directly from the mobile card list,  
**So that** I don't need to open a desktop browser for those operations.

**Acceptance Criteria:**
- AC-36-1: Each mobile application card exposes a **"⋯" (ellipsis) overflow menu button** in the card's action area.
- AC-36-2: Tapping ⋯ opens a dropdown/menu with options: **Copy**, **Delete** (and optionally History).
- AC-36-3: **Copy** opens the same "Copy workflow" modal that exists on desktop (target name input → confirm).
- AC-36-4: **Delete** shows the same confirm modal ("Delete application?") then calls the delete API.
- AC-36-5: Success/failure messages are shown via `message.success` / `message.error` consistent with desktop.
- AC-36-6: The existing Settings button and tap-to-open-canvas behaviour are NOT removed.
- AC-36-7: Canvas page is not affected.

---

### US-37 — "Desktop View" Toggle on Mobile

**As a** power user on a phone or tablet,  
**I want** a one-tap way to switch to the full desktop table layout on the Applications page,  
**So that** I can see all columns and actions without switching devices.

**Acceptance Criteria:**
- AC-37-1: A small **"Desktop view" / "Mobile view"** toggle button is visible on the Applications page on small screens.
- AC-37-2: Tapping it switches the Applications list from card layout to the full desktop `<Table>` view (with all columns and actions).
- AC-37-3: The preference is stored in `localStorage` so it persists across page reloads.
- AC-37-4: A **"Mobile view"** button is shown in the desktop-table-on-mobile view to revert.
- AC-37-5: The Records page does NOT need this toggle (it is less frequently managed on mobile).
- AC-37-6: Canvas page is not affected.

---

## Out of Scope

- Swipe-to-delete gesture
- Drag-to-reorder
- Any backend changes
- Records page desktop toggle

---

*Status: Draft*
