# Product Requirements Document — Frontend Rebuild from Design Handoff

**Document version:** 37.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-frontend-rebuild-from-design-handoff`

## 1. Overview

Rebuild the entire `workflow-ui` frontend project based on a complete design handoff bundle that implements IBM Carbon Design System. The design provides pixel-perfect specifications for all screens, components, and interactions.

## 2. User Stories

### US-REBUILD-01: As a developer, I need a new branch with the rebuilt UI
**Acceptance Criteria:**
- AC-REBUILD-01.1: New branch created in https://github.com/yiurenma/workflow-ui
- AC-REBUILD-01.2: Branch name follows convention (e.g., `feature/design-handoff-rebuild`)
- AC-REBUILD-01.3: All existing functionality preserved (no feature loss)

### US-REBUILD-02: As a user, I see the new IBM Carbon design system
**Acceptance Criteria:**
- AC-REBUILD-02.1: IBM Plex Sans font for body text
- AC-REBUILD-02.2: IBM Plex Mono font for code/monospace
- AC-REBUILD-02.3: Primary blue #0f62fe used consistently
- AC-REBUILD-02.4: Navigation background #161616 (Gray 100)
- AC-REBUILD-02.5: Zero border-radius on all components
- AC-REBUILD-02.6: Design tokens match handoff file exactly

### US-REBUILD-03: As a user, I can navigate all screens
**Acceptance Criteria:**
- AC-REBUILD-03.1: Home screen with hero and feature cards
- AC-REBUILD-03.2: Applications list with search, pagination, CRUD operations
- AC-REBUILD-03.3: Canvas editor with node palette, drag-drop, connections
- AC-REBUILD-03.4: Records list with filtering and detail view
- AC-REBUILD-03.5: Mobile responsive (390×844) with bottom tab bar

### US-REBUILD-04: As a user, I can manage applications
**Acceptance Criteria:**
- AC-REBUILD-04.1: Create new application via modal
- AC-REBUILD-04.2: Edit application settings (name, description, flags)
- AC-REBUILD-04.3: Copy application to new name
- AC-REBUILD-04.4: Delete application with confirmation
- AC-REBUILD-04.5: View revision history in drawer

### US-REBUILD-05: As a user, I can edit workflows on canvas
**Acceptance Criteria:**
- AC-REBUILD-05.1: Drag nodes from sidebar to canvas
- AC-REBUILD-05.2: Click node to open configuration drawer
- AC-REBUILD-05.3: Edit node description, rules, action fields
- AC-REBUILD-05.4: Save workflow to backend
- AC-REBUILD-05.5: Run workflow with test payload

### US-REBUILD-06: As a user, I can use AI features
**Acceptance Criteria:**
- AC-REBUILD-06.1: "Explain" button generates workflow documentation
- AC-REBUILD-06.2: "Generate" button creates workflow from natural language
- AC-REBUILD-06.3: AI responses displayed in modals
- AC-REBUILD-06.4: Token stored in localStorage (no backend changes)

### US-REBUILD-07: As a user, I can view execution records
**Acceptance Criteria:**
- AC-REBUILD-07.1: Filter by application, status, confirmation number, tracking number, customer ID, date range
- AC-REBUILD-07.2: Pagination with 5 records per page
- AC-REBUILD-07.3: Click record to view detail screen
- AC-REBUILD-07.4: Detail screen shows all fields and child records
- AC-REBUILD-07.5: Mobile card layout with tap navigation

### US-REBUILD-08: As a developer, all E2E tests pass
**Acceptance Criteria:**
- AC-REBUILD-08.1: Existing Playwright tests updated for new selectors
- AC-REBUILD-08.2: New tests written for any new UI patterns
- AC-REBUILD-08.3: Desktop Chrome (1280px) tests pass
- AC-REBUILD-08.4: Mobile Chrome (390×844) tests pass
- AC-REBUILD-08.5: Zero functional regressions detected

## 3. Design Source

**Location:** `/tmp/Workflow-handoff/workflow/project/`

**Key files:**
- `Workflow Studio.html` — Main design file with all screens and components
- `components.jsx` — Shared design tokens, mock data, React components
- `README.md` — Handoff instructions

**Design system:**
- IBM Carbon Design System
- Fonts: IBM Plex Sans (300, 400, 500, 600), IBM Plex Mono (400, 500)
- Primary color: #0f62fe (Blue 60)
- Navigation: #161616 (Gray 100)
- Border radius: 0px (sharp corners)
- Responsive: Desktop 1280px, Mobile 390×844

## 4. Backend Integration

**No backend changes required.** The rebuild must integrate with existing APIs:

- **operation-api** (port 8080): Workflow CRUD, entity settings, history
- **online-api**: Workflow execution, records

**API endpoints remain unchanged:**
- `GET /api/workflow-entity-settings` — List applications
- `POST /api/workflow-entity-settings` — Create application
- `PATCH /api/workflow-entity-settings/{id}` — Update application
- `DELETE /api/workflow-entity-settings/{id}` — Delete application
- `GET /api/workflow-entity-settings/{id}/history` — Revision history
- `POST /api/workflow` — Execute workflow
- `GET /api/workflow-records` — List execution records
- `GET /api/workflow-records/{id}` — Record detail

## 5. Playwright E2E Requirement

**Critical:** All user flows must be covered by Playwright tests before marking complete.

**Test coverage required:**
- Navigation between screens
- Application CRUD operations
- Canvas node drag-drop and configuration
- Workflow save and run
- AI Explain and Generate modals
- Records filtering and detail view
- Mobile responsive behavior
- Accessibility (WCAG 2.1 Level AA)

**Test environments:**
- Desktop Chrome 1280px
- Mobile Chrome 390×844 (isMobile: true, hasTouch: true)

**Pass criteria:**
- All existing tests pass (updated for new selectors)
- New tests written for new UI patterns
- Zero functional regressions

## 6. Out of Scope

- Backend API changes
- New features beyond design handoff
- Database schema changes
- Authentication/authorization changes

## 7. Success Metrics

- Design handoff implemented pixel-perfect
- All Playwright E2E tests pass
- Zero functional regressions
- Mobile responsive on 390×844 viewport
- WCAG 2.1 Level AA compliance maintained
