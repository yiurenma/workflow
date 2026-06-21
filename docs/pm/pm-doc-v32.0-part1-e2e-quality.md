# Product Requirements — Workflow Studio Unified Program Part 1: E2E Quality (v32.0)

**Document version:** 32.0 Part 1  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** E2E drawer close fix + 5-layer validation retrofit  
**Master label:** `TODO-workflow-studio-unified-product-ux-program`  
**Part labels:** `TODO-uat-e2e-node-drawer-close-button-broken`, `TODO-uat-e2e-retrofit-5layer-validation`

---

## 1. Overview

This document covers the first two items of the unified Workflow Studio program, focused on improving E2E test quality and fixing a critical mobile UX defect.

**Goals:**
1. Fix node editor drawer close button (HIGH priority — mobile blocker)
2. Retrofit 5-layer validation framework into existing E2E suite

---

## 2. User Stories

### US-E2E-01 — Node editor drawer close button works reliably

**As a** mobile user,  
**I want** the drawer close button (×) to close the node editor drawer with one tap,  
**So that** I can return to the canvas without needing workarounds like tapping outside or using ESC (which mobile lacks).

**Acceptance Criteria:**

**AC-E2E-01-01:** On **Desktop Chrome 1280px**, clicking `.ant-drawer-close` closes the node editor drawer immediately; drawer is removed from DOM or not visible; canvas becomes interactive.

**AC-E2E-01-02:** On **Mobile Chrome 390×844**, tapping `.ant-drawer-close` closes the node editor drawer immediately; drawer is removed from DOM or not visible; canvas becomes interactive.

**AC-E2E-01-03:** Close action works consistently across all node types (HTTP fetch, Safe HTTP fetch, Logic, etc.).

**AC-E2E-01-04:** After close, focus returns to canvas; user can immediately interact with nodes or edges.

**AC-E2E-01-05:** Playwright test case TC-NODE-ENHANCED-05 (or new dedicated close test) validates **Layer 5 effect**: drawer absent from DOM or `visibility: hidden` + canvas interactive.

---

### US-E2E-02 — Five-layer validation framework in E2E suite

**As a** QA engineer / developer,  
**I want** E2E tests to validate existence, size, viewport visibility, interaction, and visual/style effects,  
**So that** 100% pass rate guarantees actual UX quality, not just DOM presence.

**Acceptance Criteria:**

**AC-E2E-02-01:** **Layer 1 (Exist):** Element present in DOM — baseline for all tests.

**AC-E2E-02-02:** **Layer 2 (Size):** Validate dimensions where spec requires (e.g., mobile drawer height >35% viewport, button min-height 44px touch target).

**AC-E2E-02-03:** **Layer 3 (Viewport):** Use `toBeInViewport()` or equivalent; ensure critical content not clipped outside viewport.

**AC-E2E-02-04:** **Layer 4 (Interact):** Real click/type actions, not only `toBeVisible()`; validate event handlers fire.

**AC-E2E-02-05:** **Layer 5 (Effect):** Post-condition validation + computed styles, tokens, or `toHaveScreenshot()` where stable. Examples:
- Drawer close → drawer absent from DOM
- Button click → modal opens with correct `background-color`
- Form submit → success toast visible with correct text

**AC-E2E-02-06:** Priority surfaces for retrofit:
- Mobile drawers/modals (node editor, app overflow, add-node sheet)
- Forms (new application, settings, node configuration)
- Critical modals (Delete confirm, Explain, Generate, JsonPath)
- App overflow menus (History, Copy, Delete)

**AC-E2E-02-07:** Documentation in `e2e-test-report-v32.0-part1.md` lists:
- Which test cases upgraded to 5-layer
- Representative examples of each layer
- Coverage gaps (surfaces not yet upgraded)

**AC-E2E-02-08:** Reference template in `specs/uat-report-e2e-pass3.md` Appendix A (if present) used as guide.

---

## 3. Out of Scope

- Backend API changes
- New features beyond fixing drawer close
- Visual redesign (Carbon compliance handled in Part 2)
- Performance optimization

---

## 4. Success Metrics

- TC-NODE-ENHANCED-05 (or new close test) passes on Desktop + Mobile
- At least 10 existing test cases upgraded to include Layer 5 assertions
- Zero regressions in existing 82/84 pass rate (excluding pre-existing drawer close defect)

---

## 5. References

- `specs/uat-report-e2e-pass3.md` — UAT findings that identified drawer close defect
- `docs/e2e-testing-guide.md` — E2E testing conventions
- `docs/test-doc-v28.0.md` — Current E2E baseline (82/84 pass)
