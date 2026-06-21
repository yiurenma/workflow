# Architecture Document — v36.0

**Document version:** 36.0  
**Date:** 2026-04-13  
**Status:** Draft  
**TODO item:** `TODO-node-editor-rule-key-json-path-validation`  
**Related PM doc:** pm-doc-v36.0.md

---

## 1. Technical Approach

Add client-side validation to rule key fields in the node editor using `jsonpath-plus` library (already in dependencies).

---

## 2. Validation Logic

### 2.1 Validation Function

```typescript
// utils/validateJsonPath.ts
import { JSONPath } from 'jsonpath-plus';

export function validateRuleKey(input: string): { valid: boolean; error?: string } {
  // Empty check
  if (!input || input.trim() === '') {
    return { valid: false, error: 'Rule key cannot be empty' };
  }

  // Check for multiple paths (comma/semicolon separated)
  if (input.includes(',') || input.includes(';')) {
    return { 
      valid: false, 
      error: 'Rule key must be a single valid JSONPath expression (e.g., $.data.amount)' 
    };
  }

  // Validate JSONPath syntax
  try {
    JSONPath({ path: input, json: {}, wrap: false });
    return { valid: true };
  } catch (error) {
    return { 
      valid: false, 
      error: 'Rule key must be a single valid JSONPath expression (e.g., $.data.amount)' 
    };
  }
}
```

---

## 3. Implementation Plan

### 3.1 Files to Modify

**New file:**
- `src/utils/validateJsonPath.ts` — Validation utility

**Modified files:**
- `src/routes/workflows/-components/workflow-drawer/forms/HttpCallForm.tsx` — Add validation to rule key inputs
- `src/routes/workflows/-components/workflow-drawer/forms/LogicForm.tsx` — Add validation to rule key inputs

---

### 3.2 Form Integration

Add validation state and error display to rule key inputs:

```typescript
// In HttpCallForm.tsx and LogicForm.tsx
const [ruleKeyErrors, setRuleKeyErrors] = useState<Record<number, string>>({});

const handleRuleKeyBlur = (index: number, value: string) => {
  const result = validateRuleKey(value);
  if (!result.valid) {
    setRuleKeyErrors(prev => ({ ...prev, [index]: result.error! }));
  } else {
    setRuleKeyErrors(prev => {
      const next = { ...prev };
      delete next[index];
      return next;
    });
  }
};

// In rule key Input component
<Input
  value={rule.key}
  onChange={(e) => handleRuleKeyChange(index, e.target.value)}
  onBlur={(e) => handleRuleKeyBlur(index, e.target.value)}
  status={ruleKeyErrors[index] ? 'error' : undefined}
  placeholder="$.data.amount"
/>
{ruleKeyErrors[index] && (
  <div className="text-xs mt-1" style={{ color: '#da1e28' }}>
    {ruleKeyErrors[index]}
  </div>
)}
```

---

### 3.3 Save Button Logic

Disable save when validation errors exist:

```typescript
const hasValidationErrors = Object.keys(ruleKeyErrors).length > 0;

<Button
  type="primary"
  onClick={handleSave}
  disabled={hasValidationErrors || isSaving}
>
  Save
</Button>
```

---

## 4. Success Criteria

- Rule key validation runs on blur
- Invalid JSONPath shows clear error message
- Save button disabled when errors exist
- Validation uses existing `jsonpath-plus` library
- No backend changes required

---

**End of Architecture Document v36.0**
