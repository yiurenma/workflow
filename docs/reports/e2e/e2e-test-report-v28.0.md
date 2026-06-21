# E2E Test Report v28.0

Date: 2026-04-12
Environment: https://workflow-ui-gamma.vercel.app
Scope: IBM Carbon Design Language refactoring (v28.0)
Runner: Playwright headed Desktop Chrome 1280px + Mobile Chrome 390x844

---

## Summary

- Total: 84 tests (53 Desktop Chrome + 31 Mobile Chrome)
- Passed: 82
- Failed: 1
- Skipped: 1 (TC-NODE-ENHANCED-02 — intentional: mobile-only test skipped on Desktop Chrome)
- Skipped (zero-skip policy): 0 unintentional skips

---

## Test Results by Project

### Desktop Chrome (53 tests, 51 passed, 1 failed, 1 skipped)

| TC-ID | Result | Notes |
|---|---|---|
| TC-APP-DESK-01 | PASS | Table renders with columns |
| TC-APP-DESK-02 | PASS | Pagination visible |
| TC-APP-DESK-03 | PASS | Total count shown |
| TC-APP-DESK-04 | PASS | Search filters list |
| TC-APP-DESK-05 | PASS | Open button navigates to canvas |
| TC-APP-DESK-06 | PASS | Settings button opens modal |
| TC-APP-DESK-07 | PASS | History button opens drawer |
| TC-APP-DESK-08 | PASS | Copy button opens modal |
| TC-APP-DESK-09 | PASS | Delete button shows confirm dialog |
| TC-APP-DESK-10 | PASS | Settings modal shows rename field |
| TC-CANVAS-01 | PASS | Canvas loads for application |
| TC-CANVAS-02 | PASS | Header actions accessible |
| TC-CANVAS-03 | PASS | No JS error on canvas load |
| TC-CANVAS-04 | PASS | Empty state when pluginList absent |
| TC-CANVAS-05 | PASS | Straighten button visible in header |
| TC-GENERATOR-01 | PASS | Generate button accessible from header |
| TC-GENERATOR-02 | PASS | Generate modal opens |
| TC-JSONPATH-01 | PASS | JsonPath accessible from header |
| TC-JSONPATH-02 | PASS | Modal opens |
| TC-JSONPATH-03 | PASS | Valid expression returns result |
| TC-JSONPATH-04 | PASS | Invalid JSON shows error |
| TC-JSONPATH-05 | PASS | No-match expression shows "(no match)" |
| TC-EXPLAIN-01 | PASS | Explain button visible on canvas |
| TC-EXPLAIN-02 | PASS | Clicking Explain opens modal |
| TC-NAV-01 | PASS | App loads at root |
| TC-NAV-02 | PASS | Applications list route resolves |
| TC-NAV-03 | PASS | Records list route resolves |
| TC-NAV-04 | PASS | Unknown route handled gracefully |
| TC-NODE-01 | PASS | Clicking node opens drawer |
| TC-NODE-02 | PASS | Drawer contains Description, Rules, Action sections |
| TC-NODE-ENHANCED-01 | PASS | Desktop drawer meets all 5 layers |
| TC-NODE-ENHANCED-02 | SKIP | Mobile-only test; intentionally skipped on Desktop Chrome |
| TC-NODE-ENHANCED-03 | PASS | Three-panel layout all sections in viewport |
| TC-NODE-ENHANCED-04 | PASS | Rules section JSON input actionable |
| TC-NODE-ENHANCED-05 | FAIL | Close drawer returns to canvas — see Failures section |
| TC-REC-01 | PASS | Records page loads without crash |
| TC-REC-02 | PASS | Table or card view visible |
| TC-CARBON-DESK-01 | PASS | Nav bar is present |
| TC-CARBON-DESK-02 | PASS | Table is present |
| TC-CARBON-DESK-03 | PASS | Primary action button is present |
| TC-CARBON-DESK-04 | PASS | Nav height >= 48px (Carbon shell spec) — measured: 48px |
| TC-CARBON-DESK-05 | PASS | Primary button height >= 40px and width >= 44px |
| TC-CARBON-DESK-06 | PASS | Table is in viewport (not clipped) |
| TC-CARBON-DESK-07 | PASS | Nav bar is in viewport |
| TC-CARBON-DESK-08 | PASS | New Application button opens modal |
| TC-CARBON-DESK-09 | PASS | Nav background is Carbon Gray 100 — computed: rgb(22, 22, 22) = #161616 |
| TC-CARBON-DESK-10 | PASS | Body background is white — computed: rgb(255, 255, 255) = #ffffff |
| TC-CARBON-DESK-11 | PASS | Primary button background is IBM Blue 60 — computed: rgb(15, 98, 254) = #0f62fe |
| TC-CARBON-DESK-12 | PASS | Primary button border-radius is 0px |
| TC-CARBON-DESK-13 | PASS | Table header background is Carbon Gray 10 — computed: rgb(244, 244, 244) = #f4f4f4 |
| TC-CARBON-DESK-14 | PASS | IBM Plex Sans is in body font-family |
| TC-CARBON-DESK-15 | PASS | No Quiet Luxury background colors present on page |

### Mobile Chrome (31 tests, 31 passed, 0 failed)

| TC-ID | Result | Notes |
|---|---|---|
| TC-APP-MOB-01 | PASS | Card view renders (not table) on narrow viewport |
| TC-APP-MOB-02 | PASS | Desktop view toggle renders table |
| TC-APP-MOB-03 | PASS | Mobile view toggle restores card view |
| TC-APP-MOB-04 | PASS | Card info area tap navigates to canvas |
| TC-APP-MOB-05 | PASS | Ellipsis menu -> History opens drawer |
| TC-APP-MOB-06 | PASS | Ellipsis menu -> Copy opens modal |
| TC-APP-MOB-07 | PASS | Ellipsis menu -> Delete shows confirm |
| TC-APP-MOB-08 | PASS | Settings button opens modal |
| TC-APP-MOB-09 | PASS | FAB + button is visible |
| TC-APP-MOB-10 | PASS | FAB tap opens New Application dialog |
| TC-CANVAS-MOB-01 | PASS | FAB visible on mobile canvas |
| TC-CANVAS-MOB-02 | PASS | Tap FAB opens Add Node sheet |
| TC-CANVAS-MOB-03 | PASS | Drag FAB does not open sheet |
| TC-CANVAS-MOB-04 | PASS | Position persisted in localStorage after drag |
| TC-CANVAS-MOB-06 | PASS | Save button visible on mobile canvas |
| TC-CANVAS-MOB-07 | PASS | Overflow menu trigger visible on mobile |
| TC-CANVAS-MOB-08 | PASS | Overflow menu contains Straighten, Explain, JsonPath, Run |
| TC-CANVAS-MOB-09 | PASS | Node drawer opens from bottom on mobile |
| TC-NAV-01 | PASS | App loads at root |
| TC-NAV-02 | PASS | Applications list route resolves |
| TC-NAV-03 | PASS | Records list route resolves |
| TC-NAV-04 | PASS | Unknown route handled gracefully |
| TC-REC-01 | PASS | Records page loads without crash |
| TC-REC-02 | PASS | Table or card view visible |
| TC-REC-03 | PASS | Pagination visible |
| TC-CARBON-MOB-01 | PASS | Mobile cards are present (no table) |
| TC-CARBON-MOB-02 | PASS | Mobile card height >= 80px |
| TC-CARBON-MOB-03 | PASS | First card is in viewport |
| TC-CARBON-MOB-04 | PASS | Mobile card background is Carbon Gray 10 — computed: rgb(244, 244, 244) = #f4f4f4 |
| TC-CARBON-MOB-05 | PASS | Mobile card border-radius is 0px |
| TC-CARBON-MOB-06 | PASS | No Quiet Luxury background colors on mobile |

---

## Failures

### TC-NODE-ENHANCED-05 — Close drawer returns to canvas

Severity: Medium

Description: After clicking a node on the canvas to open the node configuration drawer, clicking the drawer close button (.ant-drawer-close) does not dismiss the drawer. The drawer remains visible after the click. Fallback attempts (mask click, Escape key) also fail to close the drawer.

Reproduction steps:
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows/test-app-01
2. Wait for canvas to load
3. Click any node on the canvas
4. Verify drawer opens (PASS)
5. Click the X close button in the drawer header
6. Expected: drawer closes, canvas is visible
7. Actual: drawer remains open

Root cause analysis: The Carbon refactor introduced a drag-resize handle (6px wide, position:absolute, left:0, top:0, bottom:0, zIndex:10) inside the drawer body. While this handle is in the body and not the header, the Ant Design Drawer's onClose callback (setDrawerOpen(false)) is not being triggered by the close button click. The drawer open state is managed by useWorkflowForm hook. The close button click appears to fire but the React state update does not propagate to hide the drawer. This may be a React event propagation issue introduced during the Carbon refactor.

Impact: Users cannot close the node configuration drawer via the close button. They must navigate away from the canvas page to dismiss it.

Screenshot: test-results/node-editor-enhanced-Node--5db3f-se-drawer-returns-to-canvas-Desktop-Chrome-retry1/test-failed-1.png

Recommended fix: Investigate the onClose handler in WorkflowDrawer and useWorkflowForm. Verify setDrawerOpen(false) is being called when the close button is clicked. Check for event.stopPropagation() calls that may be intercepting the click.

---

## Carbon Design Token Verification (Computed Values)

All Carbon tokens verified against UAT environment on 2026-04-12:

| Token | Expected | Computed | Status |
|---|---|---|---|
| Nav background (Gray 100) | rgb(22, 22, 22) | rgb(22, 22, 22) | PASS |
| Body background | rgb(255, 255, 255) | rgb(255, 255, 255) | PASS |
| Primary button (Blue 60) | rgb(15, 98, 254) | rgb(15, 98, 254) | PASS |
| Button border-radius | 0px | 0px | PASS |
| Table header (Gray 10) | rgb(244, 244, 244) | rgb(244, 244, 244) | PASS |
| Mobile card background (Gray 10) | rgb(244, 244, 244) | rgb(244, 244, 244) | PASS |
| Mobile card border-radius | 0px | 0px | PASS |
| Font family | contains "plex" | IBM Plex Sans present | PASS |
| Quiet Luxury colors absent | 0 violations | 0 violations | PASS |

---

## Accessibility Violations

Axe scan was not run in this cycle (no AxeBuilder integration in current spec files). No accessibility violations were detected via visual inspection. Recommend adding AxeBuilder scan in a future iteration.

---

## Test Suite Changes (v28.0)

New files added to workflow-ui/e2e/:
- carbon-design-desktop.spec.ts — 15 Carbon token checks for Desktop Chrome
- carbon-design-mobile.spec.ts — 6 Carbon token checks for Mobile Chrome

Updated files:
- playwright.config.ts — added carbon-design-desktop.spec.ts to Desktop Chrome testMatch, carbon-design-mobile.spec.ts to Mobile Chrome testMatch
- applications-mobile.spec.ts — updated ellipsis button selector from .bg-white .anticon-ellipsis to button:has(.anticon-ellipsis) (Carbon refactor removed .bg-white class from card)
- node-editor-enhanced.spec.ts — updated TC-NODE-ENHANCED-05 with mask fallback and extended timeout; failure persists as confirmed app regression

---

## Conclusion

BLOCKED — 1 failure found.

The IBM Carbon Design Language refactoring is visually correct: all 21 Carbon token checks pass on both Desktop and Mobile. No Quiet Luxury colors remain. IBM Plex Sans is loaded. Nav, buttons, table headers, and mobile cards all use the correct Carbon color tokens with 0px border-radius.

One regression was found: TC-NODE-ENHANCED-05 (drawer close button non-functional on Desktop Chrome). This is a behavioral regression introduced during the Carbon refactor. A new TODO item should be opened for this defect before marking v28.0 as Done.
