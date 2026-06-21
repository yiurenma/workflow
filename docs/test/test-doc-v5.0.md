# Test Doc v5.0 — Application & Record Tables: Vertical Scroll, Total Count, Translation Fields

**Feature:** `TODO-application-record-tables-scroll-count-translation`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Test Cases — Source / Static

| TC-ID | Description | Expected |
|-------|-------------|----------|
| TC-32-L01 | `workflows/index.tsx` Table has `scroll` prop with `y` key | `scroll={{ y: "calc(100vh - 320px)" }}` present |
| TC-32-L02 | `workflows/index.tsx` pagination has `showTotal` function | `showTotal: (total) => \`${total} total\`` present |
| TC-32-L03 | `records/index.tsx` Table has `scroll` prop with both `x` and `y` keys | `scroll={{ x: 1200, y: "calc(100vh - 360px)" }}` present |
| TC-32-L04 | `records/index.tsx` pagination has `showTotal` function | `showTotal` present |
| TC-32-L05 | Mobile "More filters" toggle rendered in records page | `showMoreFilters` state + toggle button present in JSX |
| TC-32-L06 | Canvas file (`$applicationName.tsx` or equiv.) NOT modified | git diff shows no changes to canvas route |
| TC-32-L07 | No new npm dependencies added | `package.json` unchanged |

---

## Browser Test Cases (UAT — human executes)

### Applications Table (`/workflows`)

| TC-ID | Step | What to verify |
|-------|------|----------------|
| TC-32-B01 | Open `/workflows` on desktop | Table renders; pagination shows "X total" text |
| TC-32-B02 | Shrink browser window height to ~600px | Table body scrolls vertically; header row stays pinned |
| TC-32-B03 | Navigate between pages | Correct rows shown per page; "X total" count unchanged |
| TC-32-B04 | Open canvas (click any application → Open) | Canvas pan/zoom/drag works normally; no layout regression |

### Records Table (`/records`)

| TC-ID | Step | What to verify |
|-------|------|----------------|
| TC-33-B01 | Open `/records` on desktop | Table renders with Confirmation No., Tracking No., Customer ID columns visible |
| TC-33-B02 | Shrink browser window height to ~600px | Table body scrolls vertically; header stays pinned |
| TC-33-B03 | Horizontal scroll | Table scrolls horizontally (x: 1200) — all columns accessible |
| TC-33-B04 | Pagination "X total" | Total record count shown near pagination |
| TC-33-B05 | Mobile — open `/records` on ≤768px | "More filters" button visible; tapping it expands Confirmation No., Tracking No., Customer ID, From, To filters |
| TC-33-B06 | Mobile — enter Confirmation No. and press Search | Results filtered correctly |

### Regression

| TC-ID | Step | What to verify |
|-------|------|----------------|
| TC-REG-01 | Open canvas workflow | Canvas renders; drag nodes; pan/zoom all work |
| TC-REG-02 | Open node drawer | Drawer opens with 3 panels; all subtitles/tooltips intact |

---

## Pass Criteria

- All TC-32-L01..L07 PASS (source checks)
- All TC-32-B01..B04 PASS (Applications table)
- All TC-33-B01..B06 PASS (Records table)
- All TC-REG-01..02 PASS (canvas regression)

---

*Status: Draft*
