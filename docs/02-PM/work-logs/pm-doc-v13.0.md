# PM Doc v13.0 — Canvas: Defensive null-guard for workflow data

**Status:** Awaiting approval
**Label:** `TODO-canvas-workflow-undefined-crash`
**Date:** 2026-04-08

## Problem Statement

The canvas page can crash with `Cannot read properties of undefined (reading 'pluginList')` when an application's workflow data is missing, malformed, or returns an unexpected API shape (e.g. a 200 with a wrapped envelope instead of a bare WorkFlow object).

## User Story

As a **developer/user** opening a workflow canvas,
I want the canvas to degrade gracefully when workflow data is unavailable or malformed,
So that I see a clear "not found" or empty-canvas state instead of a JavaScript crash.

## Acceptance Criteria

1. If the API returns `null`, `undefined`, or a non-WorkFlow shape, the canvas shows "Application not found" (404 result) — no JS crash.
2. If `workFlow.pluginList` is `undefined` (not absent entirely), the canvas renders empty (0 nodes) — no crash.
3. All existing tests continue to pass.
4. Error boundary or try/catch around the mapper call prevents any uncaught exception from propagating to React's error boundary as a blank white screen.
