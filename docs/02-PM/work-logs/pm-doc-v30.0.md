# Product Requirements Document v30.0

**Document version:** 30.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-uat-e2e-retrofit-5layer-validation`

## Overview

Retrofit existing Playwright E2E test suite with comprehensive five-layer validation framework to ensure tests verify not just element existence but complete UX quality including size, viewport visibility, interaction capability, and visual/style correctness.

## Problem Statement

Current E2E suite can report 100% pass while missing critical UX defects:
- Elements exist in DOM but are invisible (zero size, clipped by viewport)
- Elements are visible but not interactive (pointer-events: none, z-index issues)
- Elements render but with wrong styles (colors, spacing, typography not matching design system)
- Mobile drawers/modals appear but don't meet minimum size requirements

**Example:** A drawer can pass `toBeVisible()` assertion while being only 10% viewport height when spec requires >35%.

## User Stories

**US-E2E-30.1:** As a QA engineer, I want E2E tests to validate element dimensions so that size regressions are caught automatically.

**US-E2E-30.2:** As a QA engineer, I want E2E tests to verify viewport visibility so that clipped or off-screen content is detected.

**US-E2E-30.3:** As a QA engineer, I want E2E tests to confirm interaction capability so that non-clickable elements fail tests.

**US-E2E-30.4:** As a QA engineer, I want E2E tests to check computed styles so that design system violations are caught.

**US-E2E-30.5:** As a mobile QA engineer, I want drawer/modal height validation on mobile viewport so that unusable UI is blocked.

## Acceptance Criteria

**AC-E2E-30.1:** Five-layer validation framework documented
- Layer 1 (Exist): element in DOM
- Layer 2 (Size): `boundingBox()` dimensions meet spec (e.g., drawer height >35% viewport)
- Layer 3 (Viewport): `toBeInViewport()` / not clipped
- Layer 4 (Interact): real click/type, not only `toBeVisible()`
- Layer 5 (Effect): post-condition + computed styles, tokens, or `toHaveScreenshot()` where stable

**AC-E2E-30.2:** Priority surfaces upgraded with layer 2-5 assertions
- Mobile drawers (node editor, app overflow actions)
- Mobile modals (Delete confirm, Explain, Generate, JsonPath)
- Forms (New Application, node editor form fields)
- Critical desktop modals

**AC-E2E-30.3:** Test report documents coverage
- Which test cases upgraded to which layers
- Representative examples of each layer type
- Gaps remaining (if any)

**AC-E2E-30.4:** No regressions in existing test pass rate
- All previously passing tests continue to pass
- New assertions only added where spec is clear

## Scope

**In scope:**
- Upgrade existing test specs with layer 2-5 assertions
- Document validation framework in test-doc
- Add representative examples to E2E test report
- Focus on mobile-first surfaces (drawers, modals, forms)

**Out of scope:**
- New test cases (only retrofit existing ones)
- Backend API validation
- Performance/load testing
- Cross-browser testing beyond Desktop/Mobile Chrome

## References

- `specs/uat-report-e2e-pass3.md` — prior UAT findings
- `docs/e2e-testing-guide.md` — existing E2E patterns
- `docs/test-doc-v29.0.md` — current test cases
- `docs/e2e-test-report-v29.0.md` — baseline test results

## Success Metrics

- At least 10 test cases upgraded with layer 2 (size) assertions
- At least 5 test cases upgraded with layer 5 (style/screenshot) assertions
- Zero regression in existing pass rate
- Documentation complete in test-doc and E2E report
