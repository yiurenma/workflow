# Product Requirements — Workflow Studio Unified Program Part 3: Node Editor Enhancements (v32.0)

**Document version:** 32.0 Part 3  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Node editor rule key validation + resizable drawer + read-first mode  
**Master label:** `TODO-workflow-studio-unified-product-ux-program`  
**Part labels:** `TODO-node-editor-rule-key-json-path-validation`, `TODO-node-editor-draggable-width-readonly-default`

---

## 1. Overview

This document covers node editor enhancements to improve data entry quality and user experience when viewing/editing node configurations.

**Goals:**
1. Validate rule keys as single JSONPath expressions (prevent invalid syntax)
2. Make drawer resizable (user can widen to see more content)
3. Default to read-only view (skim content quickly before editing)
4. Handle long content gracefully (scrolling, reflow)

---

## 2. User Stories

### US-NODE-01 — Rule key must be one valid JSONPath

**As a** workflow author,  
**I want** the system to validate that each rule key field contains exactly one valid JSONPath expression,  
**So that** I don't accidentally save invalid configurations that will fail at runtime.

**Acceptance Criteria:**

**AC-NODE-01-01:** Each **rule key** field in the node editor validates on **blur** and/or **before save**.

**AC-NODE-01-02:** Validation rules:
- Must be valid JSONPath syntax (e.g., `$.data.userId`, `$.items[0].name`)
- Must be **exactly one** JSONPath expression (no comma-separated paths, no free text)
- Empty field is allowed if rules are optional

**AC-NODE-01-03:** **Blocking error** prevents save if invalid:
- Red border on invalid field
- Error message below field
- Save button disabled until fixed

**AC-NODE-01-04:** **User-facing message** explicitly says the field accepts **only a single JSONPath**:
- Example: "Rule key must be a single valid JSONPath expression (e.g., `$.data.userId`)"
- Not generic "invalid input"

**AC-NODE-01-05:** Happy path + invalid paths covered in test doc / Playwright if applicable.

---

### US-NODE-02 — Resizable node editor drawer

**As a** workflow author,  
**I want** to drag the inner vertical edge of the node editor drawer to widen it,  
**So that** I can see more content (long JSON, descriptions) without horizontal scrolling.

**Acceptance Criteria:**

**AC-NODE-02-01:** **Desktop:** User can drag the **inner vertical edge** (left edge of drawer) toward the canvas to widen the panel.

**AC-NODE-02-02:** **Min/max width:** Drawer respects minimum (e.g., 400px) and maximum (e.g., 80vw) width constraints.

**AC-NODE-02-03:** **Persistence (optional):** Drawer width persists in session or `localStorage` so user doesn't need to resize every time.

**AC-NODE-02-04:** **Mobile:** Touch drag handle OR documented deferral (mobile may use fixed bottom drawer height instead).

**AC-NODE-02-05:** **Visual affordance:** Drag handle visible (e.g., vertical line with drag cursor on hover).

---

### US-NODE-03 — Default to read-only view mode

**As a** workflow author,  
**I want** the node editor to open in read-only view by default,  
**So that** I can quickly skim full content (formatted text, pretty JSON) without input boxes cluttering the view.

**Acceptance Criteria:**

**AC-NODE-03-01:** On first open after selecting a node, drawer shows **read-only rendering**:
- Node description: formatted text (not textarea)
- Rules: pretty-printed JSON or structured list (not input fields)
- Action: pretty-printed JSON or structured display (not input fields)

**AC-NODE-03-02:** **Edit control** at top of drawer (e.g., **Edit** button) switches to existing **form mode** with input fields.

**AC-NODE-03-03:** **Done / Cancel behavior** for drafts per Arch doc:
- **Done:** Save changes, return to read-only view
- **Cancel:** Discard local state, return to read-only view with original values

**AC-NODE-03-04:** **Long content:** Inner body scrolls; content reflows when panel widens; no silent truncation.

**AC-NODE-03-05:** **Virtualization / "Show more"** only if performance requires (e.g., >1000 lines of JSON).

---

### US-NODE-04 — Coordination with drawer close fix

**As a** developer,  
**I want** node editor drawer chrome changes (resize handle, Edit button) to be implemented in one pass with drawer close fix (Part 1 §1),  
**So that** we avoid regressions from multiple overlapping changes to the same component.

**Acceptance Criteria:**

**AC-NODE-04-01:** If drawer close fix (Part 1 §1) and drawer chrome changes (resize, read-only mode) overlap, implement in one coordinated pass.

**AC-NODE-04-02:** Test all drawer interactions after changes:
- Open drawer
- Close drawer (× button, outside click, ESC)
- Resize drawer (drag handle)
- Switch to edit mode
- Save / Cancel

---

## 3. Out of Scope

- Backend API changes (validation is client-side)
- New node types or action plugins
- Canvas layout changes

---

## 4. Success Metrics

- Rule key validation prevents invalid JSONPath from being saved
- Drawer resizable on Desktop; width persists across sessions
- Read-only view shows full content without input clutter
- No regressions in existing drawer interactions (open, close, save)

---

## 5. References

- JSONPath spec: https://goessner.net/articles/JsonPath/
- `jsonpath-plus` library: https://www.npmjs.com/package/jsonpath-plus
- `arch-doc-v32.0-part1-e2e-quality.md` — Drawer close fix coordination
