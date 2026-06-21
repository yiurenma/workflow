# Test Cases — Workflow Studio Unified Program Part 4: Canvas Enhancements (v32.0)

**Document version:** 32.0 Part 4  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Canvas node palette drag-and-drop + descriptions + node copy/delete  
**Repository:** `workflow-ui`

---

## 1. Test Strategy

### 1.1 Test Levels

- **Unit tests:** Not required (UI interaction heavy)
- **Integration tests:** Not required (no backend changes)
- **E2E tests (Playwright):** Optional for drag-and-drop; manual testing primary
- **Manual testing:** Primary verification method

### 1.2 Test Environments

- **Desktop:** Chrome 1280px
- **Mobile:** Chrome 390×844 (touch events)
- **UAT:** https://workflow-ui-gamma.vercel.app

---

## 2. Test Cases

### TC-CANVAS-DRAG-01 — Drag node from palette to canvas (Desktop)

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px
- Left palette visible with node types

**Steps:**
1. Mouse down on "HTTP fetch" palette item
2. Drag cursor over canvas area
3. Release mouse button at position (x: 400, y: 300)

**Expected:**
- Drag cursor appears during drag
- Drop zone indicator visible on canvas
- New "HTTP fetch" node created at drop position (±20px tolerance)
- Node has default empty configuration

**Layer validation:**
- Layer 1 (Exist): Node present in DOM
- Layer 4 (Interact): Node can be selected and moved
- Layer 5 (Effect): Node position matches drop coordinates

---

### TC-CANVAS-DRAG-02 — Drag multiple node types

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px

**Steps:**
1. Drag "HTTP fetch" to canvas → release
2. Drag "Safe HTTP fetch" to canvas → release
3. Drag "Logic" to canvas → release

**Expected:**
- All three nodes created successfully
- Each node has correct type
- Nodes do not overlap (if dropped at different positions)

---

### TC-CANVAS-DRAG-03 — Click-to-add still works (fallback)

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px

**Steps:**
1. Click "HTTP fetch" palette item (no drag)

**Expected:**
- Node created at default position (e.g., center of viewport)
- Same behavior as before drag-and-drop feature

---

### TC-CANVAS-DRAG-04 — Drag on mobile (touch)

**Preconditions:**
- Open workflow canvas on Mobile Chrome 390×844

**Steps:**
1. Long-press "HTTP fetch" palette item
2. Drag finger over canvas
3. Release finger

**Expected:**
- Touch drag works OR documented deferral (mobile uses "Add node" sheet instead)
- If deferred: palette items not draggable on mobile; "Add node" FAB opens sheet

---

### TC-CANVAS-DESC-01 — Node descriptions visible in palette

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px

**Steps:**
1. Inspect left palette

**Expected:**
- Each node type shows:
  - **Title** (e.g., "HTTP fetch")
  - **Subtitle/description** (e.g., "Performs an HTTP request and brings response data into the workflow")
- Optional: "?" icon or tooltip for extended description

---

### TC-CANVAS-DESC-02 — Tooltip shows extended description

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px

**Steps:**
1. Hover over "?" icon next to "Safe HTTP fetch"

**Expected:**
- Tooltip appears with extended description:
  - "Same as HTTP fetch, but HTTP failure does not fail the entire workflow"

---

### TC-CANVAS-DESC-03 — Mobile "Add node" sheet shows same descriptions

**Preconditions:**
- Open workflow canvas on Mobile Chrome 390×844

**Steps:**
1. Tap "Add node" FAB (+)
2. Inspect node list in bottom sheet

**Expected:**
- Each node type shows same title + description as desktop palette
- Descriptions match exactly (no mobile-specific copy)

---

### TC-CANVAS-COPY-01 — Copy node JSON to clipboard

**Preconditions:**
- Open workflow canvas
- Select a node with configuration (description, rules, action)

**Steps:**
1. Open node editor drawer
2. Click "Copy JSON" button in drawer header

**Expected:**
- Success toast: "Node configuration copied to clipboard"
- Clipboard contains JSON with:
  - `id`, `type`, `description`, `ruleList`, `action`
  - All persisted fields included
- JSON is valid and pretty-printed

**Verification:**
- Paste clipboard into text editor
- Verify JSON structure matches node data

---

### TC-CANVAS-COPY-02 — Duplicate node on canvas

**Preconditions:**
- Open workflow canvas
- Select a node with configuration

**Steps:**
1. Open node editor drawer
2. Click "Duplicate" button in drawer header

**Expected:**
- Success toast: "Node duplicated"
- New node created with:
  - New unique ID (different from source)
  - Offset position (+50px x, +50px y from source)
  - Same configuration (description, rules, action)
  - No edges (edges not copied)

**Layer validation:**
- Layer 1 (Exist): New node present in DOM
- Layer 2 (Size): New node same size as source
- Layer 5 (Effect): New node data matches source (except ID)

---

### TC-CANVAS-COPY-03 — Copy via context menu (right-click)

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px
- Canvas has at least one node

**Steps:**
1. Right-click on a node
2. Select "Copy JSON" from context menu

**Expected:**
- Context menu appears at cursor position
- "Copy JSON" option visible
- Clicking option copies JSON to clipboard (same as TC-CANVAS-COPY-01)
- Context menu closes after selection

---

### TC-CANVAS-DELETE-01 — Delete node with no config/edges (no confirmation)

**Preconditions:**
- Open workflow canvas
- Create a new node with no rules, no action, no edges

**Steps:**
1. Select the node
2. Open node editor drawer
3. Click "Delete" button

**Expected:**
- Node deleted immediately (no confirmation dialog)
- Node removed from canvas
- Success toast: "Node deleted"

---

### TC-CANVAS-DELETE-02 — Delete node with config (confirmation required)

**Preconditions:**
- Open workflow canvas
- Select a node with rules or action configured

**Steps:**
1. Open node editor drawer
2. Click "Delete" button

**Expected:**
- Confirmation dialog appears:
  - Title: "Delete Node"
  - Content: "This node has configured rules/actions. Are you sure you want to delete it?"
  - Buttons: "Cancel" (default), "Delete" (danger)
3. Click "Delete"
4. Node removed from canvas
5. Success toast: "Node deleted"

---

### TC-CANVAS-DELETE-03 — Delete node with edges (confirmation + edge cleanup)

**Preconditions:**
- Open workflow canvas
- Create workflow: Node A → Node B → Node C
- Select Node B (has 1 incoming edge, 1 outgoing edge)

**Steps:**
1. Open node editor drawer for Node B
2. Click "Delete" button
3. Confirm deletion

**Expected:**
- Confirmation dialog mentions "2 connected edge(s)"
- After deletion:
  - Node B removed
  - Both edges removed (A→B and B→C)
  - No dangling edges visible
  - Node A and Node C remain

**Layer validation:**
- Layer 5 (Effect): Edges absent from DOM after deletion

---

### TC-CANVAS-DELETE-04 — Delete via context menu

**Preconditions:**
- Open workflow canvas on Desktop Chrome 1280px

**Steps:**
1. Right-click on a node
2. Select "Delete" from context menu

**Expected:**
- Context menu shows "Delete" option (red/danger style)
- Clicking triggers same confirmation logic as drawer button
- Context menu closes after selection

---

### TC-CANVAS-DELETE-05 — Cancel delete confirmation

**Preconditions:**
- Open workflow canvas
- Select a node with config

**Steps:**
1. Click "Delete" button
2. Confirmation dialog appears
3. Click "Cancel"

**Expected:**
- Dialog closes
- Node NOT deleted
- Canvas state unchanged

---

### TC-CANVAS-MOBILE-01 — Node actions on mobile (drawer header)

**Preconditions:**
- Open workflow canvas on Mobile Chrome 390×844
- Select a node

**Steps:**
1. Tap node to open drawer
2. Inspect drawer header

**Expected:**
- "Copy JSON", "Duplicate", "Delete" buttons visible
- Buttons have adequate touch target size (min 44×44px)
- Buttons work same as desktop

---

### TC-CANVAS-MOBILE-02 — Long-press context menu (optional)

**Preconditions:**
- Open workflow canvas on Mobile Chrome 390×844

**Steps:**
1. Long-press on a node

**Expected:**
- Context menu appears OR documented deferral (mobile uses drawer buttons only)
- If implemented: menu shows Copy/Duplicate/Delete options

---

## 3. Regression Tests

### TC-CANVAS-REG-01 — Existing canvas interactions still work

**Preconditions:**
- Open workflow canvas

**Steps:**
1. Pan canvas (drag background)
2. Zoom canvas (pinch or scroll)
3. Select node (click)
4. Move node (drag)
5. Connect nodes (drag from handle)

**Expected:**
- All existing interactions work as before
- No conflicts with new drag-and-drop feature

---

### TC-CANVAS-REG-02 — Existing E2E tests pass

**Preconditions:**
- Run full Playwright suite

**Expected:**
- All existing canvas tests pass (TC-CANVAS-01 through TC-CANVAS-05)
- No regressions in node editor tests (TC-NODE-*)
- No regressions in mobile tests (TC-CANVAS-MOB-*)

---

## 4. Acceptance Criteria Mapping

| AC | Test Cases |
|----|------------|
| AC-CANVAS-01-01 | TC-CANVAS-DRAG-01, TC-CANVAS-DRAG-02 |
| AC-CANVAS-01-02 | TC-CANVAS-DRAG-01 |
| AC-CANVAS-01-03 | TC-CANVAS-DESC-03 |
| AC-CANVAS-01-04 | TC-CANVAS-DRAG-03 |
| AC-CANVAS-02-01 | TC-CANVAS-DESC-01 |
| AC-CANVAS-02-02 | TC-CANVAS-DESC-01, TC-CANVAS-DESC-02 |
| AC-CANVAS-02-03 | TC-CANVAS-DESC-03 |
| AC-CANVAS-03-01 | TC-CANVAS-COPY-01 |
| AC-CANVAS-03-02 | TC-CANVAS-COPY-01 |
| AC-CANVAS-03-03 | TC-CANVAS-COPY-02 |
| AC-CANVAS-03-04 | TC-CANVAS-COPY-02 |
| AC-CANVAS-03-05 | TC-CANVAS-COPY-03 |
| AC-CANVAS-04-01 | TC-CANVAS-DELETE-01, TC-CANVAS-DELETE-02 |
| AC-CANVAS-04-02 | TC-CANVAS-DELETE-03 |
| AC-CANVAS-04-03 | TC-CANVAS-DELETE-04 |
| AC-CANVAS-04-04 | TC-CANVAS-DELETE-02, TC-CANVAS-DELETE-05 |

---

## 5. Test Execution Plan

### Phase 1: Drag-and-Drop (after implementation)
- TC-CANVAS-DRAG-01 through TC-CANVAS-DRAG-04
- TC-CANVAS-DESC-01 through TC-CANVAS-DESC-03
- TC-CANVAS-REG-01

### Phase 2: Copy & Delete (after implementation)
- TC-CANVAS-COPY-01 through TC-CANVAS-COPY-03
- TC-CANVAS-DELETE-01 through TC-CANVAS-DELETE-05
- TC-CANVAS-MOBILE-01, TC-CANVAS-MOBILE-02

### Phase 3: Regression (after all features)
- TC-CANVAS-REG-02
- Full E2E suite run

---

## 6. Success Criteria

- All test cases pass on Desktop Chrome 1280px
- All mobile test cases pass on Mobile Chrome 390×844
- No regressions in existing E2E suite
- UAT spot-check confirms features work on gamma environment

---

## 7. References

- `pm-doc-v32.0-part4-canvas.md` — Product requirements
- `arch-doc-v32.0-part4-canvas.md` — Technical design
- `docs/e2e-testing-guide.md` — E2E testing conventions
