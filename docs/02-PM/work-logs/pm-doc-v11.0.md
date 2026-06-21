# PM Doc v11.0 — Mobile Overflow Menu: History/Copy Open Modal, Not Navigation

**Status:** Draft  
**Label:** `TODO-mobile-app-overflow-history-copy-modal-not-navigation`  
**Date:** 2026-04-07

---

## 1. User Story

> As a **mobile user** on the Applications list, when I tap the **⋯ overflow menu** on an application card and choose **History** or **Copy**, I want that action to open the appropriate **modal or drawer** (same as on desktop), NOT navigate me to the Workflow canvas screen.

---

## 2. Problem Statement

On phone viewports, tapping **History** or **Copy** in the card overflow/dropdown menu unexpectedly navigates the user to the `/workflows/$applicationName` route (the canvas editor). The user never sees the History drawer or the Copy modal — they are silently taken to the wrong screen.

**Expected behaviour (per desktop parity):**
- **History** → opens the `<HistoryDrawer>` (side drawer listing prior versions)
- **Copy** → opens the `<Modal>` for copying the workflow to a new application name
- **Delete** → shows the `Modal.confirm()` dialog; executes delete on confirm; NO navigation

**Root cause (working hypothesis):**  
The entire mobile card `<div>` has an `onClick` handler that calls `navigate()`. When a dropdown menu item (History / Copy) is tapped on mobile:
1. The menu item fires its handler (`setHistoryTarget(...)` or `setCopySource(...)`) ✓
2. The Ant Design dropdown then closes and unmounts its portal
3. A synthetic/ghost click event fires at the tap coordinates — now pointing to the underlying card `<div>`
4. The card's `onClick` → `navigate(...)` fires → wrong-screen bug

This ghost-click behaviour is a well-known mobile touch issue with overlaid portals.

---

## 3. Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC-1 | On a mobile viewport (≤ 767 px), tapping **History** in the overflow menu opens the History drawer — no navigation occurs |
| AC-2 | On a mobile viewport, tapping **Copy** in the overflow menu opens the Copy modal — no navigation occurs |
| AC-3 | On a mobile viewport, tapping **Delete** in the overflow menu shows the confirm dialog — no navigation occurs |
| AC-4 | Tapping the **card body** (name, description, date area) still navigates to the Workflow canvas |
| AC-5 | Desktop table view is **unaffected** |
| AC-6 | The Workflow canvas / pan / zoom interactions are **unaffected** |
| AC-7 | The Settings button (separate from overflow) still works without navigation side-effects |

---

## 4. Out of Scope

- Any changes to the History drawer content or Copy modal logic
- Desktop layout
- Records table

---

## 5. Impact

| Dimension | Details |
|-----------|---------|
| Files touched | `workflow-ui/src/routes/workflows/index.tsx` |
| Repos | `workflow-ui` only |
| Deployments | Vercel (frontend) — no backend change |
