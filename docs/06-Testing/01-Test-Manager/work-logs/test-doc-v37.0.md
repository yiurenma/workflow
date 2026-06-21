# Test Document — Frontend Rebuild from Design Handoff

**Document version:** 37.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-frontend-rebuild-from-design-handoff`

## 1. Test Strategy

This document defines the test cases for verifying the frontend rebuild based on the design handoff bundle. All tests must pass before marking the TODO as complete.

## 2. Test Environments

**Desktop:**
- Browser: Chrome 1280×720
- Viewport: 1280px width

**Mobile:**
- Browser: Chrome (device emulation)
- Viewport: 390×844 (iPhone 12 Pro)
- Settings: `isMobile: true`, `hasTouch: true`

**UAT Environment:** https://workflow-ui-gamma.vercel.app

## 3. Test Cases

### TC-REBUILD-01: Branch Creation
**Precondition:** Access to https://github.com/yiurenma/workflow-ui  
**Steps:**
1. Verify new branch exists (e.g., `feature/design-handoff-rebuild`)
2. Verify branch is based on current `main`
3. Verify branch has commits for rebuild work

**Expected:** Branch created successfully with rebuild commits

---

### TC-REBUILD-02: Design System - Fonts
**Precondition:** Navigate to any screen  
**Steps:**
1. Open browser DevTools
2. Inspect body text element
3. Verify computed font-family contains "IBM Plex Sans"
4. Inspect code/monospace element (e.g., confirmation number)
5. Verify computed font-family contains "IBM Plex Mono"

**Expected:** IBM Plex Sans for body, IBM Plex Mono for code

---

### TC-REBUILD-03: Design System - Colors
**Precondition:** Navigate to Applications screen  
**Steps:**
1. Inspect primary button
2. Verify background-color is `#0f62fe` or `rgb(15, 98, 254)`
3. Inspect navigation bar
4. Verify background-color is `#161616` or `rgb(22, 22, 22)`
5. Inspect body text
6. Verify color is `#161616` or `rgb(22, 22, 22)`

**Expected:** Colors match design tokens exactly

---

### TC-REBUILD-04: Design System - Border Radius
**Precondition:** Navigate to Applications screen  
**Steps:**
1. Inspect primary button
2. Verify border-radius is `0px`
3. Inspect application card (mobile)
4. Verify border-radius is `0px`
5. Inspect modal dialog
6. Verify border-radius is `0px`

**Expected:** All components have 0px border-radius (sharp corners)

---

### TC-REBUILD-05: Home Screen - Hero Section
**Precondition:** Navigate to Home screen  
**Steps:**
1. Verify hero section exists with dark background
2. Verify heading contains "Design, test & deploy"
3. Verify subheading mentions "message enrichment"
4. Verify "Go to Applications" button exists
5. Click "Go to Applications" button
6. Verify navigation to Applications screen

**Expected:** Hero section matches design, navigation works

---

### TC-REBUILD-06: Home Screen - Feature Cards
**Precondition:** Navigate to Home screen  
**Steps:**
1. Scroll to feature cards section
2. Verify 6 feature cards displayed in grid
3. Verify cards show: Canvas, Conditions, HTTP Enrichment, AI Explain, AI Generate, Execution Records
4. Verify each card has icon, title, and description
5. Hover over card (desktop)
6. Verify hover state (background change)

**Expected:** 6 feature cards with correct content and hover states

---

### TC-REBUILD-07: Applications List - Desktop Table
**Precondition:** Navigate to Applications screen (desktop)  
**Steps:**
1. Verify table displays with columns: Application, Description, Status, Region, Last updated, Actions
2. Verify at least one application row exists
3. Verify Status column shows Active/Inactive tag
4. Hover over row
5. Verify row background changes
6. Verify Actions links appear (Open, Settings, History, Copy, Delete)

**Expected:** Table layout matches design, hover states work

---

### TC-REBUILD-08: Applications List - Mobile Cards
**Precondition:** Navigate to Applications screen (mobile 390×844)  
**Steps:**
1. Verify applications displayed as cards (not table)
2. Verify each card shows: name, status tag, description, timestamp
3. Tap card
4. Verify navigation to Canvas screen

**Expected:** Mobile card layout matches design, tap navigation works

---

### TC-REBUILD-09: Applications - Create Modal
**Precondition:** Navigate to Applications screen  
**Steps:**
1. Click "+ New application" button (desktop) or FAB (mobile)
2. Verify modal opens with title "New Application"
3. Verify "Application Name" input field exists
4. Enter "test-app-rebuild"
5. Click "Create" button
6. Verify modal closes
7. Verify new application appears in list

**Expected:** Create modal matches design, creates application successfully

---

### TC-REBUILD-10: Applications - Settings Modal
**Precondition:** Navigate to Applications screen with at least one application  
**Steps:**
1. Click "Settings" action on first application
2. Verify modal opens with title "Settings — {applicationName}"
3. Verify fields: Application Name, Description, Configuration flags (5 toggles), EIM ID, Service Account, Region, Retry Properties
4. Toggle "Enabled" switch
5. Click "Save changes"
6. Verify modal closes
7. Verify application status updated

**Expected:** Settings modal matches design, saves changes successfully

---

### TC-REBUILD-11: Applications - History Drawer
**Precondition:** Navigate to Applications screen with at least one application  
**Steps:**
1. Click "History" action on first application
2. Verify drawer opens from right side (desktop) or bottom (mobile)
3. Verify drawer title "Revision History"
4. Verify table shows: #, Timestamp, Change, Actions columns
5. Verify at least one revision row exists
6. Click outside drawer or close button
7. Verify drawer closes

**Expected:** History drawer matches design, displays revision history

---

### TC-REBUILD-12: Canvas - Layout
**Precondition:** Navigate to Canvas screen (click application)  
**Steps:**
1. Verify canvas header with breadcrumb navigation
2. Verify node palette sidebar on left (desktop)
3. Verify canvas area with dotted grid background
4. Verify toolbar buttons: Straighten, Explain, Generate, Run, Save
5. Verify zoom controls in bottom-left corner
6. Verify minimap in bottom-right corner (desktop)

**Expected:** Canvas layout matches design exactly

---

### TC-REBUILD-13: Canvas - Node Palette
**Precondition:** Navigate to Canvas screen (desktop)  
**Steps:**
1. Verify sidebar shows 6 node types
2. Verify each node shows: icon, name, description
3. Verify node types: HTTP Fetch, Safe Fetch, Dispatch, Condition, Transform, Transform+
4. Hover over node
5. Verify hover state (background change)
6. Click collapse button
7. Verify sidebar collapses to icon-only view

**Expected:** Node palette matches design, collapse/expand works

---

### TC-REBUILD-14: Canvas - Node Visual Design
**Precondition:** Navigate to Canvas screen with existing nodes  
**Steps:**
1. Verify node has white background
2. Verify node has colored left border (matches plugin color)
3. Verify node shows: icon, label, plugin name
4. Verify node has connection ports (top and bottom)
5. Hover over node
6. Verify hover state (shadow, slight lift)
7. Click node
8. Verify selected state (blue outline)

**Expected:** Node visual design matches design handoff

---

### TC-REBUILD-15: Canvas - Node Configuration Drawer
**Precondition:** Navigate to Canvas screen with at least one node  
**Steps:**
1. Click node
2. Verify drawer opens from right (desktop) or bottom (mobile)
3. Verify drawer header shows: "Node Configuration", node type, node label
4. Verify three sections: Node Description, Rules, Action
5. Verify each section has colored left border
6. Verify "Edit" button in header
7. Click "Edit"
8. Verify form fields become editable
9. Click "Done"
10. Verify drawer switches back to read-only view

**Expected:** Node drawer matches 3-section design, edit mode works

---

### TC-REBUILD-16: Canvas - AI Explain Modal
**Precondition:** Navigate to Canvas screen with nodes  
**Steps:**
1. Click "Explain" button in toolbar
2. Verify modal opens with title "AI Workflow Explainer — {appName}"
3. Verify modal shows workflow summary
4. Verify modal shows numbered list of nodes with descriptions
5. Verify "Close" button exists
6. Click "Close"
7. Verify modal closes

**Expected:** Explain modal matches design, displays workflow explanation

---

### TC-REBUILD-17: Canvas - AI Generate Modal
**Precondition:** Navigate to Canvas screen  
**Steps:**
1. Click "Generate" button in toolbar
2. Verify modal opens with title "Generate Workflow"
3. Verify textarea for natural language input
4. Enter "Fetch user data and send notification"
5. Click "Generate" button
6. Verify loading state
7. Verify canvas updates with generated nodes (or error message)

**Expected:** Generate modal matches design, generates workflow

---

### TC-REBUILD-18: Canvas - Run Modal
**Precondition:** Navigate to Canvas screen with nodes  
**Steps:**
1. Click "Run" button in toolbar
2. Verify modal opens with title "Run — {appName}"
3. Verify fields: Confirmation Number, Request Body
4. Enter test data in Request Body
5. Click "Send POST /api/workflow" button
6. Verify loading state
7. Verify response displayed in modal

**Expected:** Run modal matches design, executes workflow

---

### TC-REBUILD-19: Records List - Desktop Table
**Precondition:** Navigate to Records screen (desktop)  
**Steps:**
1. Verify table displays with columns: ID, Application, Overall Status, Confirmation No., Tracking No., Customer ID, Created, Retries, Actions
2. Verify at least one record row exists
3. Verify Overall Status column shows colored badge
4. Verify pagination controls at bottom
5. Click "View" action on first record
6. Verify navigation to Record Detail screen

**Expected:** Records table matches design, navigation works

---

### TC-REBUILD-20: Records List - Mobile Cards
**Precondition:** Navigate to Records screen (mobile 390×844)  
**Steps:**
1. Verify records displayed as cards (not table)
2. Verify each card shows: ID, status badge, application name, confirmation number, timestamp
3. Tap card
4. Verify navigation to Record Detail screen

**Expected:** Mobile card layout matches design, tap navigation works

---

### TC-REBUILD-21: Records - Filter Panel
**Precondition:** Navigate to Records screen  
**Steps:**
1. Verify filter panel above table/cards
2. Verify filter fields: Application name, Status dropdown, Confirmation No., Tracking No., Customer ID, From date, To date
3. Enter "payment" in Application name
4. Click "Search" button
5. Verify table/cards filtered to matching records
6. Click "Reset" button
7. Verify filters cleared and all records shown

**Expected:** Filter panel matches design, filtering works

---

### TC-REBUILD-22: Record Detail Screen
**Precondition:** Navigate to Records screen, click a record  
**Steps:**
1. Verify breadcrumb navigation "← Records | Record #{id}"
2. Verify "Record Details" card with all fields
3. Verify fields displayed in 2-column grid (desktop) or 1-column (mobile)
4. Verify fields: ID, Application, Overall Status, Retry Times, Confirmation No., Tracking No., Customer ID, Origin Record ID, Created, Last Modified
5. If child records exist, verify "Child Records" table below
6. Click "← Records" breadcrumb
7. Verify navigation back to Records list

**Expected:** Detail screen matches design, shows all record data

---

### TC-REBUILD-23: Mobile - Bottom Tab Bar
**Precondition:** Navigate to any screen (mobile 390×844)  
**Steps:**
1. Verify bottom tab bar visible with 3 tabs
2. Verify tabs: Home, Applications, Records
3. Verify active tab highlighted in blue
4. Tap "Home" tab
5. Verify navigation to Home screen
6. Tap "Applications" tab
7. Verify navigation to Applications screen
8. Tap "Records" tab
9. Verify navigation to Records screen

**Expected:** Bottom tab bar matches design, navigation works

---

### TC-REBUILD-24: Responsive - Breakpoint Behavior
**Precondition:** Navigate to Applications screen (desktop)  
**Steps:**
1. Verify desktop layout (table, horizontal nav)
2. Resize browser to 768px width
3. Verify layout switches to mobile (cards, bottom tab bar)
4. Resize browser to 1280px width
5. Verify layout switches back to desktop

**Expected:** Responsive breakpoint at 768px works correctly

---

### TC-REBUILD-25: API Integration - Applications CRUD
**Precondition:** Backend APIs running  
**Steps:**
1. Navigate to Applications screen
2. Verify applications loaded from `GET /api/workflow-entity-settings`
3. Create new application
4. Verify `POST /api/workflow-entity-settings` called
5. Edit application settings
6. Verify `PATCH /api/workflow-entity-settings/{id}` called
7. Delete application
8. Verify `DELETE /api/workflow-entity-settings/{id}` called

**Expected:** All API calls work, no integration breaks

---

### TC-REBUILD-26: API Integration - Workflow Save/Load
**Precondition:** Navigate to Canvas screen  
**Steps:**
1. Verify workflow loaded from `GET /api/workflow/{applicationName}`
2. Add/modify nodes
3. Click "Save" button
4. Verify `POST /api/workflow/{applicationName}` called with workflow JSON
5. Refresh page
6. Verify workflow loaded with saved changes

**Expected:** Workflow save/load API integration works

---

### TC-REBUILD-27: API Integration - Workflow Execution
**Precondition:** Navigate to Canvas screen with nodes  
**Steps:**
1. Click "Run" button
2. Enter test payload
3. Click "Send POST /api/workflow"
4. Verify `POST /api/workflow?applicationName={name}&confirmationNumber={id}` called
5. Verify response displayed in modal

**Expected:** Workflow execution API integration works

---

### TC-REBUILD-28: API Integration - Records List
**Precondition:** Navigate to Records screen  
**Steps:**
1. Verify records loaded from `GET /api/workflow-records`
2. Apply filters
3. Verify `GET /api/workflow-records?{filters}` called with query params
4. Click record
5. Verify `GET /api/workflow-records/{id}` called
6. Verify detail screen shows record data

**Expected:** Records API integration works

---

### TC-REBUILD-29: Accessibility - Keyboard Navigation
**Precondition:** Navigate to Applications screen  
**Steps:**
1. Press Tab key repeatedly
2. Verify focus moves through interactive elements in logical order
3. Verify focus indicator visible on all elements
4. Press Enter on "+ New application" button
5. Verify modal opens
6. Press Tab to navigate modal fields
7. Press Escape
8. Verify modal closes

**Expected:** Full keyboard navigation works, focus visible

---

### TC-REBUILD-30: Accessibility - Screen Reader
**Precondition:** Enable screen reader (VoiceOver/NVDA)  
**Steps:**
1. Navigate to Canvas screen
2. Click node to open drawer
3. Verify screen reader announces "Node Configuration"
4. Navigate through drawer sections
5. Verify screen reader announces section headings
6. Navigate to form fields
7. Verify screen reader announces field labels

**Expected:** Screen reader announces all content correctly

---

### TC-REBUILD-31: Accessibility - Color Contrast
**Precondition:** Navigate to any screen  
**Steps:**
1. Open Axe DevTools browser extension
2. Run accessibility scan
3. Verify zero color contrast violations
4. Manually check key text elements:
   - Body text on white: ≥4.5:1
   - Secondary text on white: ≥4.5:1
   - Button text on #0f62fe: ≥4.5:1
   - Nav text on #161616: ≥4.5:1

**Expected:** All text meets WCAG 2.1 AA contrast requirements (4.5:1)

---

### TC-REBUILD-32: Accessibility - ARIA Labels
**Precondition:** Navigate to Canvas screen  
**Steps:**
1. Click node to open drawer
2. Inspect drawer element in DevTools
3. Verify `role="dialog"` attribute exists
4. Verify `aria-label="Node Configuration"` attribute exists
5. Navigate to modal dialogs
6. Verify all modals have `role="dialog"` and `aria-label`

**Expected:** All dialogs have proper ARIA attributes

---

### TC-REBUILD-33: Performance - Page Load
**Precondition:** Clear browser cache  
**Steps:**
1. Open browser DevTools Network tab
2. Navigate to Applications screen
3. Wait for page to fully load
4. Verify DOMContentLoaded < 2 seconds
5. Verify Load event < 3 seconds
6. Run Lighthouse audit
7. Verify Performance score ≥90

**Expected:** Fast page load, Lighthouse score ≥90

---

### TC-REBUILD-34: Performance - Bundle Size
**Precondition:** Production build  
**Steps:**
1. Run `npm run build`
2. Check `dist/` directory
3. Verify main JS bundle < 500KB (gzipped)
4. Verify main CSS bundle < 100KB (gzipped)
5. Verify total bundle size < 1MB (gzipped)

**Expected:** Bundle sizes within acceptable limits

---

### TC-REBUILD-35: Regression - Existing E2E Tests
**Precondition:** All Playwright tests updated for new selectors  
**Steps:**
1. Run `npm run test:e2e`
2. Verify all tests pass (desktop + mobile)
3. Check test results for:
   - Navigation tests: PASS
   - Applications tests: PASS
   - Canvas tests: PASS
   - Node editor tests: PASS
   - Records tests: PASS
4. Verify zero test failures

**Expected:** All existing E2E tests pass with zero failures

---

## 4. Test Execution Summary

**Total Test Cases:** 35

**Breakdown:**
- Design System: 4 tests (TC-REBUILD-02 to TC-REBUILD-04)
- Home Screen: 2 tests (TC-REBUILD-05 to TC-REBUILD-06)
- Applications: 5 tests (TC-REBUILD-07 to TC-REBUILD-11)
- Canvas: 7 tests (TC-REBUILD-12 to TC-REBUILD-18)
- Records: 4 tests (TC-REBUILD-19 to TC-REBUILD-22)
- Mobile: 1 test (TC-REBUILD-23)
- Responsive: 1 test (TC-REBUILD-24)
- API Integration: 4 tests (TC-REBUILD-25 to TC-REBUILD-28)
- Accessibility: 4 tests (TC-REBUILD-29 to TC-REBUILD-32)
- Performance: 2 tests (TC-REBUILD-33 to TC-REBUILD-34)
- Regression: 1 test (TC-REBUILD-35)

**Pass Criteria:**
- All 35 test cases must pass
- Zero functional regressions
- WCAG 2.1 Level AA compliance maintained
- Lighthouse Performance score ≥90

## 5. Test Execution Log

| Test Case | Status | Tester | Date | Notes |
|-----------|--------|--------|------|-------|
| TC-REBUILD-01 | Pending | - | - | - |
| TC-REBUILD-02 | Pending | - | - | - |
| ... | ... | ... | ... | ... |

## 6. Defects Found

| ID | Test Case | Severity | Description | Status |
|----|-----------|----------|-------------|--------|
| - | - | - | - | - |

## 7. Sign-off

**QA Engineer:** _______________  
**Date:** _______________  

**Test Manager:** _______________  
**Date:** _______________
