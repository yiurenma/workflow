# Product Requirements — Workflow Studio Unified Program Part 4: Canvas Enhancements (v32.0)

**Document version:** 32.0 Part 4  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Canvas node palette drag-and-drop + descriptions + node copy/delete  
**Master label:** `TODO-workflow-studio-unified-product-ux-program`  
**Part labels:** `TODO-canvas-node-palette-descriptions-drag-drop`, `TODO-canvas-node-copy-full-config-delete`

---

## 1. Overview

This document covers canvas enhancements to improve node discovery, creation, and manipulation workflows.

**Goals:**
1. Enable drag-and-drop from left palette to canvas
2. Add educational descriptions to each node type
3. Support full-config node copy (JSON export + duplicate on canvas)
4. Support node delete with edge cleanup

---

## 2. User Stories

### US-CANVAS-01 — Drag-and-drop node creation from palette

**As a** workflow author,  
**I want** to drag node types from the left palette onto the canvas,  
**So that** I can create nodes using the same interaction pattern I expect from other visual editors.

**Acceptance Criteria:**

**AC-CANVAS-01-01:** Every node type in the **left palette** can be **dragged** onto the canvas.

**AC-CANVAS-01-02:** **Drag interaction:**
- Mouse down on palette item → drag cursor appears
- Drag over canvas → drop zone indicator visible
- Release mouse → node created at drop position

**AC-CANVAS-01-03:** **Parity with mobile "Add node" sheet:** Copy for palette items matches mobile list descriptions.

**AC-CANVAS-01-04:** **Fallback:** Existing click-to-add behavior still works (click palette item → node added at default position).

---

### US-CANVAS-02 — Educational descriptions for node types

**As a** workflow author (especially new users),  
**I want** each node type to show a one-line description and optional tooltip,  
**So that** I understand what each node does before adding it to my workflow.

**Acceptance Criteria:**

**AC-CANVAS-02-01:** Each node type in the **left palette** shows:
- **Title:** Node type name (e.g., "HTTP fetch", "Safe HTTP fetch", "Logic")
- **Subtitle:** One-line description (e.g., "Performs an HTTP request and brings response data into the workflow")

**AC-CANVAS-02-02:** **Optional tooltip / "?" icon:** Second sentence with more detail (e.g., "Supports GET, POST, PUT, DELETE methods with headers and body").

**AC-CANVAS-02-03:** **Example copy direction** (PM + eng to validate vs runtime):
- **HTTP fetch:** "Performs an HTTP request and brings response data into the workflow"
- **Safe HTTP fetch:** "Same as HTTP fetch, but HTTP failure does not fail the entire workflow"
- **Logic:** "Evaluates conditional rules and routes execution based on results"

**AC-CANVAS-02-04:** **Mobile lists show same text:** Descriptions in mobile "Add node" sheet match desktop palette.

**AC-CANVAS-02-05:** All registered palette / plugin types have descriptions (no blank subtitles).

---

### US-CANVAS-03 — Copy node with full configuration

**As a** workflow author,  
**I want** to copy a node's full configuration (description, rules, action) to clipboard or duplicate it on canvas,  
**So that** I can reuse complex configurations or share them with teammates.

**Acceptance Criteria:**

**AC-CANVAS-03-01:** **Copy modes:**
- **(A) Copy JSON to clipboard:** Full node payload (description, ruleList, action, plugin config) as JSON for external use
- **(B) Duplicate on canvas:** New node with same config, new node ID, offset position

**AC-CANVAS-03-02:** **Full payload includes:**
- `description` field
- `ruleList` array (all rules with keys, types, values)
- `action` / plugin config (HTTP URL, headers, body, etc.)
- Any other persisted fields for that node type

**AC-CANVAS-03-03:** **Duplicate behavior:**
- New node ID generated (not same as source)
- Position offset (e.g., +50px right, +50px down) so not overlapping source
- **Edge policy explicit:** No edges by default (or PM-specified policy)

**AC-CANVAS-03-04:** **Placement:** Primary actions in:
- **Drawer header** (when node editor open)
- **Node context menu** (right-click / long-press on node)
- Optional keyboard shortcuts (e.g., Cmd+D for duplicate)

---

### US-CANVAS-04 — Delete node with edge cleanup

**As a** workflow author,  
**I want** to delete a node and have all connected edges removed automatically,  
**So that** I don't have dangling edges pointing to non-existent nodes.

**Acceptance Criteria:**

**AC-CANVAS-04-01:** **Delete action** removes:
- Node from graph
- All incident edges (incoming + outgoing)

**AC-CANVAS-04-02:** **Placement:** Delete action in:
- **Drawer header** (when node editor open)
- **Node context menu** (right-click / long-press on node)
- Optional keyboard shortcut (e.g., Delete key)

**AC-CANVAS-04-03:** **Delete confirm:** Show confirmation dialog when:
- Node has non-trivial config (rules or action configured)
- Node has >1 connected edge
- PM sets threshold for when confirm is required

**AC-CANVAS-04-04:** **No confirm needed:** Simple nodes (no config, no edges) can be deleted without confirmation.

**AC-CANVAS-04-05:** Documented + tested on desktop and mobile overflow/drawer paths.

---

## 3. Out of Scope

- Backend API changes (all operations client-side on canvas state)
- New node types or plugins
- Undo/redo system (future enhancement)

---

## 4. Success Metrics

- Drag-and-drop works for all palette node types
- All node types have descriptions (no blank subtitles)
- Copy JSON + Duplicate on canvas both work
- Delete removes node + edges; confirm shown when appropriate
- Mobile "Add node" sheet shows same descriptions as desktop palette

---

## 5. References

- ReactFlow drag-and-drop: https://reactflow.dev/examples/interaction/drag-and-drop
- `arch-doc-v32.0-part4-canvas.md` — Technical design
