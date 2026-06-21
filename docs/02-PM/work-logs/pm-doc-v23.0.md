# PM Doc v23.0 — Canvas AI: Workflow Generator (Copilot JSON replaces canvas)

**Status:** Awaiting approval
**Label:** `TODO-canvas-ai-workflow-json-copilot-replace`
**Date:** 2026-04-08

---

## Problem Statement

Building a workflow from scratch requires knowing all node types and their configuration. A user should be able to describe in plain English what they need and have AI generate a complete, valid `WorkFlow` JSON that they can preview and apply to the canvas with one click.

## User Story

As a **workflow author**,
I want a "Generate" action next to Explain in the canvas toolbar,
So that I can describe my workflow requirements in natural language and have AI produce a ready-to-use canvas layout that I can review and apply.

## Acceptance Criteria

1. A **"Generate"** button appears in the `WorkflowHeader` toolbar (desktop: direct button; mobile: in `⋯` overflow menu, same as Explain).
2. Clicking opens a modal with: a **requirements textarea**, the **current workflow JSON** (optional context, pre-filled), and a **Generate** button.
3. On Generate: calls GitHub Models / Anthropic API using a **system prompt** (`WORKFLOW_GENERATOR_SYSTEM_PROMPT`) + user message (app name + requirements + optional current JSON). Uses `role: "system"` + `role: "user"`.
4. **`max_tokens`** is set to **4096** (not 1024 as in Explain) to accommodate large graphs.
5. The response is parsed as JSON and validated against the `WorkFlow` shape (`pluginList`, `uiMapList`, each plugin having `uiMap.id`, `uiMap.position`).
6. A **preview / confirm step** shows the user how many nodes will be created before applying.
7. On confirm, the canvas nodes and edges are **replaced** with the generated workflow.
8. **Accident handling** — all 8 TC-GEN test cases (see Test doc) are covered: happy path, invalid JSON, truncation, wrong shape, markdown fences, API error, cancel, token limit.
9. Requires no backend changes — client-side only, same token mechanism as Explain.
