# Product Requirements — Workflow Studio Unified Program Part 5: AI Quality (v32.0)

**Document version:** 32.0 Part 5  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** AI workflow generator semantic quality improvements  
**Master label:** `TODO-workflow-studio-unified-product-ux-program`  
**Part label:** `TODO-canvas-ai-workflow-generator-quality-prompt-semantics`

---

## 1. Overview

The workflow generator (v23.0) produces syntactically valid JSON that renders on canvas, but semantic quality is poor: wrong plugin IDs, incorrect action kinds, invalid rule keys (JSONPath), wrong rule types, broken edges.

**Goal:** Improve generator output quality through better prompts, validation, and repair mechanisms.

---

## 2. User Stories

### US-AI-01 — Workflow generator produces semantically correct workflows

**As a** workflow author,  
**I want** the AI workflow generator to produce workflows with correct plugin IDs, action kinds, rule keys, and edges,  
**So that** generated workflows work correctly without manual fixes.

**Acceptance Criteria:**

**AC-AI-01-01:** **Baseline shipped:** v23.0 `WorkflowGeneratorModal.tsx`, `WORKFLOW_GENERATOR_SYSTEM_PROMPT` (`TODO-canvas-ai-workflow-json-copilot-replace`).

**AC-AI-01-02:** **Gap identified:** Model output often parses and renders but semantics are wrong:
- Plugin IDs don't match registered plugins
- Action kinds invalid for plugin type
- Rule keys not valid JSONPath expressions
- Rule types wrong (e.g., `string` instead of `number`)
- Edges point to non-existent node IDs

**AC-AI-01-03:** **Measurement loop (UAT):**
- App: `E2E_TEST_CANVAS` on `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Run **Explain** on reference workflow → get description
- Paste description into **Generate** → get generated workflow
- Compare output to golden workflow (open nodes, inspect rules/actions)
- Document failure **patterns** with screenshots + JSON snippets

**AC-AI-01-04:** **Fix levers:**
- **Stronger system prompt** with canonical examples of valid workflows
- **Enumerated allowed shapes:** List valid plugin IDs, action kinds, rule types
- **Post-parse validation:** Check generated JSON against schema; flag errors
- **User-visible repair:** Show validation errors; let user fix or regenerate

**AC-AI-01-05:** **Playwright extension:** Extend beyond TC-GENERATOR-01/02 if feasible (e.g., validate generated workflow structure, not just modal open/close).

---

## 3. Out of Scope

- Backend API changes (generator is client-side)
- New AI models or providers
- Real-time streaming generation (future enhancement)

---

## 4. Success Metrics

- Measurement loop documented with failure patterns
- System prompt updated with canonical examples
- Post-parse validation catches common errors
- User can see validation errors and regenerate
- Optional: Playwright test validates generated workflow structure

---

## 5. References

- `pm-doc-v23.0.md` — v23.0 generator baseline
- `arch-doc-v23.0.md` — v23.0 technical design
- `docs/e2e-testing-guide.md` — E2E conventions
- `docs/e2e-full-test-guide.md` — Full E2E guide
