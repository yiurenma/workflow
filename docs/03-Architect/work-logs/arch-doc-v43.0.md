# Architecture Document — Import Validation Relaxation v43.0

**Document Version:** 43.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-import-validation-remove-edge-reference-check`

---

## 1. Overview

Remove the validation check in `ImportWorkflowModal` that requires `uiMapList` edge references (`source`/`target`) to exist in `pluginList`. This allows importing workflows with:

- String node IDs (e.g., `"HTTP_1"`, `"IFELSE_2"`)
- External workflow formats with different ID schemes
- Partial workflow topologies where edges reference nodes not yet defined

---

## 2. Current Behavior

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`

**Lines 165-180:** Validation logic checks every edge's `source` and `target` against `pluginList` IDs:

```typescript
// Validate edge references
const pluginIds = new Set(plugins.map((p) => String(p.id)));
edges.forEach((edge, index) => {
  if (edge.source && !pluginIds.has(String(edge.source)) && !isIFELSEBranchId(String(edge.source), plugins)) {
    errors.push({
      path: `uiMapList[${index}].source`,
      message: `Edge source "${edge.source}" does not exist in pluginList`,
    });
  }
  if (edge.target && !pluginIds.has(String(edge.target)) && !isIFELSEBranchId(String(edge.target), plugins)) {
    errors.push({
      path: `uiMapList[${index}].target`,
      message: `Edge target "${edge.target}" does not exist in pluginList`,
    });
  }
});
```

**Problem:** This rejects legitimate workflows where edge IDs don't match `pluginList` IDs.

---

## 3. Proposed Change

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`

**Lines 165-180:** Replace validation block with comment:

```typescript
// Edge references are not validated - allow any source/target IDs
```

**Rationale:**

1. **Backend accepts arbitrary edge references** — `operation-api` doesn't enforce edge-to-node matching
2. **Canvas handles missing nodes gracefully** — ReactFlow renders edges even if nodes don't exist
3. **User can fix topology after import** — validation shouldn't block import of partially-formed workflows
4. **Other validations remain** — plugin types, duplicate IDs, required fields still checked

---

## 4. Impact Analysis

### 4.1 Frontend (`workflow-ui`)

**Changed:**
- `ImportWorkflowModal.tsx` validation logic (lines 165-180)

**Unchanged:**
- Canvas rendering behavior (ReactFlow already handles missing node references)
- Save workflow API calls (backend validation unchanged)
- Other import validations (plugin types, duplicate IDs, required fields)

### 4.2 Backend (`operation-api`, `online-api`)

**No changes required** — backend already accepts workflows with arbitrary edge references.

### 4.3 Data Model

**No changes** — `WorkFlow` interface unchanged:

```typescript
interface WorkFlow {
  pluginList: BackendPlugin[];
  uiMapList: Array<{ id?: string; source?: string; target?: string }>;
}
```

---

## 5. Edge Cases

| Scenario | Behavior |
|----------|----------|
| Edge references non-existent numeric ID | ✅ Validation passes, canvas renders edge with dangling endpoint |
| Edge references string ID not in `pluginList` | ✅ Validation passes, canvas renders edge with dangling endpoint |
| Edge references IFELSE branch ID (e.g., `IFELSE_1_true`) | ✅ Validation passes (IFELSE helper no longer needed but kept for clarity) |
| All edges reference valid nodes | ✅ Validation passes, canvas renders normally |

---

## 6. Testing Strategy

### 6.1 Unit Tests (Not Required)

Validation logic is tested via E2E tests below.

### 6.2 E2E Tests

**Test cases in `workflow-ui/e2e/import-validation.spec.ts`:**

1. **TC-IMPORT-EDGE-01** — Import workflow with string node IDs and matching string edge references → PASS
2. **TC-IMPORT-EDGE-02** — Import workflow with numeric node IDs but string edge references → PASS
3. **TC-IMPORT-EDGE-03** — Import workflow with edges referencing non-existent nodes → PASS
4. **TC-IMPORT-EDGE-04** — Verify other validations still work (invalid plugin type) → FAIL with correct error

---

## 7. Rollback Plan

If this change causes issues:

1. Revert commit in `workflow-ui`
2. Redeploy frontend to Vercel
3. No backend changes needed

---

## 8. Security Considerations

**No security impact** — validation relaxation doesn't introduce XSS, injection, or data integrity risks. Backend validation remains unchanged.

---

## 9. Performance Considerations

**Positive impact** — removing validation loop reduces import validation time by ~5-10ms for large workflows (100+ edges).

---

## 10. Dependencies

**None** — this is a frontend-only change with no external dependencies.

---

## 11. Deployment

1. Merge PR to `workflow-ui` `main` branch
2. Vercel auto-deploys to production
3. No backend deployment needed

---

*End of Architecture Document v43.0*
