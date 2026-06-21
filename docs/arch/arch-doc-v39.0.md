# Architecture Document — Import Plugin Type Validation Alignment (v39.0)

**Document version:** 39.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Recognition label:** `TODO-import-plugin-types-match-api-mapper`

---

## 1. Overview

This document specifies the technical approach to fix the plugin type validation mismatch in `ImportWorkflowModal.tsx` so that it accepts the 6 actual wire-format types stored by the backend, rather than the incorrect UI-only enum names.

---

## 2. Root Cause Analysis

### Current State

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`  
**Line 47:** `const VALID_PLUGIN_TYPES = ["FUNCTION_V2", "FUNCTION_V3", "HTTP_CALL", "LOGIC"];`

**Problem:** This list contains:
- ❌ `HTTP_CALL` — never stored, this is `Plugin.CONSUMER` enum name
- ❌ `LOGIC` — never stored, this is `Plugin.IF_ELSE` enum name
- ✅ `FUNCTION_V2` — correct
- ✅ `FUNCTION_V3` — correct

**Missing types:**
- `CONSUMER` (HTTP call without error handling)
- `CONSUMERWITHOUTERROR` (HTTP call with error suppression)
- `IFELSE` (conditional logic)
- `MESSAGE` (message/notification step)

### Reference Implementation

**File:** `workflow-ui/src/api/mappers/workFlowMapper.ts`

**Lines 11-20** — `backendTypeToPlugin()` (what the system reads):
```typescript
export function backendTypeToPlugin(t: string): Plugin {
  const u = (t || '').toUpperCase().replace(/-/g, '');
  if (u === 'CONSUMER') return Plugin.CONSUMER;
  if (u === 'CONSUMERWITHOUTERROR') return Plugin.CONSUMER_WITHOUT_ERROR;
  if (u === 'IFELSE') return Plugin.IF_ELSE;
  if (u === 'MESSAGE') return Plugin.MESSAGE;
  if (u === 'FUNCTION_V2' || u === 'FUNCTION') return Plugin.FUNCTION;
  if (u === 'FUNCTION_V3') return Plugin.FUNCTION_V3;
  return Plugin.FUNCTION;
}
```

**Lines 22-39** — `pluginToBackendType()` (what the system writes):
```typescript
export function pluginToBackendType(p: Plugin): string {
  switch (p) {
    case Plugin.CONSUMER:
      return 'CONSUMER';
    case Plugin.CONSUMER_WITHOUT_ERROR:
      return 'CONSUMERWITHOUTERROR';
    case Plugin.IF_ELSE:
      return 'IFELSE';
    case Plugin.MESSAGE:
      return 'MESSAGE';
    case Plugin.FUNCTION:
      return 'FUNCTION_V2';
    case Plugin.FUNCTION_V3:
      return 'FUNCTION_V3';
    default:
      return 'CONSUMER';
  }
}
```

**Key observations:**
1. Wire format uses uppercase, no underscores: `CONSUMER`, `CONSUMERWITHOUTERROR`, `IFELSE`, `MESSAGE`
2. `FUNCTION_V2` and `FUNCTION_V3` have underscores (exception to the rule)
3. `FUNCTION` (v1) is accepted on **read** for backward compatibility but never written
4. `HTTP_CALL` and `LOGIC` are UI-only enum names, never appear in wire format

---

## 3. Solution Design

### 3.1 Fix Validation Constant

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`  
**Line 47**

**Change:**
```typescript
// Before
const VALID_PLUGIN_TYPES = ["FUNCTION_V2", "FUNCTION_V3", "HTTP_CALL", "LOGIC"];

// After
const VALID_PLUGIN_TYPES = [
  "CONSUMER",
  "CONSUMERWITHOUTERROR",
  "IFELSE",
  "MESSAGE",
  "FUNCTION_V2",
  "FUNCTION_V3"
];
```

**Rationale:**
- Matches `pluginToBackendType()` output exactly
- Covers all 6 node types the system can store
- Removes UI-only enum names that cause false validation errors

### 3.2 Update Error Message

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`  
**Lines 105-109**

**Current:**
```typescript
errors.push({
  path: `pluginList[${index}].action.type`,
  message: `Invalid plugin type: "${plugin.action.type}" (must be one of: ${VALID_PLUGIN_TYPES.join(", ")})`,
});
```

**After fix:** Error message will automatically show correct types:
```
Invalid plugin type: "HTTP_CALL" (must be one of: CONSUMER, CONSUMERWITHOUTERROR, IFELSE, MESSAGE, FUNCTION_V2, FUNCTION_V3)
```

No code change needed — the message already references `VALID_PLUGIN_TYPES.join(", ")`.

### 3.3 Update Example JSON

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`  
**Lines 28-45**

**Current example uses:** `"type": "FUNCTION_V2"`

**Recommendation:** Keep as-is (already correct) or optionally show a multi-node example with different types.

**Optional enhancement (not required for this fix):**
```typescript
const EXAMPLE_JSON = `{
  "pluginList": [
    {
      "id": 1,
      "description": "Fetch user data",
      "action": { "type": "CONSUMER", "provider": "http", "remark": "GET /users" }
    },
    {
      "id": 2,
      "description": "Check amount",
      "action": { "type": "IFELSE", "provider": "logic", "remark": "$.amount > 100" }
    },
    {
      "id": 3,
      "description": "Process payment",
      "action": { "type": "FUNCTION_V2", "provider": "payment", "remark": "charge" }
    }
  ],
  "uiMapList": [
    { "source": "1", "target": "2" },
    { "source": "2", "target": "3" }
  ]
}`;
```

### 3.4 Update UI Help Text

**File:** `workflow-ui/src/components/ImportWorkflowModal.tsx`  
**Line 314**

**Current:**
```typescript
A valid <code>WorkFlow</code> JSON object with <code>pluginList</code> and <code>uiMapList</code> arrays — the same format the app saves. Plugin types: <code>FUNCTION_V2</code>, <code>FUNCTION_V3</code>, <code>HTTP_CALL</code>, <code>LOGIC</code>.
```

**Change to:**
```typescript
A valid <code>WorkFlow</code> JSON object with <code>pluginList</code> and <code>uiMapList</code> arrays — the same format the app saves. Plugin types: <code>CONSUMER</code>, <code>CONSUMERWITHOUTERROR</code>, <code>IFELSE</code>, <code>MESSAGE</code>, <code>FUNCTION_V2</code>, <code>FUNCTION_V3</code>.
```

---

## 4. Test Application Requirements

### 4.1 Six-Node Coverage Application

Create or update a test application named `test-all-node-types` with exactly 6 nodes, one of each type:

```json
{
  "pluginList": [
    {
      "id": 1,
      "description": "HTTP Consumer",
      "linkingIdOfRuleListAndAction": "rule-1",
      "ruleList": [{ "key": "$.data", "remark": "Data exists" }],
      "action": {
        "type": "CONSUMER",
        "provider": "http",
        "remark": "GET /api/data"
      },
      "uiMap": { "id": "node-1", "type": "CONSUMER", "position": { "x": 100, "y": 100 } }
    },
    {
      "id": 2,
      "description": "HTTP Consumer Without Error",
      "linkingIdOfRuleListAndAction": "rule-2",
      "ruleList": [{ "key": "$.retry", "remark": "Retry flag" }],
      "action": {
        "type": "CONSUMERWITHOUTERROR",
        "provider": "http",
        "remark": "POST /api/submit"
      },
      "uiMap": { "id": "node-2", "type": "CONSUMERWITHOUTERROR", "position": { "x": 100, "y": 200 } }
    },
    {
      "id": 3,
      "description": "Conditional Logic",
      "linkingIdOfRuleListAndAction": "rule-3",
      "ruleList": [{ "key": "$.amount", "remark": "Amount exists" }],
      "action": {
        "type": "IFELSE",
        "provider": "logic",
        "remark": "$.amount > 100"
      },
      "uiMap": { "id": "node-3", "type": "IFELSE", "position": { "x": 100, "y": 300 } }
    },
    {
      "id": 4,
      "description": "Send Message",
      "linkingIdOfRuleListAndAction": "rule-4",
      "ruleList": [{ "key": "$.notify", "remark": "Notify flag" }],
      "action": {
        "type": "MESSAGE",
        "provider": "notification",
        "remark": "Send email"
      },
      "uiMap": { "id": "node-4", "type": "MESSAGE", "position": { "x": 100, "y": 400 } }
    },
    {
      "id": 5,
      "description": "Function V2",
      "linkingIdOfRuleListAndAction": "rule-5",
      "ruleList": [{ "key": "$.process", "remark": "Process flag" }],
      "action": {
        "type": "FUNCTION_V2",
        "provider": "compute",
        "remark": "Calculate total"
      },
      "uiMap": { "id": "node-5", "type": "FUNCTION_V2", "position": { "x": 100, "y": 500 } }
    },
    {
      "id": 6,
      "description": "Function V3",
      "linkingIdOfRuleListAndAction": "rule-6",
      "ruleList": [{ "key": "$.finalize", "remark": "Finalize flag" }],
      "action": {
        "type": "FUNCTION_V3",
        "provider": "compute",
        "remark": "Finalize result"
      },
      "uiMap": { "id": "node-6", "type": "FUNCTION_V3", "position": { "x": 100, "y": 600 } }
    }
  ],
  "uiMapList": [
    { "id": "edge-1", "source": "node-1", "target": "node-2" },
    { "id": "edge-2", "source": "node-2", "target": "node-3" },
    { "id": "edge-3", "source": "node-3", "target": "node-4" },
    { "id": "edge-4", "source": "node-4", "target": "node-5" },
    { "id": "edge-5", "source": "node-5", "target": "node-6" }
  ]
}
```

### 4.2 Test Coverage

This application must be used for:
1. **Save workflow** — verify backend stores all 6 types correctly
2. **Load workflow** — verify canvas renders all 6 node types
3. **Node editor drawer** — verify each node type opens and displays correctly
4. **Export to JSON** — verify exported JSON contains correct wire-format types
5. **Import from JSON** — verify import validation accepts all 6 types (primary test case for this fix)

---

## 5. Implementation Steps

1. Update `VALID_PLUGIN_TYPES` constant in `ImportWorkflowModal.tsx` (line 47)
2. Update help text in `ImportWorkflowModal.tsx` (line 314)
3. Optionally update `EXAMPLE_JSON` to show multiple node types (lines 28-45)
4. Create or update test application `test-all-node-types` with 6-node coverage
5. Run local verification:
   - Export `test-all-node-types` workflow to JSON
   - Open Import modal
   - Paste exported JSON
   - Verify validation passes with "Valid workflow — 6 nodes, 5 edges"
6. Commit changes to `workflow-ui` on `main` branch
7. Push to origin
8. QA verification (see test-doc-v39.0.md)

---

## 6. Backward Compatibility

**No breaking changes:**
- Existing workflows with `FUNCTION_V2` and `FUNCTION_V3` continue to work
- `backendTypeToPlugin()` still accepts `FUNCTION` (v1) on read for backward compatibility
- Import validation now accepts **more** types, not fewer — purely additive

**Migration:** None required. This is a bug fix, not a feature change.

---

## 7. Alternative Approaches Considered

### Option A: Fetch valid types from backend API

**Pros:** Single source of truth, no hardcoded list  
**Cons:** Adds network dependency, complicates offline validation, requires new backend endpoint  
**Decision:** Rejected — overkill for a 6-item static list that rarely changes

### Option B: Reuse `pluginToBackendType()` logic

**Pros:** DRY principle, guaranteed alignment  
**Cons:** Requires refactoring mapper to export type list, adds coupling  
**Decision:** Rejected — simple constant update is sufficient

### Option C: Accept both UI enum names and wire format

**Pros:** More forgiving validation  
**Cons:** Allows invalid data that will fail on save, confuses users about correct format  
**Decision:** Rejected — validation should match actual storage format

---

## 8. Security Considerations

**No security impact:**
- Validation is client-side only (backend already validates on save)
- No new attack surface
- No changes to authentication, authorization, or data storage

---

## 9. Performance Considerations

**No performance impact:**
- Validation is synchronous, in-memory
- Array length increases from 4 to 6 items (negligible)
- No additional network requests

---

## 10. Rollback Plan

If issues arise after deployment:
1. Revert commit in `workflow-ui`
2. Push to origin
3. Vercel auto-deploys previous version
4. Investigate root cause before re-attempting

**Risk:** Very low — this is a pure validation fix with no backend changes.

---

*End of Architecture doc v39.0. Awaiting human approval before proceeding to implementation.*
