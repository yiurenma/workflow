# UI Test Report v17.0 — E2E Pass 2: Full Regression

**Date:** 2026-04-08
**Label:** `TODO-e2e-playwright-pass2-full-regression-after-backlog`

## Summary

| Result | Count |
|--------|-------|
| PASS   | 64    |
| SKIP   | 24    |
| FAIL   | 0     |
| TOTAL  | 88    |

All 24 skips are expected viewport-mismatch skips (Mobile-only tests skipped on Desktop Chrome project; Desktop-only tests skipped on Mobile Chrome project).

## Test Suites

| Suite | Desktop Pass | Desktop Skip | Mobile Pass | Mobile Skip | Fail |
|-------|-------------|-------------|-------------|-------------|------|
| navigation.spec.ts | 4 | 0 | 4 | 0 | 0 |
| applications-desktop.spec.ts | 9 | 0 | 0 | 9 | 0 |
| applications-mobile.spec.ts | 0 | 10 | 10 | 0 | 0 |
| canvas.spec.ts | 9 | 0 | 9 | 0 | 0 |
| canvas-mobile.spec.ts | 1 | 4 | 4 | 1 | 0 |
| node-editor.spec.ts | 2 | 0 | 2 | 0 | 0 |
| explain.spec.ts | 2 | 0 | 2 | 0 | 0 |
| records.spec.ts | 3 | 0 | 3 | 0 | 0 |
| **Total** | **30** | **14** | **34** | **10** | **0** |

## New Tests Added Since Pass 1

| TC | Suite | Description |
|----|-------|-------------|
| TC-CANVAS-04 | canvas.spec.ts | Canvas empty state when pluginList absent (v13.0) |
| TC-CANVAS-MOB-01..05 | canvas-mobile.spec.ts | Mobile FAB: visible, tap, drag, localStorage, desktop-absent (v15.0) |
| TC-JSONPATH-01..05 | canvas.spec.ts | JsonPath playground: button, modal, match, invalid JSON, no-match (v16.0) |

## Comparison with Pass 1

| Metric | Pass 1 | Pass 2 |
|--------|--------|--------|
| Total tests | 66 | 88 |
| Pass | 47 | 64 |
| Skip | 19 | 24 |
| Fail | 0 | **0** |

Pass count increased by 17 (all new tests pass). Skip count increased by 5 (TC-CANVAS-MOB viewport-mismatch skips). Zero regressions.

## Conclusion

All backlog items (v13.0–v16.0) are implemented and verified. No existing tests regressed. E2E pass 2 complete.
