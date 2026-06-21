# Test Doc v11.0 — Mobile Overflow Menu: Modal-Not-Navigation Fix

**Status:** Draft  
**Label:** `TODO-mobile-app-overflow-history-copy-modal-not-navigation`  
**Date:** 2026-04-07

---

## 1. Scope

Mobile card overflow menu on the Applications list (`/workflows/`). Narrow viewport (≤ 767 px).

---

## 2. Test Environment

| Item | Value |
|------|-------|
| Frontend URL | https://workflow-ui-gamma.vercel.app/workflows/ |
| Viewport | 375 × 812 px (iPhone SE / 12) or DevTools mobile mode |
| At least 1 application | must exist in the system |

---

## 3. Test Cases

### TC-MOB-OVF-01 — History opens drawer (not navigation)

**Precondition:** At least one application card is visible on mobile.  
**Steps:**
1. Tap the **⋯** (ellipsis) button on any application card.
2. Tap **History** in the dropdown menu.

**Expected:**
- The History **drawer** slides in from the right (or bottom).
- The URL **does not change** — still `/workflows/`.
- The user is NOT taken to the canvas editor.

---

### TC-MOB-OVF-02 — Copy opens modal (not navigation)

**Precondition:** At least one application card is visible.  
**Steps:**
1. Tap the **⋯** button on any application card.
2. Tap **Copy** in the dropdown menu.

**Expected:**
- The Copy **modal** appears with a "Target application name" input.
- The URL **does not change**.
- The user is NOT taken to the canvas editor.

---

### TC-MOB-OVF-03 — Delete shows confirm dialog (not navigation)

**Precondition:** At least one application card is visible.  
**Steps:**
1. Tap the **⋯** button on any application card.
2. Tap **Delete** in the dropdown menu.

**Expected:**
- A **confirmation dialog** appears ("Delete application — are you sure?").
- The URL **does not change**.
- Tapping Cancel dismisses the dialog; no deletion occurs.

---

### TC-MOB-OVF-04 — Card body tap still navigates

**Precondition:** At least one application card is visible.  
**Steps:**
1. Tap the **application name / description area** (center of card, away from ⋯ or Settings).

**Expected:**
- User is navigated to `/workflows/$applicationName` (canvas editor).

---

### TC-MOB-OVF-05 — Settings button still works

**Precondition:** At least one application card is visible.  
**Steps:**
1. Tap the **Settings** button on any application card (not the ⋯ button).

**Expected:**
- The Settings modal opens.
- No navigation occurs.

---

### TC-MOB-OVF-06 — Desktop table view unaffected

**Precondition:** Use a desktop browser (≥ 768 px) or toggle "Desktop view" on mobile.  
**Steps:**
1. Click the **History** button in the Actions column of the table.
2. Click the **Copy** button in the Actions column.
3. Click the **Delete** button in the Actions column.

**Expected:**
- History opens the drawer.
- Copy opens the modal.
- Delete shows confirm dialog.
- Each behaves identically to before the fix.

---

### TC-MOB-OVF-07 — Regression: canvas / pan / zoom unaffected

**Precondition:** Open any application on the canvas editor.  
**Steps:**
1. Pan and zoom the canvas.
2. Drag a node.

**Expected:**
- Canvas interactions work normally; no regression from the Applications list fix.

---

## 4. Pass / Fail Criteria

All 7 TCs must pass for the fix to be considered complete.

---

## 5. Regression Reference

Related items:
- `TODO-q2-mobile-apps-actions-desktop-entry-constraints` (mobile card overflow implementation)
- `TODO-mobile-add-application-fab-draggable` (FAB)
