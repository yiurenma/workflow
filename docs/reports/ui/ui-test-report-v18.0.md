# UI Test Report v18.0 — Canvas Mobile Header Overflow Fix

**Date:** 2026-04-08
**Label:** `TODO-canvas-mobile-header-save-overflow`

## Summary

| Result | Count |
|--------|-------|
| PASS   | 67    |
| SKIP   | 27    |
| FAIL   | 0     |
| TOTAL  | 94    |

All 27 skips are expected viewport-mismatch skips.

## New Tests Added

| TC ID | Suite | Viewport | Description | Result |
|-------|-------|----------|-------------|--------|
| TC-CANVAS-MOB-06 | canvas-mobile.spec.ts | Mobile | Save button visible on mobile canvas | PASS |
| TC-CANVAS-MOB-07 | canvas-mobile.spec.ts | Mobile | `⋯` overflow menu trigger visible | PASS |
| TC-CANVAS-MOB-08 | canvas-mobile.spec.ts | Mobile | Overflow menu contains Explain, JsonPath, Run | PASS |

## Updated Tests

| TC ID | Change | Result |
|-------|--------|--------|
| TC-CANVAS-02 | Renamed to "header actions accessible"; on mobile checks `⋯` + Save, on desktop checks Explain | PASS (both viewports) |
| TC-EXPLAIN-01 | On mobile, checks `⋯` overflow button visible instead of Explain directly | PASS (both viewports) |
| TC-EXPLAIN-02 | Uses `clickCanvasHeaderAction()` helper — opens dropdown on mobile, clicks button on desktop | PASS (both viewports) |
| TC-JSONPATH-01 | On mobile, checks `⋯` button visible | PASS (both viewports) |
| TC-JSONPATH-02..05 | Uses `clickCanvasHeaderAction()` helper | PASS (both viewports) |

## Change Implemented

`workflow-header/index.tsx`:
- Added `useIsMobile`, `Dropdown`, `EllipsisOutlined`
- Mobile (< 768 px): `⋯` dropdown (Explain / JsonPath / Run) + **Save** button always visible
- Desktop (≥ 768 px): all four buttons in flat row (unchanged)

`mocks.ts`: added `clickCanvasHeaderAction(page, name)` helper — opens dropdown on mobile, clicks button directly on desktop.

## TypeScript

`npx tsc --noEmit` — clean, 0 errors.

## Comparison with Pass 2

| Metric | Pass 2 | v18.0 |
|--------|--------|-------|
| Total tests | 88 | 94 |
| Pass | 64 | 67 |
| Skip | 24 | 27 |
| Fail | 0 | **0** |
