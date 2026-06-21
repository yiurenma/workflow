# Product Requirements Document v25.0

**Document version:** 25.0  
**Date:** 2026-04-11  
**Status:** Draft  
**TODO label:** `TODO-node-editor-form-descriptions-color-contrast`

## Overview

Fix insufficient color contrast in form field description text within the node editor drawer to meet WCAG 2.1 Level AA accessibility standards.

## Background

During TC-NODE-ENHANCED-01 testing after fixing the drawer header, form field description text was found to have insufficient color contrast:
- Current: `text-zinc-400` (#a1a1aa) provides 2.62:1 contrast on white background and 2.51:1 on #fafafa background
- Required: 4.5:1 minimum for WCAG 2.1 Level AA compliance
- Affected components: HttpCallForm and LogicForm

## User Story

**US-CV-25.1:** As a user with visual impairments or working in bright lighting conditions, I need form field descriptions in the node editor to have sufficient color contrast so that I can read the guidance text without strain.

## Acceptance Criteria

**AC-CV-25.1:** Form field description text color changed from `text-zinc-400` to `text-zinc-600` in the following 3 description paragraphs:
1. "The name shown on the canvas..." (node name description)
2. "Run only when..." (rules description)
3. "What the system does..." (action description)

**AC-CV-25.2:** Color contrast ratio meets or exceeds 4.5:1 on both white (#ffffff) and light gray (#fafafa) backgrounds

**AC-CV-25.3:** Visual appearance remains consistent with the overall design system (text should still appear as secondary/helper text, not primary content)

**AC-CV-25.4:** No other styling changes to the affected components

## Scope

- **In scope:** 
  - HttpCallForm component description text
  - LogicForm component description text
  - Color contrast validation

- **Out of scope:**
  - Other form components not mentioned
  - Other accessibility improvements beyond this specific color contrast issue
  - Backend changes (frontend-only fix)

## Success Metrics

- All 3 description paragraphs pass WCAG 2.1 Level AA color contrast requirements
- No visual regression in other parts of the node editor
- Accessibility audit tools report no color contrast violations for these elements

## References

- Source: `specs/uat-report-e2e-pass3.md`
- Related: `docs/uat-report-v24.0.md` (previous accessibility fixes)
- WCAG 2.1 Level AA: https://www.w3.org/WAI/WCAG21/quickref/#contrast-minimum
