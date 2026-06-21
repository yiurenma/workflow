# PM Doc v24.0 — Node Editor Accessibility Violations

**Status:** Draft
**Label:** `TODO-uat-e2e-node-drawer-accessibility-violations`
**Date:** 2026-04-11

---

## Problem Statement

The node editor drawer fails two WCAG 2.1 accessibility checks discovered during E2E testing (TC-NODE-ENHANCED-01):

1. **Missing `aria-label`** — Screen readers cannot identify the drawer's purpose (WCAG 2.1 Level A violation)
2. **Insufficient color contrast** — Header text has 2.51:1 contrast ratio, below the 4.5:1 minimum for normal text (WCAG 2.1 Level AA violation)

These violations prevent screen reader users from understanding the drawer context and make the interface difficult to read for users with low vision or color blindness.

---

## User Stories

### CV-US-32 — Node editor drawer is accessible to screen reader users

**As a** screen reader user editing a workflow node,
**I want** the node editor drawer to announce its purpose when opened,
**So that** I understand what region I'm interacting with and can navigate the form efficiently.

**Acceptance Criteria:**

**CV-AC-32-1:** The node editor drawer has `aria-label="Node Configuration"` on its container element.

**CV-AC-32-2:** When the drawer opens, screen readers (NVDA, JAWS, VoiceOver) announce "Node Configuration" or equivalent localized text.

**CV-AC-32-3:** The fix applies to both desktop (420px wide drawer) and mobile (full-width drawer) viewports.

**CV-AC-32-4:** Axe DevTools or similar accessibility scanner reports zero violations for missing `aria-label` on the drawer.

### CV-US-33 — Node editor header text meets WCAG AA contrast requirements

**As a** user with low vision or color blindness,
**I want** the node editor header text to have sufficient contrast against its background,
**So that** I can read the node type and configuration labels without strain.

**Acceptance Criteria:**

**CV-AC-33-1:** The header text color is changed from the current value to `#6b6b75` or darker to achieve at least 4.5:1 contrast ratio against the background.

**CV-AC-33-2:** The contrast ratio is verified using WebAIM Contrast Checker or browser DevTools and meets WCAG 2.1 Level AA standards for normal text.

**CV-AC-33-3:** The new color applies to all text in the node editor header (node type label, section titles).

**CV-AC-33-4:** Desktop and mobile viewports both meet the contrast requirement.

**CV-AC-33-5:** Axe DevTools or similar accessibility scanner reports zero color contrast violations in the node editor drawer.

---

## Out of Scope

- Full WCAG 2.1 AAA compliance (only Level A and AA violations are addressed)
- Keyboard navigation improvements beyond the current implementation
- Focus management or focus trap behavior changes
- High contrast mode or Windows High Contrast support
- Other accessibility issues not identified in TC-NODE-ENHANCED-01
- Localization of `aria-label` text (English only for this iteration)

---

## Change Summary

This is a defect fix addressing accessibility violations found during UAT E2E testing. No new features are introduced. The changes are scoped to:

1. Adding one `aria-label` attribute to the drawer container
2. Changing one CSS color value for header text

Both changes are non-breaking and do not affect existing functionality or layout.
