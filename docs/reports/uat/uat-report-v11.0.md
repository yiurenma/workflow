# UAT Report — v11.0: Mobile Application Overflow Menu Fix

**Date:** 2026-04-08
**Feature:** Mobile overflow menu (History / Copy / Delete) no longer navigates to canvas
**TODO label:** `TODO-mobile-app-overflow-history-copy-modal-not-navigation`
**Branch:** `claude/complete-todo-tasks-QIAHA`

---

## Summary

The bug causing History and Copy to navigate to the canvas instead of opening their respective modals/drawers on mobile has been resolved. All three overflow actions (History, Copy, Delete) now behave consistently with desktop — History opens a history drawer, Copy opens a modal, Delete triggers a confirm dialog — without any unintended canvas navigation.

**Result: PASS — all UAT scenarios passed.**

---

## Root Cause Recap

The mobile card had `onClick={navigate}` on the outer `<div>` wrapping both the info area and the actions zone. When an Ant Design Dropdown portal unmounts after a menu-item click, the click event bubbled through the DOM to the outer div, triggering ghost navigation. This ghost click happened _after_ the dropdown action had already dispatched (e.g. `setHistoryTarget(...)`), so both the modal AND the navigation fired simultaneously.

**Fix (PR diff summary — `src/routes/workflows/index.tsx`):**
- Removed `onClick={navigate}` from the outer card `<div>`
- Moved it to the inner info-column `<div>` only (with `cursor-pointer` class)
- Actions zone (`<Button>Settings</Button>`, `<Dropdown>⋯</Dropdown>`) retains no navigate handler

---

## UAT Scenarios

| # | Scenario | Viewport | Steps | Expected | Actual | Status |
|---|----------|----------|-------|----------|--------|--------|
| U1 | History opens drawer, no navigation | 390 × 844 (iPhone) | 1. Open app list → 2. Tap ⋯ on any card → 3. Tap "History" | History drawer opens, URL stays at `/workflows/` | Drawer opens, URL unchanged | **PASS** |
| U2 | Copy opens modal, no navigation | 390 × 844 | 1. Open app list → 2. Tap ⋯ → 3. Tap "Copy" | Copy modal opens, URL stays at `/workflows/` | Modal opens, URL unchanged | **PASS** |
| U3 | Delete shows confirm, no navigation | 390 × 844 | 1. Open app list → 2. Tap ⋯ → 3. Tap "Delete" | Confirm dialog appears, URL stays at `/workflows/` | Confirm shown, URL unchanged | **PASS** |
| U4 | Tapping card info area navigates to canvas | 390 × 844 | 1. Open app list → 2. Tap app name/info area | Navigates to `/workflows/<name>` canvas | URL changes to canvas | **PASS** |
| U5 | Settings button opens modal, no navigation | 390 × 844 | 1. Open app list → 2. Tap "Settings" on a card | Settings modal opens, URL unchanged | Modal opens | **PASS** |
| U6 | Desktop behavior unaffected | 1280 × 800 | 1. Open app list → 2. Click "History" / "Copy" / "Delete" in table actions | All modals/drawers open correctly | Unchanged from before | **PASS** |
| U7 | No regression on canvas page | 390 × 844 | 1. Navigate to canvas → 2. interact | Canvas renders and functions normally | Canvas OK | **PASS** |

---

## E2E Automation Coverage

The following Playwright tests cover the fix and run in CI as part of v12.0 suite:

| Test ID | Spec | Description |
|---------|------|-------------|
| TC-APP-MOB-04 | `applications-mobile.spec.ts` | Card tap navigates to canvas |
| TC-APP-MOB-05 | `applications-mobile.spec.ts` | ⋯ → History opens drawer (no nav) |
| TC-APP-MOB-06 | `applications-mobile.spec.ts` | ⋯ → Copy opens modal (no nav) |
| TC-APP-MOB-07 | `applications-mobile.spec.ts` | ⋯ → Delete shows confirm (no nav) |
| TC-APP-MOB-08 | `applications-mobile.spec.ts` | Settings opens modal (no nav) |

All 5 tests pass (Mobile Chrome project, 390 × 844 viewport, `isMobile: true, hasTouch: true`).

---

## Sign-off

- **QA Agent:** Automated Playwright suite — 5/5 mobile tests pass
- **UAT outcome:** PASS — no regressions, all overflow actions behave correctly on mobile
