# PM Doc v16.0 — Canvas: JsonPath Playground Modal

**Status:** Awaiting approval
**Label:** `TODO-canvas-json-path-verification-modal`
**Date:** 2026-04-08

---

## Problem Statement

Workflow rules use JsonPath expressions (Jayway JsonPath syntax) to select fields from incoming messages. Authors currently have no way to verify that a JsonPath expression returns the expected value from a sample document before publishing a workflow. They must deploy and test in production or use an external tool.

---

## User Story

As a **workflow author** on the canvas,
I want a "JsonPath" button near the existing Explain/Run/Save controls,
So that I can paste a JSON document and a JsonPath expression, run the match client-side, and see the matched value(s) or a clear error — without leaving the app.

---

## Acceptance Criteria

1. A **"JsonPath"** button appears in the `WorkflowHeader` toolbar alongside Explain / Run / Save.
2. Clicking the button opens a **modal** containing:
   - A **JsonPath expression** field (text input, single line, e.g. `$.customer.id`).
   - A **JSON document** field (multiline textarea, accepts arbitrary JSON).
   - A **"Validate"** (or "Run") button.
3. On Validate:
   - If the JSON is **invalid**, show a clear error: `"Invalid JSON: <parse message>"`.
   - If the path is **invalid or produces no match**, show: `"No match"` or the JSONPath library error.
   - If successful, show the matched value(s) — scalar, array, or structured — in a readable format (formatted JSON or plain value).
4. The evaluation is **client-side only** — no backend call.
5. The modal is accessible on **both desktop and mobile**.
6. No changes to canvas data flow or save logic.

---

## Library Choice

Use `jsonpath-plus` (npm package `jsonpath-plus`, MIT license). It implements Jayway JsonPath semantics including recursive descent (`..`), filters (`[?(@.key == 'value')]`), and array slicing. Client-side evaluation in browser is supported.
