# UI Test Report v5.0 — Application & Record Tables: Vertical Scroll, Total Count, Translation Fields

**Feature:** `TODO-application-record-tables-scroll-count-translation`  
**Date:** 2026-04-07  
**Tester:** QA Agent (Claude Sonnet 4.6)  
**Test Doc:** `test-doc-v5.0.md`

---

## Source Verification

| TC-ID | Check | Result | Evidence |
|-------|-------|--------|----------|
| TC-32-L01 | `workflows/index.tsx` Table has `scroll={{ y: "calc(100vh - 320px)" }}` | ✅ PASS | `workflows/index.tsx` — `scroll={{ y: "calc(100vh - 320px)" }}` added to `<Table>` |
| TC-32-L02 | `workflows/index.tsx` pagination has `showTotal` | ✅ PASS | `showTotal: (total: number) => \`${total} total\`` present in pagination config |
| TC-32-L03 | `records/index.tsx` Table has `scroll={{ x: 1200, y: "calc(100vh - 360px)" }}` | ✅ PASS | `scroll={{ x: 1200, y: "calc(100vh - 360px)" }}` present |
| TC-32-L04 | `records/index.tsx` pagination has `showTotal` | ✅ PASS | `showTotal: (total: number) => \`${total} total\`` present |
| TC-32-L05 | Mobile "More filters" toggle in records page | ✅ PASS | `showMoreFilters` state + `setShowMoreFilters` toggle button + conditional `(!isMobile \|\| showMoreFilters)` guard present |
| TC-32-L06 | Canvas file not modified | ✅ PASS | Only `workflows/index.tsx` and `records/index.tsx` changed; `$applicationName.tsx` untouched |
| TC-32-L07 | No new npm dependencies added | ✅ PASS | `package.json` unchanged for this feature |

**Source checks: 7/7 PASS**

---

## Browser Tests — UAT Required

| TC-ID | Check | Status |
|-------|-------|--------|
| TC-32-B01 | Applications table shows "X total" and renders normally | Browser TBD |
| TC-32-B02 | Applications table scrolls vertically at short viewport | Browser TBD |
| TC-32-B03 | Applications pagination still works | Browser TBD |
| TC-32-B04 | Canvas not regressed | Browser TBD |
| TC-33-B01 | Records table columns all visible on desktop | Browser TBD |
| TC-33-B02 | Records table vertical scroll at short viewport | Browser TBD |
| TC-33-B03 | Records table horizontal scroll | Browser TBD |
| TC-33-B04 | Records pagination shows "X total" | Browser TBD |
| TC-33-B05 | Mobile "More filters" button visible and expands extra filters | Browser TBD |
| TC-33-B06 | Mobile filter by Confirmation No. works | Browser TBD |

---

## UAT Script for Human

**Applications table** — Open https://workflow-ui-gamma.vercel.app/workflows (hard refresh):

| Step | Action | What to check |
|------|--------|---------------|
| 1 | Load the page | Table renders; pagination shows e.g. "47 total" (or whatever count) next to page numbers |
| 2 | Shrink browser window height to ~600px | Table body gets a scrollbar; rows remain accessible by scrolling inside the table; header row stays fixed |
| 3 | Click a page number in pagination | New page of rows loads correctly |
| 4 | Click **Open** on any application | Canvas opens; drag a node, pan, zoom — all work normally |

**Records table** — Open https://workflow-ui-gamma.vercel.app/records (hard refresh):

| Step | Action | What to check |
|------|--------|---------------|
| 5 | Load the page on desktop | All columns visible: ID, Application, Overall Status, Confirmation No., Tracking No., Customer ID, Created, Retries, Actions |
| 6 | Shrink browser window height to ~600px | Table body scrolls vertically; header stays fixed |
| 7 | Scroll table horizontally | All columns accessible (x: 1200 scroll) |
| 8 | Check pagination | "X total" label visible |
| 9 | On mobile (or narrow viewport ≤768px) | "More filters" button visible next to Search |
| 10 | Tap "More filters" | Confirmation No., Tracking No., Customer ID, From, To inputs appear |

---

*End of UI Test Report v5.0 — Source checks 7/7 PASS; Browser UAT pending human execution.*
