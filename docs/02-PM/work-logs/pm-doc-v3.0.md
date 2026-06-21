# PM Doc — Artboard Node Editor: Three-Panel Layout + Smart Text Formatting

**Label**: `TODO-artboard-node-editor-3panel-json-format`  
**Version**: 3.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Scope**: workflow-ui only — no backend changes required  
**Input**: TODO.md item "Artboard node editor — three-panel layout + smart text formatting"

---

## 1. Background

The workflow artboard's node editor is a Drawer panel (`workflow-drawer/index.tsx`) that currently renders either `HttpCallForm` or `LogicForm` depending on the selected node type. Both forms contain the correct data fields, but their layout is not visually structured to clearly communicate the **three logical concerns** of each node: what it *is* (description), when it *runs* (rules), and what it *does* (action/behavior).

Additionally, text fields that accept JSON content (headers, body, extraction schema, logic payload) have no formatting assistance — users must manually format JSON, and unstructured text is shown as-is.

This release (US-30) reworks the node editor layout and adds smart text formatting to make the editor more readable and easier to use.

---

## 2. User Story

**US-30 — Artboard node editor: three-panel layout + smart text formatting**

> **As an** integration engineer configuring workflow nodes on the artboard,  
> **I want** the node editor panel to be clearly split into three visually distinct sections — description, rules, and action — with automatic JSON formatting in text fields,  
> **so that** I can understand the purpose, conditions, and behavior of each node at a glance, and enter JSON configuration without manual formatting.

---

## 3. Acceptance Criteria

### AC-30-1 — Three visually distinct panels

The node editor (both `HttpCallForm` and `LogicForm`) must be organized into **three unmistakably distinct sections**:

| Panel | Content | Present in |
|---|---|---|
| **1 — Node Description** | "What is this node for?" — the `description` field (step name) | Both forms |
| **2 — Rules** | "When does this node run?" — the `ruleList` (JSONPath trigger conditions) | Both forms |
| **3 — Action Type** | "What does this node do?" — HTTP config (URL, method, headers, body) or Logic config (provider, type, else logic) | Per node type |

Each section must have:
- A clearly visible section heading (e.g. bold label or a styled header bar).
- Visual separation from adjacent sections (border, background colour change, or sufficient spacing — designer's choice, consistent across both forms).
- The sections must appear in the order listed: Description → Rules → Action.

### AC-30-2 — Section headings are scannable

- Headings use a consistent visual treatment (e.g. `text-xs font-semibold uppercase tracking-wide text-zinc-500` or an equivalent clearly legible style).
- The heading text for the three panels must be: **"Node Description"**, **"Rules"**, **"Action"** (or equivalent clear labels approved by a human reviewer).

### AC-30-3 — JSON auto-formatting in text fields

For every `<Input.TextArea>` or `<Input>` field that is expected to contain JSON (fields named: `httpRequestHeaders`, `httpRequestBody`, `trackingNumberSchemaInHttpResponse`, `elseLogic`):

- **On blur** (when the user leaves the field): if the field value is valid JSON, it is **pretty-printed** (2-space indent, `JSON.stringify(parsed, null, 2)`).
- If the value is **not valid JSON** (parse throws), the content is **left unchanged** — no corruption of user input.
- The formatting happens silently — no modal, no toast — just the text field content updates.
- The re-formatted value is propagated via `onValuesChange` so the parent canvas receives the formatted string.

### AC-30-4 — Non-JSON fields are not affected

Fields that are not expected to contain JSON (`description`, `ruleList[].key`, `ruleList[].remark`, `provider`, `type`, `remark`, `httpRequestMethod`, URL fields) must **not** trigger any formatting on blur.

### AC-30-5 — No data loss

- Smart formatting must never modify a value that is not valid JSON.
- After formatting, the form state and the canvas node data must reflect the formatted (or original, if not valid JSON) value.
- Round-trip test: format → save → reload → the stored value must be identical to the formatted string.

### AC-30-6 — Layout consistent across node types

- `HttpCallForm` and `LogicForm` must use the same section heading component/style so the panels look identical in structure across node types.
- A shared `NodeSection` wrapper component may be introduced to enforce consistency.

### AC-30-7 — Mobile layout preserved

- The three-panel layout must not break the mobile drawer layout (full-width, `useIsMobile`).
- Section headings and panel borders must render correctly on small viewports.

### AC-30-8 — Existing functionality unchanged

- All existing form fields, their names, tooltips, and validation remain unchanged.
- `onValuesChange` continues to fire correctly and propagate to the canvas.
- Node selection and drawer open/close behaviour unchanged.

---

## 4. Out of Scope

| Item | Rationale |
|---|---|
| XML or YAML formatting | JSON only for this release |
| Syntax highlighting / rich editor | Future enhancement; plain textarea with formatting sufficient for now |
| Field-level JSON schema validation | Future enhancement |
| Adding new form fields | This release is layout/UX only |
| Backend changes | No backend changes required |

---

## 5. Coordination

- **Frontend dev**: implement `NodeSection` wrapper and `useJsonFormat` hook (or inline blur handler) in `HttpCallForm` and `LogicForm`.
- **QA**: test JSON formatting with valid JSON, invalid JSON, empty string, and large payloads.
- **Architect**: review the `NodeSection` approach and JSON formatting implementation (see Arch Doc v3.0).

---

*End of PM Doc v3.0 — Artboard node editor 3-panel + JSON format — Draft, awaiting approval.*
