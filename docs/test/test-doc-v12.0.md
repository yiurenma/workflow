# Test Doc v12.0 — E2E Playwright Pass 1: Test Cases

**Status:** Draft  
**Label:** `TODO-e2e-playwright-pass1-author-full-run`  
**Date:** 2026-04-07

---

## 1. Scope

All key UI function points of `workflow-ui`. Tests run against `https://workflow-ui-gamma.vercel.app`.

---

## 2. Test Cases

### Navigation

| ID | Description | Steps | Expected |
|----|-------------|-------|----------|
| TC-NAV-01 | App loads at root | Navigate to `/` | Page loads without error; redirect or content visible |
| TC-NAV-02 | Applications list route | Navigate to `/workflows/` | "Applications" heading visible |
| TC-NAV-03 | Records list route | Navigate to `/records/` | Records page loads; table or empty state visible |
| TC-NAV-04 | Unknown route | Navigate to `/does-not-exist` | App shows 404 or redirects gracefully |

---

### Applications List — Desktop

| ID | Description | Steps | Expected |
|----|-------------|-------|----------|
| TC-APP-DESK-01 | Table renders | Load `/workflows/` at ≥ 768 px | Ant Design table with columns visible |
| TC-APP-DESK-02 | Pagination visible | Load list | Pagination component visible below table |
| TC-APP-DESK-03 | Total count shown | Load list | "N total" text visible |
| TC-APP-DESK-04 | Search filters list | Type a name in search box; submit | Table updates (or shows empty state) |
| TC-APP-DESK-05 | Open button navigates | Click "Open" in first row | URL changes to `/workflows/$name` |
| TC-APP-DESK-06 | Settings opens modal | Click "Settings" in first row | Settings modal appears; no navigation |
| TC-APP-DESK-07 | History opens drawer | Click "History" in first row | History drawer appears; no navigation |
| TC-APP-DESK-08 | Copy opens modal | Click "Copy" in first row | Copy modal with name input appears |
| TC-APP-DESK-09 | Delete shows confirm | Click "Delete" in first row | Confirm dialog appears |

---

### Applications List — Mobile

| ID | Description | Steps | Expected |
|----|-------------|-------|----------|
| TC-APP-MOB-01 | Card view renders | Load `/workflows/` at ≤ 767 px | Application cards visible (not table) |
| TC-APP-MOB-02 | Desktop view toggle | Tap "Desktop view" button | Table view renders |
| TC-APP-MOB-03 | Mobile view toggle | In table view, tap "Mobile view" | Card view restores |
| TC-APP-MOB-04 | Card tap navigates | Tap card info area | Navigates to canvas |
| TC-APP-MOB-05 | ⋯ History → drawer | Tap ⋯ → History | History drawer opens; URL unchanged |
| TC-APP-MOB-06 | ⋯ Copy → modal | Tap ⋯ → Copy | Copy modal opens; URL unchanged |
| TC-APP-MOB-07 | ⋯ Delete → confirm | Tap ⋯ → Delete | Confirm dialog opens; URL unchanged |
| TC-APP-MOB-08 | Settings button | Tap Settings on card | Settings modal opens; no navigation |
| TC-APP-MOB-09 | FAB visible | Load mobile list | Fixed + button visible in corner |
| TC-APP-MOB-10 | FAB tap opens dialog | Tap FAB + button | New application dialog opens |

---

### Canvas / Artboard

| ID | Description | Steps | Expected |
|----|-------------|-------|----------|
| TC-CANVAS-01 | Canvas loads | Open any application | Canvas element visible; no JS error |
| TC-CANVAS-02 | Nodes visible | Canvas loads | At least one node visible (if application has workflow) |
| TC-CANVAS-03 | Explain button visible | Canvas loads | "Explain" button visible in header |

---

### Node Editor

| ID | Description | Steps | Expected |
|----|-------------|-------|----------|
| TC-NODE-01 | Drawer opens on click | Click a node | Side drawer appears |
| TC-NODE-02 | Three panels present | Drawer open | Description, Rules, Action sections visible |

---

### Records List

| ID | Description | Steps | Expected |
|----|-------------|-------|----------|
| TC-REC-01 | Records page loads | Navigate to `/records/` | Page renders; no crash |
| TC-REC-02 | Table/card visible | Records page loaded | Table (desktop) or cards (mobile) visible |
| TC-REC-03 | Pagination visible | Records page loaded | Pagination control present |

---

## 3. Pass / Fail Criteria

- **Pass:** All TC pass or known external blockers (e.g. auth wall, empty test data) are documented with rationale.
- **Fail:** Any TC fails due to code regression — document in QA report and open a new TODO item.

---

## 4. QA Run Procedure

1. Install dependencies: `npm install --save-dev @playwright/test && npx playwright install chromium`
2. Run full suite: `npm run test:e2e`
3. Capture output (pass/fail summary, error messages for failures)
4. Write `workflow-agent-teams/docs/ui-test-report-v12.0.md` with results
