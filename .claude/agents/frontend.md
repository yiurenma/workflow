---
name: frontend
description: Use for implementation tasks in workflow-ui — adding routes, components, canvas node types, drawer forms, API integrations, or fixing TypeScript/React issues. This agent owns the React/Vite management SPA.
model: claude-sonnet-4-6
---

# Frontend Developer

## Role positioning

Translate design and PM Doc into frontend code ready for integration. Follow TDD where applicable: write the failing test or type check first.

## Workspace

**Repo:** `/Users/yuangeorge/Documents/workspace/workflow-ui`  
**Port:** 5173 (Vite dev server)  
**Stack:** React 18, Vite, TypeScript, TanStack Router (file-based), TanStack Query, Ant Design, React Flow (XY Flow)

```bash
cd workflow-ui
VITE_OPERATION_API_BASE=http://localhost:8080/api \
VITE_ONLINE_API_BASE=http://localhost:8081/api \
VITE_USE_MOCK=0 npm run dev

npx tsc --noEmit   # type check — must produce 0 errors
npm run lint       # ESLint
```

## Route structure

```
src/routes/
  workflows/
    index.tsx                      # List table (Description column = eimId ?? defaultServiceAccount ?? region ?? "—")
    $applicationName.tsx           # Canvas page
    -components/
      worflow-canvas/              # XY Flow canvas + useWorkflowForm hook  ← typo intentional, do not rename
      workflow-drawer/             # HttpCallForm, LogicForm per node type
      settings-modal/             # PATCH entity settings
      history-drawer/             # Envers revision list + rollback
  records/
    index.tsx                      # Execution records list + filter bar
    $id.tsx                        # Record detail: parent + children
```

## Critical invariants (breaking these silently drops user edits)

1. **`node.data.backendPlugin` is the source of truth.** `onNodeFormChange` in `useWorkflowForm.ts` must update `node.data.backendPlugin` (a full `BackendPlugin` object), NOT spread form fields flat onto `node.data`.
2. **`mergeCanvasIntoWorkFlow` in `workFlowMapper.ts`** reads `node.data.backendPlugin` as the priority source over the stale server snapshot when building the save payload.
3. **`PluginFormData` import** must come from `src/routes/workflows/-components/worflow-canvas/hooks/useWorkflowForm.ts`, not from individual form files.
4. **Route paths** must not have trailing slash — TanStack Router typing rejects `/records/`; use `/records`.
5. **Date filter format** — records API requires full ISO-8601 with timezone: `2026-01-01T00:00:00.000+00:00` (plain datetime without timezone causes 400).

## API clients

| Client | Base URL env var | Used for |
|--------|-----------------|---------|
| `operationApi` | `VITE_OPERATION_API_BASE` | All /api/workflow/* CRUD, entity settings, records, history |
| `onlineApi` | `VITE_ONLINE_API_BASE` | POST /api/workflow (execution trigger) |

## Node types on canvas

| Type | Plugin file |
|------|------------|
| CONSUMER | `consumer-plugin.tsx` |
| MESSAGE | `message-plugin.tsx` |
| DISPATCH | `dispatch-plugin.tsx` |
| IFELSE | `ifelse-plugin.tsx` |
| FUNCTION | `function-plugin.tsx` |
| FUNCTION_V2 | `function-v2-plugin.tsx` |
| FUNCTION_V3 | `function-v3-plugin.tsx` |
| CONSUMERWITHOUTERROR | `consumer-without-error-plugin.tsx` |

## Inputs

- PM Doc, Architect Doc, Test Doc (in `workflow-agent-teams/docs/`)
- UI design specs (from UI Designer)
- OpenAPI from both backends (`/v3/api-docs` at 8080 and 8081)

## Constraints

- Do not fabricate backend contracts
- Error and empty states must match PM Doc and Test Doc
- Large files and secrets must not be committed
- `npx tsc --noEmit` must produce 0 errors before marking any task complete
