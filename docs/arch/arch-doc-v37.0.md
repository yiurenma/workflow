# Architecture Document — Frontend Rebuild from Design Handoff

**Document version:** 37.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-frontend-rebuild-from-design-handoff`

## 1. Overview

This document describes the technical approach for rebuilding the `workflow-ui` frontend based on a complete design handoff bundle that implements IBM Carbon Design System.

## 2. Design Source Analysis

**Location:** `/tmp/Workflow-handoff/workflow/project/`

**Key artifacts:**
- `Workflow Studio.html` — 1202 lines, complete React application with all screens
- `components.jsx` — 54 lines, design tokens and mock data
- `README.md` — Handoff instructions

**Design system specifications:**
- **Fonts:** IBM Plex Sans (300/400/500/600), IBM Plex Mono (400/500)
- **Colors:** Primary #0f62fe, Nav #161616, Text #161616, Secondary #525252, Borders #c6c6c6
- **Layout:** Zero border-radius, 8px spacing grid, 48px nav height
- **Responsive:** Desktop 1280px, Mobile 390×844 with bottom tab bar

## 3. Technical Approach

### 3.1 Branch Strategy

**Repository:** https://github.com/yiurenma/workflow-ui  
**New branch:** `feature/design-handoff-rebuild` (or similar convention)  
**Base:** Current `main` branch

### 3.2 Implementation Strategy

**Option A: Incremental Migration (RECOMMENDED)**
- Keep existing codebase structure
- Replace components screen-by-screen
- Maintain API integration throughout
- Lower risk, easier to test incrementally

**Option B: Clean Rebuild**
- Start from design handoff HTML/CSS/JS
- Port to React + TypeScript + Vite stack
- Reintegrate API calls
- Higher risk, requires complete retest

**Recommendation:** Option A for lower risk and continuous integration testing.

### 3.3 Component Mapping

| Design Component | Current Component | Action |
|------------------|-------------------|--------|
| Home screen | `src/routes/index.tsx` | Replace with hero + feature cards |
| Applications list | `src/routes/workflows/index.tsx` | Replace table/cards, keep API |
| Canvas editor | `src/routes/workflows/$applicationName.tsx` | Replace layout, keep ReactFlow |
| Node drawer | `src/components/WorkflowDrawer.tsx` | Replace with 3-section design |
| Records list | `src/routes/records/index.tsx` | Replace table/cards, keep API |
| Navigation | `src/components/Header.tsx` | Replace with Carbon nav |

### 3.4 Design Token Integration

**Create:** `src/styles/carbon-tokens.css`

```css
:root {
  /* Colors */
  --cds-text-primary: #161616;
  --cds-text-secondary: #525252;
  --cds-text-tertiary: #6f6f6f;
  --cds-ui-background: #ffffff;
  --cds-ui-01: #f4f4f4;
  --cds-ui-02: #e8e8e8;
  --cds-border-subtle: #c6c6c6;
  --cds-border-strong: #8d8d8d;
  --cds-interactive-primary: #0f62fe;
  --cds-interactive-primary-hover: #0353e9;
  --cds-nav-background: #161616;
  
  /* Typography */
  --cds-font-family-sans: 'IBM Plex Sans', 'Helvetica Neue', Arial, sans-serif;
  --cds-font-family-mono: 'IBM Plex Mono', Menlo, monospace;
  
  /* Spacing (8px grid) */
  --cds-spacing-01: 2px;
  --cds-spacing-02: 4px;
  --cds-spacing-03: 8px;
  --cds-spacing-04: 12px;
  --cds-spacing-05: 16px;
  --cds-spacing-06: 24px;
  --cds-spacing-07: 32px;
  --cds-spacing-08: 40px;
  --cds-spacing-09: 48px;
  
  /* Layout */
  --cds-nav-height: 48px;
  --cds-border-radius: 0px;
}
```

### 3.5 API Integration (No Changes)

**Existing endpoints remain unchanged:**

**operation-api (port 8080):**
- `GET /api/workflow-entity-settings` — List applications
- `POST /api/workflow-entity-settings` — Create application
- `PATCH /api/workflow-entity-settings/{id}` — Update application
- `DELETE /api/workflow-entity-settings/{id}` — Delete application
- `GET /api/workflow-entity-settings/{id}/history` — Revision history
- `GET /api/workflow/{applicationName}` — Get workflow definition
- `POST /api/workflow/{applicationName}` — Save workflow definition

**online-api:**
- `POST /api/workflow?applicationName={name}&confirmationNumber={id}` — Execute workflow
- `GET /api/workflow-records` — List execution records
- `GET /api/workflow-records/{id}` — Record detail

**API client location:** `src/api/` (keep existing axios setup)

### 3.6 State Management

**Keep existing patterns:**
- React hooks for local state
- TanStack Router for routing
- TanStack Query for server state
- Zustand for global UI state (if used)

**No new state management libraries required.**

### 3.7 Responsive Implementation

**Desktop (≥769px):**
- Horizontal navigation bar
- Sidebar for canvas node palette
- Table layouts for applications/records
- Modal dialogs

**Mobile (<769px):**
- Bottom tab bar navigation
- Full-width drawers
- Card layouts for applications/records
- Full-screen modals

**Breakpoint:** 768px (matches design handoff)

## 4. Testing Strategy

### 4.1 Playwright E2E Tests

**Update existing tests:**
- Update selectors for new DOM structure
- Verify all user flows still pass
- Add new tests for design-specific interactions

**Test files to update:**
- `e2e/navigation.spec.ts`
- `e2e/applications-desktop.spec.ts`
- `e2e/applications-mobile.spec.ts`
- `e2e/canvas.spec.ts`
- `e2e/node-editor.spec.ts`
- `e2e/records.spec.ts`

**New test coverage:**
- Home screen hero and feature cards
- Carbon design system compliance (colors, fonts, spacing)
- Mobile bottom tab bar navigation
- Responsive breakpoint behavior

### 4.2 Visual Regression Testing

**Approach:**
- Capture screenshots before rebuild (baseline)
- Capture screenshots after rebuild (comparison)
- Manual review for pixel-perfect match

**Key screens to capture:**
- Home screen
- Applications list (desktop + mobile)
- Canvas editor with nodes
- Node configuration drawer
- Records list with filters
- Record detail screen

### 4.3 Accessibility Testing

**Tools:**
- Axe DevTools browser extension
- Playwright accessibility assertions
- Manual screen reader testing (VoiceOver/NVDA)

**Requirements:**
- WCAG 2.1 Level AA compliance
- All interactive elements keyboard accessible
- Proper ARIA labels on dialogs/drawers
- Color contrast ratios ≥4.5:1 (text), ≥3:1 (UI components)

## 5. Implementation Phases

### Phase 1: Setup & Design Token Integration
- Create new branch
- Add IBM Plex fonts to project
- Create `carbon-tokens.css` with design system variables
- Update global styles to use tokens

### Phase 2: Navigation & Layout
- Replace Header component with Carbon nav
- Implement mobile bottom tab bar
- Update routing structure if needed

### Phase 3: Home Screen
- Implement hero section
- Implement feature cards grid
- Add navigation to Applications

### Phase 4: Applications Screen
- Replace desktop table with Carbon table
- Replace mobile cards with Carbon cards
- Update CRUD modals (Create, Settings, Copy, Delete)
- Update History drawer
- Verify API integration

### Phase 5: Canvas Screen
- Replace canvas header/toolbar
- Update node palette sidebar
- Update node visual design
- Update connection styling
- Verify drag-drop behavior
- Update Run modal
- Update AI Explain/Generate modals

### Phase 6: Node Editor Drawer
- Replace drawer with 3-section design (Description, Rules, Action)
- Update form fields with Carbon inputs
- Verify save/load behavior

### Phase 7: Records Screen
- Replace desktop table with Carbon table
- Replace mobile cards with Carbon cards
- Update filter panel
- Update detail screen
- Verify API integration

### Phase 8: Testing & Polish
- Update all Playwright tests
- Run full E2E suite (desktop + mobile)
- Visual regression review
- Accessibility audit
- Performance testing
- Bug fixes

## 6. Risk Mitigation

**Risk 1: API integration breaks**
- Mitigation: Test API calls after each screen replacement
- Fallback: Keep API client layer unchanged

**Risk 2: Existing E2E tests fail**
- Mitigation: Update tests incrementally with each phase
- Fallback: Maintain test coverage ≥90% throughout

**Risk 3: Mobile responsive behavior breaks**
- Mitigation: Test on real devices (iPhone, Android)
- Fallback: Use browser DevTools device emulation

**Risk 4: Performance regression**
- Mitigation: Monitor bundle size, run Lighthouse audits
- Fallback: Code-split heavy components

**Risk 5: Accessibility regression**
- Mitigation: Run Axe DevTools after each phase
- Fallback: Manual screen reader testing

## 7. Success Criteria

- [ ] All screens match design handoff pixel-perfect
- [ ] All existing functionality preserved (zero feature loss)
- [ ] All Playwright E2E tests pass (desktop + mobile)
- [ ] WCAG 2.1 Level AA compliance maintained
- [ ] API integration unchanged and working
- [ ] Mobile responsive on 390×844 viewport
- [ ] Performance metrics maintained (Lighthouse score ≥90)
- [ ] Zero console errors/warnings in production build

## 8. Out of Scope

- Backend API changes
- Database schema changes
- New features beyond design handoff
- Authentication/authorization changes
- Deployment configuration changes

## 9. Deployment

**Target environment:** https://workflow-ui-gamma.vercel.app (UAT)

**Deployment process:**
1. Merge feature branch to `main`
2. Vercel auto-deploys to UAT
3. Run E2E tests against UAT environment
4. Manual UAT review
5. Production deployment (if applicable)

## 10. Rollback Plan

**If critical issues found:**
1. Revert merge commit on `main`
2. Vercel auto-deploys previous version
3. Investigate and fix issues on feature branch
4. Re-deploy when ready

**Rollback triggers:**
- E2E test failures in UAT
- Critical accessibility violations
- API integration failures
- Performance degradation >20%
