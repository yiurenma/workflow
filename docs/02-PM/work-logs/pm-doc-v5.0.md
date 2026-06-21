# PM Doc v5.0 — Application & Record Tables: Vertical Scroll, Total Count, Translation Fields

**Feature:** Application + Record tables — no vertical scroll, missing totals & translation  
**Label:** `TODO-application-record-tables-scroll-count-translation`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Problem Statement

Two table pages are broken for users who manage many applications or records:

1. **Application Team form** (`/workflows`): The Applications table has no vertical scroll. When many rows are returned (e.g. 20 per page fill a tall table), the page layout can cut off rows or make the full table unviewable if the viewport is short. There is also no explicit "X total applications" count visible at a glance.

2. **Record Team form** (`/records`): The Records table has horizontal scroll (`x: 1200`) but no vertical scroll. Translation-related fields (Confirmation No., Tracking No., Customer ID) are hidden on mobile. There is no standalone total count label above the table.

**Constraint:** Any layout change must NOT regress the workflow canvas / artboard (pan, zoom, node drag, drawing) on the `/$applicationName` route. Fixes must be isolated to `/workflows` and `/records` pages only.

---

## User Stories

### US-32 — Application Table Vertical Scroll + Total Count

**As a** workflow administrator,  
**I want** the Applications table to be vertically scrollable within the page content area and to see a total application count,  
**So that** I can view all rows without the table breaking out of the layout, and I know at a glance how many applications exist.

**Acceptance Criteria:**
- AC-32-1: Desktop — the Applications table has a constrained height and scrolls vertically when there are more rows than fit in the visible area.
- AC-32-2: Desktop — a "X applications total" label is shown above or near the table (or as `showTotal` in the pagination).
- AC-32-3: The canvas / artboard page is NOT affected by this change.
- AC-32-4: Mobile card list is NOT changed (it already scrolls naturally).
- AC-32-5: Pagination still works correctly (page change, total count).

---

### US-33 — Record Table Vertical Scroll + Total Count + Translation Fields Visible

**As a** QA engineer or support analyst,  
**I want** the Records table to scroll vertically, show all translation-related fields (Confirmation No., Tracking No., Customer ID) on all screen sizes, and display the total record count,  
**So that** I can review all records, including translation-related data, without fields being hidden.

**Acceptance Criteria:**
- AC-33-1: Desktop — the Records table has a constrained height and scrolls vertically when there are more rows than fit in the viewport.
- AC-33-2: Desktop — "X records total" shown via `showTotal` in pagination (or standalone label).
- AC-33-3: Confirmation No., Tracking No., Customer ID columns are visible on desktop (they already exist in columns array — verify they are not hidden).
- AC-33-4: Mobile — filter bar surfaces Confirmation No. and Tracking No. filters (currently hidden behind `!isMobile` guard), or at minimum a "More filters" affordance is added so mobile users can filter by these fields.
- AC-33-5: Canvas is NOT affected.
- AC-33-6: Existing pagination still works.

---

## Out of Scope

- Resizable columns
- Export to CSV/Excel
- Inline editing of table cells
- Any changes to the canvas/artboard (`/$applicationName`) route

---

*Status: Draft*
