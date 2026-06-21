# Product Requirements Document v34.0

**Document Version:** 34.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Label:** `TODO-e2e-fix-test-failures-post-v30`

---

## 1. Background

After implementing P0/P1/P2 fixes for E2E test failures (v33.0), a full test suite run reveals 8 remaining failures (99/107 pass, 92.5% pass rate). Analysis shows all 8 failures are caused by test specs not adapting to v31.0's "read-first mode" feature.

**Root cause:** v31.0 changed node editor drawer to open in read-only view by default, requiring users to click "Edit" button to enter edit mode. Test specs written before v31.0 assume drawer opens in edit mode.

---

## 2. User Story

**As** an E2E test maintainer  
**I want** test specs to correctly handle the read-first mode UX flow  
**So that** the test suite accurately validates the current application behavior and achieves 100% pass rate

---

## 3. Acceptance Criteria

### AC-01: Node Editor Test Specs Updated (4 tests)

Tests must click "Edit" button before asserting editability:
- TC-DRAWER-CLOSE-08, TC-DRAWER-CLOSE-10
- TC-NODE-ENHANCED-01, TC-NODE-ENHANCED-04

### AC-02: Carbon Design Table Viewport Fixed (1 test)

TC-CARBON-DESK-06 must scroll table into view before assertion.

### AC-03: FAB Size Expectation Updated (1 test)

TC-CANVAS-MOB-01 must expect ≥48px (Carbon standard), not ≥56px.

### AC-04: Full Test Suite Pass

107/107 tests pass (100% pass rate).

---

## 4. Success Metrics

- Pass rate: 107/107 (100%)
- Flaky tests: 0
