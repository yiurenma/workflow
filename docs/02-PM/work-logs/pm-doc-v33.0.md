# PM Document v33.0 — E2E Test Failure Remediation

**Document version:** 33.0  
**Date:** 2026-04-12  
**Status:** Approved  
**Recognition label:** `TODO-e2e-fix-test-failures-post-v30`

## Executive Summary

Fix remaining E2E test failures from the full suite run after v30.0 five-layer validation retrofit. Current state shows significant failures in applications-desktop and carbon-design-desktop test suites, primarily due to mock data loading issues and Carbon Design System validation failures.

## Problem Statement

After implementing the five-layer validation framework in v30.0, a full E2E test suite run revealed 44 failures out of 106 tests (57.5% pass rate). These failures fall into three priority categories:

### P0 — Mock Data Loading Issues (22 failures)
Applications list tests are failing because content is not loading properly. Tests navigate to `/workflows/` but the table/cards don't render with mock data.

**Affected test cases:**
- TC-APP-DESK-01 through TC-APP-DESK-10 (all applications-desktop tests)
- Navigation tests that depend on applications list

**Root cause hypothesis:** Tests are not waiting for `networkidle` or content selectors after `page.goto()`. The mock route handlers may not be matching actual API endpoints.

### P1 — Carbon Design System Validation (16 failures)
Carbon Design System validation tests are failing because the UI is not fully implementing Carbon tokens and specifications.

**Affected test cases:**
- TC-CARBON-DESK-01 through TC-CARBON-DESK-14 (desktop Carbon tests)
- TC-CARBON-MOB-01 through TC-CARBON-MOB-05 (mobile Carbon tests)

**Root cause:** UI components are not applying `--cds-*` CSS custom properties, Ant Design defaults are not overridden, and sizing specifications (nav height ≥48px, button sizes ≥40×44px) are not met.

### P2 — Component Interaction Issues (6 failures)
- Node editor drawer animation timing (2 failures)
- Mobile drawer scroll bounds (2 failures)
- Canvas mobile FAB visibility (2 failures)

## User Stories

### US-33.1: Mock Data Loading Reliability
**As a** QA engineer  
**I want** E2E tests to reliably load mock data  
**So that** application list tests pass consistently

**Acceptance Criteria:**
- AC-33.1.1: All applications-desktop tests wait for `networkidle` after navigation
- AC-33.1.2: Mock route handlers match actual API endpoint patterns
- AC-33.1.3: Tests verify content presence before asserting on specific elements
- AC-33.1.4: Debug logging added to identify mock route mismatches

### US-33.2: Carbon Design System Compliance
**As a** frontend developer  
**I want** UI components to fully implement Carbon Design System  
**So that** design system validation tests pass

**Acceptance Criteria:**
- AC-33.2.1: Navigation bar uses Carbon Gray 100 (#161616) background
- AC-33.2.2: Navigation bar height is ≥48px (Carbon shell spec)
- AC-33.2.3: Primary action buttons are ≥40px height and ≥44px width
- AC-33.2.4: All color values use `--cds-*` CSS custom properties
- AC-33.2.5: Border radius is 0px (Carbon spec, not Ant Design default)

### US-33.3: Component Interaction Stability
**As a** QA engineer  
**I want** component interactions to be stable and predictable  
**So that** interaction tests pass reliably

**Acceptance Criteria:**
- AC-33.3.1: Node editor drawer animations complete before assertions
- AC-33.3.2: Mobile drawer scroll bounds are correctly calculated
- AC-33.3.3: Canvas mobile FAB is visible and interactive

## Success Criteria

- **Pass rate 100% (106/106 tests passing)**
- All P0 issues resolved (mock data loading)
- All P1 issues resolved (Carbon Design System compliance)
- All P2 issues resolved (component interactions)
- Test artifacts (HTML report, screenshots) available for review
- Zero flaky tests - all tests pass consistently

## Out of Scope

- Adding new test cases (focus is on fixing existing failures)
- Refactoring test infrastructure (unless required for fixes)
- Visual regression testing with screenshots (too brittle)

## References

- Prior E2E report: `e2e-test-report-v30.0.md`
- TODO item: Section 2.1 in `TODO.md`
- Test artifacts: `workflow-ui/playwright-report/index.html`
