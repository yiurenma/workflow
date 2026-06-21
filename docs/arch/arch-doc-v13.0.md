# Arch Doc v13.0 — Canvas: Defensive null-guard for workflow data

**Label:** `TODO-canvas-workflow-undefined-crash`
**Date:** 2026-04-08

---

## Scope

Single file change: `workflow-ui/src/routes/workflows/-components/worflow-canvas/hooks/useWorkflowState.ts`

---

## Root Cause Detail

`$applicationName.tsx` already guards `workFlow == null` (shows 404 result) and
`workFlowToNodesAndEdges` already uses optional chaining (`workFlow?.pluginList?.length`).
However, the `useEffect` in `useWorkflowState` calls the mapper without a `try/catch`:

```ts
useEffect(() => {
  if (workFlow != null) {
    const mapped = workFlowToNodesAndEdges(workFlow);  // can throw on malformed data
    setNodes(mapped.nodes);
    setEdges(mapped.edges);
  }
}, [applicationName, workFlow, setNodes, setEdges]);
```

If the mapper throws for any reason (malformed `uiMap`, unexpected shape from server),
the exception propagates unhandled, triggering React's error boundary as a blank white screen.

---

## Fix

Wrap the mapper call in `useWorkflowState.ts` with `try/catch`. On error log and fall back to empty canvas:

```ts
useEffect(() => {
  if (workFlow != null) {
    try {
      const mapped = workFlowToNodesAndEdges(workFlow);
      setNodes(mapped.nodes);
      setEdges(mapped.edges);
    } catch (err) {
      console.error('[WorkflowState] Failed to map workflow to nodes/edges:', err);
      setNodes([]);
      setEdges([]);
    }
  } else {
    setNodes([]);
    setEdges([]);
  }
}, [applicationName, workFlow, setNodes, setEdges]);
```

No other files change. The `$applicationName.tsx` null/error route guards remain unchanged.

---

## Diff Summary

| File | Change |
|------|--------|
| `hooks/useWorkflowState.ts` | Add `try/catch` around `workFlowToNodesAndEdges` call (lines 54–66) |

---

## No New Dependencies

No new packages required.

---

## Risk

Low. The change is purely defensive — wraps existing logic, does not alter the happy path.
