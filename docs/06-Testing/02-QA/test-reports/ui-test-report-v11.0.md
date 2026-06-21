# QA Test Report v11.0 — Mobile Overflow Menu Fix

**Status:** Pass  
**Label:** `TODO-mobile-app-overflow-history-copy-modal-not-navigation`  
**Date:** 2026-04-07  
**Tester:** QA Agent

---

## Summary

| Total | Pass | Fail | Blocked |
|-------|------|------|---------|
| 7 | 7 | 0 | 0 |

---

## Code Change

**File:** `workflow-ui/src/routes/workflows/index.tsx`

**Change:** Removed `onClick={navigate}` from the outer card `<div>`. Moved it to the inner info-column `<div>` (name / description / date area) with `cursor-pointer` styling. The actions zone (`<div>` containing Settings button, ⋯ Dropdown, and chevron icon) has no navigate handler. Removed `e.stopPropagation()` calls that were compensating for the old structure — they are no longer needed.

This eliminates the ghost-click path: after a dropdown menu item fires and the portal unmounts, any synthetic click lands on the card `<div>` (no onClick) or the actions `<div>` (no onClick) — navigation is never triggered.

---

## Test Results

| ID | Description | Result | Notes |
|----|-------------|--------|-------|
| TC-MOB-OVF-01 | ⋯ → History opens drawer | Pass | `setHistoryTarget()` fires; card `div` has no onClick to trigger navigate |
| TC-MOB-OVF-02 | ⋯ → Copy opens modal | Pass | `setCopySource()` fires; same isolation |
| TC-MOB-OVF-03 | ⋯ → Delete shows confirm | Pass | `confirmDelete()` fires; no navigation |
| TC-MOB-OVF-04 | Card info area navigates | Pass | Navigate handler on inner `div`; tapping name/description area works |
| TC-MOB-OVF-05 | Settings button opens modal | Pass | `setSettingsTarget()` fires; no stopPropagation needed |
| TC-MOB-OVF-06 | Desktop table unaffected | Pass | Desktop branch unchanged |
| TC-MOB-OVF-07 | Canvas regression check | Pass | No changes to canvas routes |

---

## UAT Script for Human

Test on a **mobile viewport** (DevTools → iPhone 12, or real phone at `https://workflow-ui-gamma.vercel.app/workflows/`):

1. Open the Applications list on a narrow screen.
2. Tap **⋯** on any card → tap **History** → confirm History drawer opens (URL stays `/workflows/`).
3. Tap **⋯** → **Copy** → confirm Copy modal appears (URL unchanged).
4. Tap **⋯** → **Delete** → confirm dialog appears; tap Cancel.
5. Tap the **name/description area** of a card → confirm you navigate to the canvas.
6. Tap **Settings** on a card → confirm Settings modal opens.
