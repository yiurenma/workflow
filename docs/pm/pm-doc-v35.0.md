# Product Requirements Document — v35.0

**Document version:** 35.0  
**Date:** 2026-04-13  
**Status:** Draft  
**TODO item:** `TODO-ui-ibm-carbon-audit-residual-styling`  
**Label:** UI parity — IBM Carbon residual styling + Playwright blind spots

---

## 1. Background

The v28.0 IBM Carbon Design Language refactoring (`TODO-ui-design-ibm-design-language-refactoring`) successfully migrated the majority of Workflow Studio UI to IBM Carbon standards. However, user reports and internal review have identified surfaces that still exhibit legacy Ant Design or Quiet Luxury styling patterns (incorrect border-radius, colors, button variants).

**Reported example:** The **Delete** flow for an **application** (table row action or overflow menu) — the confirmation dialog does not follow Carbon modal patterns.

**Root cause hypothesis:**
- Imperative `Modal.confirm` calls bypass the themed wrapper
- Portal-rendered components (Dropdown, Tooltip, Popconfirm) lack Carbon `className` injection
- Paths missed during v28 sweep or features added post-v28 without Carbon review
- Residual Tailwind utility classes or inline styles

---

## 2. Objectives

1. **Audit:** Produce a comprehensive, numbered gap list of all UI surfaces that do not conform to IBM Carbon Design Language
2. **Fix:** Apply Carbon design tokens (`--cds-*`), modal/button patterns, and remove non-Carbon styling
3. **Test coverage:** Document why existing Playwright tests did not catch these gaps and add assertions to prevent regression

---

## 3. User Stories

### US-49 — Carbon-compliant confirmation dialogs

> **As a** user deleting an application or performing destructive actions,  
> **I want** confirmation dialogs to follow IBM Carbon modal patterns,  
> **So that** the interface feels consistent and professional across all flows.

**Acceptance Criteria:**

**AC-49-1:** All `Modal.confirm` and `Popconfirm` instances use Carbon modal styling:
- 0px border-radius (rectangular)
- Primary button: Blue 60 `#0f62fe` background, 48px height
- Danger button: Red 60 `#da1e28` background, 48px height
- Modal header: Gray 100 `#161616` text, IBM Plex Sans weight 400
- Modal body: Gray 70 `#525252` text, 16px size
- Backdrop: `rgba(22, 22, 22, 0.5)` dark overlay

**AC-49-2:** Application delete confirmation (desktop table row action + mobile overflow menu) uses Carbon danger modal pattern

**AC-49-3:** All other destructive actions (workflow delete, record delete, etc.) follow same pattern

**AC-49-4:** Declarative `<Modal>` components preferred over imperative `Modal.confirm` where feasible

---

### US-50 — Carbon-compliant portal components

> **As a** user interacting with dropdowns, tooltips, and popovers,  
> **I want** these components to follow Carbon styling,  
> **So that** the interface maintains visual consistency even for portal-rendered elements.

**Acceptance Criteria:**

**AC-50-1:** All `Dropdown`, `Tooltip`, `Popconfirm`, and portal-rendered components apply Carbon classes and tokens:
- Background: Gray 10 `#f4f4f4` for surfaces, Gray 100 `#161616` for dark variants
- Text: Gray 100 `#161616` for primary, Gray 70 `#525252` for secondary
- Border: Gray 30 `#c6c6c6`, 1px solid
- Shadow: `0 2px 6px rgba(0,0,0,0.3)` for elevated surfaces
- 0px border-radius (rectangular)

**AC-50-2:** Dropdown menus in application table overflow, mobile navigation, and canvas toolbar follow Carbon patterns

**AC-50-3:** Tooltips use Carbon typography (14px, IBM Plex Sans, Gray 100 text on Gray 10 background)

---

### US-51 — Audit report and test coverage gaps

> **As a** QA engineer,  
> **I want** a documented audit of Carbon compliance gaps and test coverage improvements,  
> **So that** we can prevent future regressions and understand why existing tests missed these issues.

**Acceptance Criteria:**

**AC-51-1:** Audit produces a numbered gap list with:
- Route/screen name
- Component file path
- Screenshot showing non-Carbon styling
- Severity (P0: user-facing critical, P1: user-facing minor, P2: internal/edge case)

**AC-51-2:** Audit covers:
- Applications (table, mobile cards, Settings modal, History modal, Copy modal, Delete confirmation)
- Records (table, mobile cards, detail view)
- Canvas (toolbar, WorkflowDrawer, JsonPath modal, Explain modal, Generate modal)
- Toasts and empty states
- All `Modal.confirm` / `Popconfirm` paths

**AC-51-3:** Test coverage analysis documents:
- Which v28-era Playwright tests lacked Layer 5 (computed style) assertions
- Which flows were never opened in E2E tests (e.g., Delete confirmation)
- Recommendations for per-modal checks or visual baseline screenshots

**AC-51-4:** New or updated Playwright test cases added to prevent regression of fixed gaps

---

## 4. Scope

**In scope:**
- Audit all user-facing UI surfaces for Carbon compliance
- Fix identified gaps by applying Carbon tokens and patterns
- Update Playwright tests to catch future regressions
- Document root causes and test coverage gaps

**Out of scope:**
- New features or functionality changes
- Backend API changes
- Performance optimization
- Accessibility improvements beyond Carbon baseline (already covered in v24.0, v25.0)

---

## 5. Success Metrics

- **Gap list:** All identified gaps documented with severity and root cause
- **Fix completion:** 100% of P0 and P1 gaps resolved, P2 gaps resolved or documented as acceptable
- **Test coverage:** Playwright tests updated to include Layer 5 assertions for critical modals and portal components
- **UAT verification:** Spot-check on https://workflow-ui-gamma.vercel.app confirms Carbon compliance

---

## 6. Dependencies

- v28.0 IBM Carbon refactoring baseline
- v30.0 five-layer validation framework
- Existing Playwright test suite

---

## 7. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Audit reveals extensive gaps requiring major refactor | Prioritize P0/P1 gaps; defer P2 to future iteration |
| Imperative `Modal.confirm` difficult to theme | Create Carbon-themed wrapper or migrate to declarative `<Modal>` |
| Portal components bypass theme context | Inject Carbon classes via `popupClassName` / `overlayClassName` props |
| Test updates introduce flakiness | Use stable selectors and wait strategies from v30.0 framework |

---

## 8. Timeline

- **Audit:** 1 day (PM + QA)
- **Architecture review:** 0.5 day (Architect)
- **Test case design:** 0.5 day (Test Manager)
- **Human approval gate:** (blocking)
- **Implementation:** 2-3 days (Frontend dev)
- **QA verification:** 1 day (QA)
- **E2E testing:** 0.5 day (E2E Tester)
- **UAT:** 0.5 day (E2E Tester on gamma environment)

---

## 9. Appendix

### Example: Application Delete Confirmation (Current vs. Expected)

**Current (non-Carbon):**
- Rounded corners (4px or 8px border-radius)
- Generic button colors (Ant Design primary blue)
- Inconsistent modal header styling

**Expected (Carbon):**
- 0px border-radius (rectangular modal)
- Danger button: Red 60 `#da1e28` background
- Modal header: Gray 100 `#161616` text, IBM Plex Sans
- Backdrop: `rgba(22, 22, 22, 0.5)`

---

**End of PM Document v35.0**
