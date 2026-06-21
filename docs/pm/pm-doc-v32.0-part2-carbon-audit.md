# Product Requirements — Workflow Studio Unified Program Part 2: IBM Carbon Audit (v32.0)

**Document version:** 32.0 Part 2  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** IBM Carbon residual styling audit + Playwright blind spots  
**Master label:** `TODO-workflow-studio-unified-product-ux-program`  
**Part label:** `TODO-ui-ibm-carbon-audit-residual-styling`

---

## 1. Overview

After v28.0 IBM Carbon refactor shipped, some surfaces still exhibit legacy Ant Design or Quiet Luxury styling (wrong border-radius, colors, button variants). This document defines the audit process and remediation approach.

**Context:** v28.0 (`TODO-ui-design-ibm-design-language-refactoring`, `arch-doc-v28.0.md`) migrated the UI to IBM Carbon Design System, but some paths were missed or added after the migration.

---

## 2. User Stories

### US-CARBON-01 — All UI surfaces follow IBM Carbon Design System

**As a** user,  
**I want** all UI surfaces (modals, buttons, forms, dropdowns) to follow IBM Carbon Design System consistently,  
**So that** the product has a cohesive, professional appearance without visual inconsistencies.

**Acceptance Criteria:**

**AC-CARBON-01-01:** **Audit deliverable** — Numbered gap list produced covering:
- Route / screen / component file
- Screenshot showing non-Carbon styling
- Severity (HIGH / MEDIUM / LOW)
- Root cause category (imperative Modal.confirm, portal without Carbon class, residual Tailwind, etc.)

**AC-CARBON-01-02:** **Audit scope** — All user-facing surfaces:
- **Applications:** Table, mobile cards, Settings modal, History modal, Copy modal, Delete confirm
- **Records:** Table, mobile cards, filters
- **Canvas:** Toolbar, WorkflowDrawer, JsonPath modal, Explain modal, Generate modal
- **Toasts / notifications**
- **Empty states**
- **All `Modal.confirm` / `Popconfirm` paths**

**AC-CARBON-01-03:** **Reported example validated** — Delete flow for an application (table row or overflow) uses Carbon-aligned confirm dialog or actions.

**AC-CARBON-01-04:** **Typical root causes addressed:**
- Imperative `Modal.confirm` bypassing themed wrapper → replaced with declarative `<Modal>` or Carbon-wrapped confirm
- `Dropdown` / `Tooltip` / `Popconfirm` in portal without Carbon `className` → add Carbon classes
- Residual Tailwind or inline styles → replace with `--cds-*` tokens
- Paths missed in v28 sweep → apply Carbon patterns
- Features added after v28 without Carbon review → retrofit Carbon

**AC-CARBON-01-05:** **Fix direction applied:**
- Use `--cds-*` tokens for colors, spacing, typography
- Apply Carbon modal / danger button patterns
- Prefer declarative `<Modal>` over imperative `Modal.confirm`
- Align typography and spacing to Carbon grid

---

### US-CARBON-02 — Playwright tests validate Carbon compliance where feasible

**As a** QA engineer,  
**I want** E2E tests to validate Carbon styling on critical surfaces,  
**So that** future changes don't regress Carbon compliance.

**Acceptance Criteria:**

**AC-CARBON-02-01:** **Root cause analysis** — Document why v28 E2E tests didn't catch Carbon gaps:
- E2E does not infer design systems — fails only on asserted behavior or visuals
- If tests never open Delete confirm, or only assert `toBeVisible()` with no Layer 5 (computed color, border-radius, Carbon class substring, or screenshot), pass ≠ Carbon compliance

**AC-CARBON-02-02:** **Deliverable** — List which v28-era tests lacked Layer 5 assertions for Carbon compliance.

**AC-CARBON-02-03:** **Enhancement** — Add per-modal checks or visual baselines for critical dialogs:
- Delete confirm: validate `border-radius: 0px`, primary button `background-color: #0f62fe` (Blue 60)
- Settings modal: validate header uses IBM Plex Sans, `--cds-text-primary` color
- JsonPath modal: validate Carbon button styles

**AC-CARBON-02-04:** **Scope** — Focus on high-visibility surfaces; not every component needs visual regression tests.

---

## 3. Out of Scope

- Redesigning UX flows (only fixing styling)
- Backend API changes
- Performance optimization
- New features

---

## 4. Success Metrics

- Gap list produced with ≥10 identified non-Carbon surfaces
- All HIGH severity gaps fixed
- Spot-check UAT confirms Carbon compliance on Applications Delete, Settings, History flows
- Optional: new UI test report version documents Carbon validation coverage

---

## 5. References

- `arch-doc-v28.0.md` — IBM Carbon refactor baseline
- `e2e-test-report-v28.0.md` — v28 E2E results (82/84 pass)
- IBM Carbon Design System: https://carbondesignsystem.com/
