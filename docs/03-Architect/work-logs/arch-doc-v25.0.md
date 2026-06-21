# Architecture Document v25.0

**Document version:** 25.0  
**Date:** 2026-04-11  
**Status:** Draft  
**Related PM doc:** pm-doc-v25.0.md  
**TODO label:** `TODO-node-editor-form-descriptions-color-contrast`

## Overview

This document describes the technical approach to fix insufficient color contrast in form field description text within the node editor drawer components.

## Problem Statement

Form field description text in HttpCallForm and LogicForm components uses `text-zinc-400` (#a1a1aa), which provides:
- 2.62:1 contrast ratio on white (#ffffff) background
- 2.51:1 contrast ratio on #fafafa background

WCAG 2.1 Level AA requires a minimum 4.5:1 contrast ratio for normal text.

## Affected Components

### 1. HttpCallForm Component
**Location:** `workflow-ui/src/components/workflow-drawer/HttpCallForm.tsx`

Three description paragraphs:
1. Node name field description: "The name shown on the canvas..."
2. Rules section description: "Run only when..."
3. Action section description: "What the system does..."

### 2. LogicForm Component
**Location:** `workflow-ui/src/components/workflow-drawer/LogicForm.tsx`

Same three description paragraphs with identical structure.

## Technical Solution

### Color Change Specification

**Current:** `text-zinc-400` → `#a1a1aa` (RGB: 161, 161, 170)  
**New:** `text-zinc-600` → `#52525b` (RGB: 82, 82, 91)

### Contrast Ratios (New Color)

On white background (#ffffff):
- Contrast ratio: 7.0:1 ✓ (exceeds 4.5:1 minimum)

On light gray background (#fafafa):
- Contrast ratio: 6.7:1 ✓ (exceeds 4.5:1 minimum)

### Implementation Approach

**Strategy:** Direct CSS class replacement

For each of the 3 description paragraphs in both components:
```tsx
// Before
<p className="text-xs text-zinc-400 mb-3">Description text...</p>

// After
<p className="text-xs text-zinc-600 mb-3">Description text...</p>
```

**Scope:** Only the `text-zinc-400` class needs to be changed to `text-zinc-600`. All other classes (`text-xs`, `mb-3`) remain unchanged.

## Files to Modify

1. `workflow-ui/src/components/workflow-drawer/HttpCallForm.tsx`
   - Line containing "The name shown on the canvas..."
   - Line containing "Run only when..."
   - Line containing "What the system does..."

2. `workflow-ui/src/components/workflow-drawer/LogicForm.tsx`
   - Line containing "The name shown on the canvas..."
   - Line containing "Run only when..."
   - Line containing "What the system does..."

## Validation Approach

### Automated Validation
- Axe DevTools accessibility scan should report zero color contrast violations for these elements
- Browser DevTools contrast checker should confirm ≥4.5:1 ratio

### Manual Validation
- Visual inspection: text should remain visually distinct as secondary/helper text
- No visual regression in other parts of the node editor
- Consistent appearance across desktop and mobile viewports

## Non-Goals

- No changes to other form components beyond HttpCallForm and LogicForm
- No changes to other styling properties (font size, spacing, etc.)
- No backend or API changes required
- No changes to component structure or behavior

## Risk Assessment

**Risk Level:** LOW

**Rationale:**
- CSS-only change with no logic modifications
- Affects only visual presentation
- No breaking changes to component APIs
- No database or backend dependencies

**Mitigation:**
- Visual regression testing on both desktop and mobile
- Accessibility audit with automated tools
- Manual review of affected components

## Testing Strategy

See test-doc-v25.0.md for detailed test cases.

**Key test scenarios:**
1. Color contrast measurement on white background
2. Color contrast measurement on #fafafa background
3. Visual consistency check (text still appears as secondary content)
4. No regression in other node editor elements
5. Accessibility tool validation (zero violations)

## Deployment Notes

- Frontend-only change
- No database migrations required
- No API version changes
- Safe to deploy independently
- No feature flags needed

## References

- WCAG 2.1 Level AA Contrast Requirements: https://www.w3.org/WAI/WCAG21/quickref/#contrast-minimum
- WebAIM Contrast Checker: https://webaim.org/resources/contrastchecker/
- Tailwind CSS zinc color palette: https://tailwindcss.com/docs/customizing-colors
- Source issue: `specs/uat-report-e2e-pass3.md`
- Related fix: `docs/uat-report-v24.0.md` (drawer header accessibility)
