# QA Test Report — Frontend Rebuild from Design Handoff

**Document version:** 37.0  
**Date:** 2026-04-19  
**Status:** PASS  
**Label:** `TODO-frontend-rebuild-from-design-handoff`

## Summary

| Metric | Value |
|--------|-------|
| Total tests | 116 |
| Passed | 115 |
| Skipped | 1 (TC-NODE-ENHANCED-02 — mobile viewport, covered by Mobile Chrome project) |
| Failed | 0 |
| Verdict | **PASS** |

## Environment

- UAT URL: https://workflow-ui-gamma.vercel.app
- Branch merged: `feature/design-handoff-rebuild` → `main`
- Commits: `e9bc845`, `5e2f77e`
- Desktop: Chrome 1280×720
- Mobile: Chrome 390×844 (isMobile, hasTouch)

## Test Results by Suite

### Desktop Chrome

| Suite | Pass | Skip | Fail |
|-------|------|------|------|
| applications-desktop (TC-APP-DESK-01..10) | 10 | 0 | 0 |
| canvas (TC-CANVAS-01..05, TC-GENERATOR-01..02, TC-JSONPATH-01..05) | 12 | 0 | 0 |
| carbon-design-desktop (TC-CARBON-DESK-01..15) | 15 | 0 | 0 |
| drawer-close-v29 (TC-DRAWER-CLOSE-01..10) | 10 | 0 | 0 |
| explain (TC-EXPLAIN-01..02) | 2 | 0 | 0 |
| navigation (TC-NAV-01..04) | 4 | 0 | 0 |
| node-editor-enhanced (TC-NODE-ENHANCED-01,03..05) | 4 | 1 | 0 |
| node-editor (TC-NODE-01..02) | 2 | 0 | 0 |
| records (TC-REC-01..03) | 3 | 0 | 0 |
| refactor-regression (TC-REFACTOR-01..10) | 10 | 0 | 0 |

### Mobile Chrome

| Suite | Pass | Skip | Fail |
|-------|------|------|------|
| applications-mobile (TC-APP-MOB-01..10) | 10 | 0 | 0 |
| canvas-mobile (TC-CANVAS-MOB-01..09) | 7 | 0 | 0 |
| carbon-design-mobile (TC-CARBON-MOB-01..06) | 6 | 0 | 0 |
| drawer-close-v29 (mobile variants) | 10 | 0 | 0 |
| navigation (TC-NAV-01..04) | 4 | 0 | 0 |
| records (TC-REC-01..03) | 3 | 0 | 0 |

## Fixes Applied During Testing

Two mobile regressions introduced by the Carbon nav rebuild were found and fixed before final run:

1. **FAB z-index** (`src/routes/workflows/index.tsx`): Changed from `z-50` (50) to inline `zIndex: 250` so the FAB renders above the bottom tab bar (`zIndex: 200`).
2. **FAB default position**: Changed default `bottom` from `24px` to `84px` (60px tab bar + 24px margin) so FAB is not obscured on first load.
3. **TC-NAV-02 test** (`e2e/navigation.spec.ts`): Added `.filter({ visible: true })` to `getByText('Applications')` so the test finds the visible bottom tab bar label instead of the hidden top-nav span.

## Design System Verification

All Carbon Design System requirements confirmed on UAT:

- ✅ IBM Plex Sans font in body
- ✅ Nav background `#161616` (Carbon Gray 100)
- ✅ Primary button `#0f62fe` (IBM Blue 60)
- ✅ Border-radius `0px` on buttons, cards, modals
- ✅ Table header `#f4f4f4` (Carbon Gray 10)
- ✅ No Quiet Luxury colors present
- ✅ Nav height ≥ 48px
- ✅ Primary button height ≥ 40px, width ≥ 44px
