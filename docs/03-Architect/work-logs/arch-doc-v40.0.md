# Architecture Document v40.0

**Document version:** 40.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-validation-relax-uiMapList-ifelse-edges-vs-pluginList`

## Overview

This document describes the technical approach to relax validation rules for IFELSE branch edges in workflow import/validation logic. The current validator incorrectly treats IFELSE virtual branch identifiers as missing nodes, causing false-positive validation failures.

## Problem Analysis

### Current Validation Logic

The workflow validator in `workflow-ui` checks that every `source` and `target` in `uiMapList` edges exists in `pluginList`:

```typescript
// Current logic (simplified)
for (const edge of uiMapList) {
  if (!pluginList.find(p => p.id === edge.source)) {
    errors.push(`Node '${edge.source}' not found in pluginList`);
  }
  if (!pluginList.find(p => p.id === edge.target)) {
    errors.push(`Node '${edge.target}' not found in pluginList`);
  }
}
```

### IFELSE Branch Structure

IFELSE nodes create virtual branch points that appear in edges but not as standalone plugin entries:

**pluginList entry:**
```json
{
  "id": "IFELSE_1",
  "action": {
    "type": "IFELSE",
    "logic": { ... }
  }
}
```

**uiMapList edges:**
```json
[
  { "source": "HTTP_CALL_1", "target": "IFELSE_1" },
  { "source": "IFELSE_1_true", "target": "MESSAGE_1" },
  { "source": "IFELSE_1_false", "target": "MESSAGE_2" }
]
```

The validator fails on `IFELSE_1_true` and `IFELSE_1_false` because they don't exist in `pluginList`.

### Branch Identifier Pattern

Based on UAT observations and existing workflows:
- Pattern: `{nodeId}_{branch}` where `{nodeId}` is an IFELSE node ID and `{branch}` is `true` or `false`
- Case-insensitive: both `IFELSE_1_true` and `IFELSE_1_TRUE` appear in production data
- Examples: `IFELSE_1_true`, `IFELSE_1_false`, `IFELSE_2_TRUE`, `IFELSE_2_FALSE`

## Solution Design

### Approach

Modify the validation logic to recognize IFELSE branch identifiers as valid edge endpoints without requiring them in `pluginList`.

### Implementation Location

**File:** `workflow-ui/src/utils/workflowValidation.ts` (or equivalent validation module)

If validation is currently inline in `ImportWorkflowModal.tsx`, extract it to a dedicated utility module first.

### Validation Logic Changes

```typescript
// New helper function
function isIFELSEBranchId(id: string, pluginList: Plugin[]): boolean {
  // Pattern: {nodeId}_true or {nodeId}_false (case-insensitive)
  const match = id.match(/^(.+)_(true|false)$/i);
  if (!match) return false;
  
  const [, nodeId, branch] = match;
  
  // Check if parent IFELSE node exists
  const parentNode = pluginList.find(p => p.id === nodeId);
  return parentNode?.action?.type === 'IFELSE';
}

// Updated validation logic
function validateEdges(uiMapList: Edge[], pluginList: Plugin[]): ValidationError[] {
  const errors: ValidationError[] = [];
  
  for (const edge of uiMapList) {
    // Validate source
    if (!pluginList.find(p => p.id === edge.source) && 
        !isIFELSEBranchId(edge.source, pluginList)) {
      errors.push({
        path: `uiMapList[${index}].source`,
        message: `Node '${edge.source}' referenced in edge but not found in pluginList`
      });
    }
    
    // Validate target
    if (!pluginList.find(p => p.id === edge.target) && 
        !isIFELSEBranchId(edge.target, pluginList)) {
      errors.push({
        path: `uiMapList[${index}].target`,
        message: `Node '${edge.target}' referenced in edge but not found in pluginList`
      });
    }
  }
  
  return errors;
}
```

### Edge Cases

1. **Orphan branch reference:** `IFELSE_1_true` exists but `IFELSE_1` does not
   - **Behavior:** Validation fails (parent node must exist)
   - **Error message:** "IFELSE branch 'IFELSE_1_true' references missing parent node 'IFELSE_1'"

2. **Branch reference to non-IFELSE node:** `HTTP_CALL_1_true` where `HTTP_CALL_1` is not IFELSE
   - **Behavior:** Validation fails (parent must be IFELSE type)
   - **Error message:** "Node 'HTTP_CALL_1_true' not found in pluginList"

3. **Invalid branch suffix:** `IFELSE_1_maybe`
   - **Behavior:** Validation fails (only `true`/`false` allowed)
   - **Error message:** "Node 'IFELSE_1_maybe' not found in pluginList"

4. **Nested IFELSE:** `IFELSE_1_true` → `IFELSE_2` → `IFELSE_2_false`
   - **Behavior:** All validate correctly if parent nodes exist

5. **Multiple IFELSE nodes:** `IFELSE_1`, `IFELSE_2`, `IFELSE_3` each with branches
   - **Behavior:** Each branch validates against its own parent

## Data Flow

```
User imports workflow JSON
         ↓
Parse JSON
         ↓
Validate structure
         ↓
Validate edges ← NEW LOGIC HERE
  ├─ Check if source/target in pluginList
  └─ OR check if IFELSE branch pattern + parent exists
         ↓
Display errors or proceed
```

## API Contract

**No backend changes required.** This is purely client-side validation logic.

The backend already accepts and stores workflows with IFELSE branches correctly. The issue is only in the frontend validator.

## Testing Strategy

### Unit Tests

Create `workflowValidation.test.ts`:

```typescript
describe('IFELSE branch validation', () => {
  it('should accept valid IFELSE branch edges', () => {
    const workflow = {
      pluginList: [
        { id: 'IFELSE_1', action: { type: 'IFELSE' } },
        { id: 'MESSAGE_1', action: { type: 'MESSAGE' } }
      ],
      uiMapList: [
        { source: 'IFELSE_1_true', target: 'MESSAGE_1' }
      ]
    };
    
    const errors = validateWorkflow(workflow);
    expect(errors).toHaveLength(0);
  });
  
  it('should reject IFELSE branch without parent node', () => {
    const workflow = {
      pluginList: [
        { id: 'MESSAGE_1', action: { type: 'MESSAGE' } }
      ],
      uiMapList: [
        { source: 'IFELSE_1_true', target: 'MESSAGE_1' }
      ]
    };
    
    const errors = validateWorkflow(workflow);
    expect(errors).toContainEqual(
      expect.objectContaining({
        message: expect.stringContaining('IFELSE_1')
      })
    );
  });
  
  it('should reject branch reference to non-IFELSE node', () => {
    const workflow = {
      pluginList: [
        { id: 'HTTP_CALL_1', action: { type: 'CONSUMER' } },
        { id: 'MESSAGE_1', action: { type: 'MESSAGE' } }
      ],
      uiMapList: [
        { source: 'HTTP_CALL_1_true', target: 'MESSAGE_1' }
      ]
    };
    
    const errors = validateWorkflow(workflow);
    expect(errors.length).toBeGreaterThan(0);
  });
  
  it('should handle case-insensitive branch suffixes', () => {
    const workflow = {
      pluginList: [
        { id: 'IFELSE_1', action: { type: 'IFELSE' } },
        { id: 'MESSAGE_1', action: { type: 'MESSAGE' } }
      ],
      uiMapList: [
        { source: 'IFELSE_1_TRUE', target: 'MESSAGE_1' },
        { source: 'IFELSE_1_false', target: 'MESSAGE_1' }
      ]
    };
    
    const errors = validateWorkflow(workflow);
    expect(errors).toHaveLength(0);
  });
});
```

### Integration Tests

Update existing import tests to include IFELSE workflows:

- TC-IMPORT-IFELSE-01: Import workflow with single IFELSE node and TRUE/FALSE branches
- TC-IMPORT-IFELSE-02: Import workflow with multiple IFELSE nodes
- TC-IMPORT-IFELSE-03: Import workflow with nested IFELSE (IFELSE inside branch)
- TC-IMPORT-IFELSE-04: Reject workflow with orphan IFELSE branch (parent missing)
- TC-IMPORT-IFELSE-05: Reject workflow with invalid branch suffix

## Rollout Plan

1. **Extract validation logic** to `workflowValidation.ts` if not already modular
2. **Implement IFELSE branch recognition** with unit tests
3. **Update error messages** for clarity
4. **Run existing E2E test suite** to ensure no regressions
5. **Add new E2E tests** for IFELSE workflows
6. **Deploy to UAT** and verify with real IFELSE workflows
7. **Monitor** for any edge cases in production

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Relaxing validation too much allows invalid workflows | High | Only relax for specific IFELSE pattern; maintain strict validation for all other cases |
| IFELSE branch naming convention changes in future | Medium | Use regex pattern that can be easily updated; document convention |
| Existing workflows with different branch patterns fail | Medium | Test with real UAT workflows before deploying; add logging to identify patterns |
| Performance impact of regex matching on large workflows | Low | Regex is simple and only runs on edges (typically < 100 per workflow) |

## Open Questions

1. **Q:** Are there other node types besides IFELSE that use virtual branch identifiers?
   **A:** To be confirmed by reviewing `workFlowMapper.ts` and backend code. If found, extend pattern matching.

2. **Q:** Should we validate that both TRUE and FALSE branches exist for every IFELSE node?
   **A:** No. Workflows may have only one branch (e.g., "do X if condition, otherwise do nothing"). This is valid.

3. **Q:** What if branch suffix is capitalized differently in different parts of the workflow?
   **A:** Use case-insensitive matching (`/_(true|false)$/i`) to handle all variations.

## Dependencies

- No backend changes required
- No API contract changes
- No database schema changes
- Requires coordination with QA for test case coverage

## Success Criteria

- Zero validation failures for valid IFELSE workflows in UAT
- Validation still catches genuinely missing node references (no false negatives)
- All existing E2E tests pass (no regressions)
- New E2E tests for IFELSE workflows pass
