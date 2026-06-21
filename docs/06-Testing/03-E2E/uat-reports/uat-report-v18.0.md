# UAT Report v18.0 — Canvas Mobile Header Overflow Fix

**Date:** 2026-04-08
**Label:** `TODO-canvas-mobile-header-save-overflow`
**Status:** PASS

---

## UAT Scenarios

| # | Scenario | Viewport | Expected | Result |
|---|----------|----------|----------|--------|
| 1 | Save button visible and tappable | Mobile (390×844) | Button "Save" in viewport, tappable | PASS |
| 2 | `⋯` overflow button visible | Mobile (390×844) | Ellipsis button present in header | PASS |
| 3 | Overflow menu opens on tap | Mobile (390×844) | Dropdown appears with Explain / JsonPath / Run | PASS |
| 4 | Explain opens from overflow menu | Mobile (390×844) | Modal appears after dropdown → Explain | PASS |
| 5 | JsonPath opens from overflow menu | Mobile (390×844) | JsonPath Playground modal appears | PASS |
| 6 | Desktop header unchanged | Desktop (1280×800) | All 4 buttons (Explain / JsonPath / Run / Save) in flat row | PASS |
| 7 | No regression on existing canvas tests | Both | TC-CANVAS-01..04 all pass | PASS |

## Summary

All 7 scenarios passed. The regression (Save clipped off-screen on mobile) is resolved. The `⋯` dropdown correctly contains the three secondary actions. Desktop layout is fully preserved.

**Root cause post-mortem:** v16.0 added a 4th button to the header toolbar without a mobile breakpoint check. Going forward, any new `WorkflowHeader` toolbar addition must include a Mobile Chrome E2E assertion that **Save** remains visible (TC-CANVAS-MOB-06 now acts as a standing regression guard).
