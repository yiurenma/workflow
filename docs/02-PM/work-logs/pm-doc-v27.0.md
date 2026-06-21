# Product Requirements Document v27.0

**Document version:** 27.0  
**Date:** 2026-04-11  
**Status:** Draft  
**TODO label:** `TODO-ui-design-quiet-luxury-refactoring-frontend-skill`

## Overview

Refactor Workflow Studio UI from generic tech aesthetic to sophisticated "Quiet Luxury" design system. This is a visual-only refactoring that preserves all business logic and functionality while elevating the interface to convey warmth, sophistication, and refined minimalism.

## Background

Current UI uses standard tech stack defaults (Inter font, cold grays, generic spacing). The platform needs a distinctive visual identity that signals quality and attention to detail without sacrificing usability or accessibility.

**Design Direction:** "Quiet Luxury" — sophisticated, warm minimalism characterized by:
- Distinctive typography (move beyond Inter)
- Earth tones and warm neutrals vs cold tech grays
- Generous, intentional spacing and hierarchy
- Refined details and subtle motion
- Elevated but not ostentatious

## User Stories

### US-UI-27.1: Typography System

**As a** user of Workflow Studio, **I want** the interface to use distinctive, readable typography **so that** the platform feels polished and professional rather than generic.

**Acceptance criteria:**

**AC-UI-27.1.1:** Replace Inter font with a distinctive pairing:
- Headings: serif or refined sans-serif (e.g., Crimson Pro, Lora, or Instrument Serif for warmth)
- Body: high-quality sans-serif optimized for UI (e.g., Inter remains acceptable if paired with distinctive headings, or consider Manrope, DM Sans)

**AC-UI-27.1.2:** Establish clear typographic hierarchy:
- Page titles: larger, distinctive weight
- Section headers: medium weight, adequate spacing
- Body text: comfortable reading size (14-16px base)
- Helper text: smaller but still legible (12-13px minimum)

**AC-UI-27.1.3:** All text maintains WCAG AA contrast requirements (4.5:1 for body, 3:1 for large text)

### US-UI-27.2: Color Palette — Warm Earth Tones

**As a** user of Workflow Studio, **I want** the interface to use warm, sophisticated colors **so that** the platform feels inviting and human-centered rather than cold and technical.

**Acceptance criteria:**

**AC-UI-27.2.1:** Replace cold grays (zinc-400, zinc-500, zinc-600) with warm neutrals:
- Primary text: warm dark gray or charcoal (#2d2d2d, #3a3a3a range)
- Secondary text: warm mid-gray (#6b6b6b, #7a7a7a range)
- Borders/dividers: soft warm gray (#d4d4d4, #e0e0e0 range)

**AC-UI-27.2.2:** Introduce earth tone accents:
- Primary actions: warm terracotta, clay, or muted gold
- Success states: sage green or olive
- Warning states: amber or ochre
- Error states: muted rust or brick red
- Info states: slate blue or warm gray-blue

**AC-UI-27.2.3:** Background palette:
- Primary background: warm white or cream (#fafaf8, #f8f7f5 range)
- Secondary background: light sand or beige (#f4f3f0, #eeede9 range)
- Card/surface: slightly elevated warm white

**AC-UI-27.2.4:** All color combinations maintain WCAG AA contrast requirements

### US-UI-27.3: Spacing and Hierarchy

**As a** user of Workflow Studio, **I want** the interface to have generous, intentional spacing **so that** content is easy to scan and the platform feels uncluttered.

**Acceptance criteria:**

**AC-UI-27.3.1:** Increase whitespace in key areas:
- Navigation header: more vertical padding (16-20px vs current)
- Application list cards: more internal padding (20-24px vs current)
- Table rows: increased row height (48-56px vs current)
- Form fields: more vertical spacing between fields (16-20px)

**AC-UI-27.3.2:** Establish clear visual hierarchy:
- Page-level spacing: 32-40px between major sections
- Component-level spacing: 16-24px between related elements
- Element-level spacing: 8-12px between tightly coupled items

**AC-UI-27.3.3:** Maintain responsive behavior — spacing scales appropriately on mobile

### US-UI-27.4: Component Refinement

**As a** user of Workflow Studio, **I want** UI components to have refined, sophisticated styling **so that** every interaction feels polished.

**Acceptance criteria:**

**AC-UI-27.4.1:** Navigation header:
- Refined typography for logo/title
- Subtle background (warm white or light cream)
- Soft shadow or border (not harsh)

**AC-UI-27.4.2:** Application list (desktop table):
- Refined table styling (subtle borders, adequate row height)
- Hover states: subtle warm background change
- Action buttons: refined styling consistent with color palette

**AC-UI-27.4.3:** Application cards (mobile):
- Soft shadows (not harsh drop shadows)
- Rounded corners (8-12px)
- Warm background colors
- Clear visual hierarchy within card

**AC-UI-27.4.4:** Buttons:
- Primary: warm accent color, adequate padding
- Secondary: outlined or ghost style with warm colors
- Hover/active states: subtle, refined transitions

**AC-UI-27.4.5:** Status tags:
- Soft, muted colors (not bright/saturated)
- Rounded corners
- Adequate padding for readability

**AC-UI-27.4.6:** Canvas nodes:
- Refined node styling (soft shadows, warm backgrounds)
- Clear typography
- Subtle hover/selection states

**AC-UI-27.4.7:** Drawers and modals:
- Warm backgrounds
- Refined headers with clear typography
- Adequate padding and spacing
- Soft shadows for depth

### US-UI-27.5: Motion and Transitions

**As a** user of Workflow Studio, **I want** UI transitions to be smooth and refined **so that** interactions feel polished and intentional.

**Acceptance criteria:**

**AC-UI-27.5.1:** Establish consistent transition timing:
- Fast interactions (hover, focus): 150-200ms
- Medium interactions (drawer open, modal): 250-300ms
- Slow interactions (page transitions): 300-400ms

**AC-UI-27.5.2:** Use refined easing functions:
- Prefer ease-out or custom cubic-bezier for natural feel
- Avoid linear or harsh easing

**AC-UI-27.5.3:** Apply transitions consistently:
- Button hover states
- Drawer/modal open/close
- Dropdown menus
- Canvas interactions (where appropriate)

### US-UI-27.6: Visual Verification

**As a** developer implementing this refactoring, **I want** clear before/after visual comparison **so that** I can verify the design improvements.

**Acceptance criteria:**

**AC-UI-27.6.1:** Capture baseline screenshots from UAT environment (https://workflow-ui-gamma.vercel.app/workflows):
- Application list (desktop)
- Application list (mobile)
- Canvas view
- Node editor drawer
- Modal dialogs
- Records list

**AC-UI-27.6.2:** Capture comparison screenshots from local development after refactoring (same views)

**AC-UI-27.6.3:** Document visual changes in `STYLE_REFACTOR_LOG.md`:
- Typography changes
- Color palette changes
- Spacing adjustments
- Component refinements
- Before/after screenshot references

## Scope

**In scope:**
- Typography system (font families, sizes, weights, hierarchy)
- Color palette (text, backgrounds, accents, status colors)
- Spacing and layout (padding, margins, whitespace)
- Component styling (navigation, tables, cards, buttons, tags, nodes, drawers, modals)
- Transitions and motion
- Visual documentation

**Out of scope:**
- Business logic changes
- API changes
- New features or functionality
- Component structure changes (unless required for styling)
- Accessibility fixes beyond maintaining current WCAG AA compliance
- Performance optimization (unless styling causes regression)

**Target files:**
- `workflow-ui/src/index.css` (global styles, typography, color variables)
- `workflow-ui/src/routes/__root.tsx` (navigation header)
- `workflow-ui/src/routes/workflows/index.tsx` (application list)
- Canvas components (nodes, edges, controls)
- Drawer components (node editor)
- Modal components
- Button components
- Card components
- Table components

## Success Metrics

- Visual comparison screenshots show clear improvement in sophistication and warmth
- All WCAG AA contrast requirements maintained (4.5:1 for body text, 3:1 for large text)
- No functional regressions (all existing tests pass)
- Build succeeds without errors
- UAT environment deployment succeeds
- Design system documented in `STYLE_REFACTOR_LOG.md`

## Process Requirements

Per `CLAUDE.md` mandatory process:

1. PM writes this doc (current step)
2. Architect writes `arch-doc-v27.0.md` (design system specification)
3. Test Manager writes `test-doc-v27.0.md` (visual regression + accessibility)
4. **STOP** — present all three docs to human for approval
5. Only after approval: Delivery Manager dispatches implementation using `/frontend-design` skill
6. Implementation: Frontend dev refactors styling
7. QA: verify against test cases, write `ui-test-report-v27.0.md`
8. E2E Tester: run Playwright suite, write `e2e-test-report-v27.0.md`
9. Commit + push all repos
10. UAT: Test Manager produces UAT script, human executes, Test Manager writes `uat-report-v27.0.md`
11. Mark TODO as Done only after human confirms UAT acceptable

## References

- TODO label: `TODO-ui-design-quiet-luxury-refactoring-frontend-skill`
- UAT baseline: https://workflow-ui-gamma.vercel.app/workflows
- Design direction: "Quiet Luxury" — sophisticated, warm minimalism
- Implementation requirement: Must use `/frontend-design` skill
