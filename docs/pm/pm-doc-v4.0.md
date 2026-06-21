# PM Doc — Node Editor Cross-Audience Clarity

**Label**: `TODO-node-editor-cross-audience-clarity`  
**Version**: 4.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Scope**: workflow-ui only — drawer panel UX  
**Input**: TODO.md item "Node editor — cross-audience clarity (dev / QA / business)"

---

## 1. Background

The node editor drawer now has three visually distinct panels (Node Description / Rules / Action) from US-30. However, the panel headings alone do not explain to different audiences what each section means or what they should do with it. A developer knows what "ruleList" maps to; a business user does not. This release (US-31) adds plain-English subtitles and field-level hints so the drawer is self-explanatory for all three audiences.

---

## 2. User Stories

**US-31-DEV — Developer clarity**
> **As a** developer configuring a workflow node,  
> **I want** each panel to show the persisted field names it maps to,  
> **so that** I can understand what gets saved to the backend without consulting external docs.

**US-31-QA — QA clarity**
> **As a** QA engineer testing a workflow node,  
> **I want** clear section boundaries with independent save/load behaviour documented,  
> **so that** I can write and run regression cases for each panel in isolation.

**US-31-BIZ — Business/product clarity**
> **As a** product owner reviewing a workflow node,  
> **I want** a plain-English one-sentence definition under each panel heading,  
> **so that** I understand what each section controls without reading technical tooltips.

---

## 3. Acceptance Criteria

### AC-31-1 — Section subtitles (all three panels, both forms)

Each `NodeSection` panel must display a **subtitle** — a short plain-English sentence directly under the heading — using consistent styling across all panels and both forms (`HttpCallForm` and `LogicForm`).

| Panel | Subtitle text |
|---|---|
| Node Description | "The name shown on the canvas. What is this step called?" |
| Rules | "Run only when… — conditions that must all match before this step executes." |
| Action | "What the system does when this step runs." |

### AC-31-2 — Developer field map (section heading tooltip)

Each panel heading must have a tooltip showing the persisted backend field name(s) for that section, so developers can cross-reference with the API:

| Panel | Tooltip content |
|---|---|
| Node Description | `Persisted as: plugin.description` |
| Rules | `Persisted as: plugin.ruleList[].key + ruleList[].remark` |
| Action | `Persisted as: plugin.action (type, provider, httpRequest*, elseLogic, …)` |

### AC-31-3 — Subtitle styling

- Subtitle uses `text-xs text-zinc-400` (lighter than the heading, clearly secondary).
- Subtitle appears on a new line immediately below the section heading, before the form fields.
- Consistent across `HttpCallForm` and `LogicForm`.

### AC-31-4 — No layout breakage

- Existing form fields, tooltips, and spacing unchanged.
- Mobile layout (full-width drawer) must render correctly.

### AC-31-5 — No backend changes

- Pure frontend change. No API, schema, or env changes.

---

## 4. Out of Scope

| Item | Rationale |
|---|---|
| Color legend matching canvas node cards | Marked optional in TODO; deferred |
| Collapsible help / "What is this?" accordion | Subtitle is sufficient for this release |
| Linking to external API docs | URL not yet defined; deferred |

---

## 5. Coordination

- **Frontend dev**: update `NodeSection` to accept `subtitle` and `headingTooltip` props; update both forms.
- **Test Manager**: add section-isolation test rows (save Description only, Rules only, Action only).

---

*End of PM Doc v4.0 — Draft, awaiting approval.*
