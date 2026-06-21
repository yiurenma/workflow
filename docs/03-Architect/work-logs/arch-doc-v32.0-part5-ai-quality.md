# Architecture — Workflow Studio Unified Program Part 5: AI Quality (v32.0)

**Document version:** 32.0 Part 5  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** AI workflow generator semantic quality improvements  
**Repository:** `workflow-ui`

---

## 1. Technical Context

### 1.1 Current Generator Implementation

**Baseline:** v23.0 (`TODO-canvas-ai-workflow-json-copilot-replace`)

**Files:**
- `workflow-ui/src/components/WorkflowGeneratorModal.tsx` — Generator UI
- `workflow-ui/src/constants/prompts.ts` — `WORKFLOW_GENERATOR_SYSTEM_PROMPT`

**Current behavior:**
- User enters natural language description
- System prompt + user description sent to AI API (GitHub Models or Anthropic)
- Response parsed as JSON, loaded onto canvas
- **Gap:** Output often syntactically valid but semantically incorrect

---

## 2. Problem Analysis

### 2.1 Common Semantic Errors

**Category A: Invalid Plugin IDs**
- Generated: `"pluginId": "http_call"`
- Actual: `"pluginId": "http_fetch"`
- **Root cause:** Model hallucinates plugin names not in registry

**Category B: Incorrect Action Kinds**
- Generated: `"action": { "kind": "fetch", ... }`
- Actual: `"action": { "kind": "http", ... }`
- **Root cause:** Model invents action kinds not supported by plugin

**Category C: Invalid Rule Keys (JSONPath)**
- Generated: `"key": "data.userId"` (missing `$` prefix)
- Generated: `"key": "$.data.userId, $.data.userName"` (comma-separated)
- **Root cause:** Model doesn't understand JSONPath syntax constraints

**Category D: Wrong Rule Types**
- Generated: `"type": "string"` for numeric comparison
- Actual: `"type": "number"`
- **Root cause:** Model doesn't infer type from rule value

**Category E: Broken Edges**
- Generated: `"edges": [{ "source": "node1", "target": "node99" }]`
- Actual: `node99` doesn't exist in nodes array
- **Root cause:** Model loses track of node IDs during generation

---

## 3. Solution Design

### 3.1 Measurement Loop (UAT)

**Test application:** `E2E_TEST_CANVAS` on `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`

**Process:**
1. Open reference workflow (known-good configuration)
2. Click **Explain** → get AI-generated description
3. Copy description
4. Click **Generate** → paste description → generate workflow
5. Compare generated workflow to reference:
   - Open each node in drawer
   - Inspect plugin IDs, action kinds, rule keys, rule types
   - Check edges point to valid nodes
6. Document failures:
   - Screenshot of generated canvas
   - JSON diff (reference vs generated)
   - Categorize error (A/B/C/D/E above)

**Deliverable:** `docs/ai-generator-failure-patterns-v32.0.md` with:
- 5-10 test cases (simple → complex workflows)
- Failure rate per category
- Representative JSON snippets

---

### 3.2 Improved System Prompt

**Current prompt (v23.0):**
```
You are a workflow generator. Generate a valid workflow JSON...
```

**Enhanced prompt (v32.0):**
```typescript
export const WORKFLOW_GENERATOR_SYSTEM_PROMPT = `
You are a workflow generator for Workflow Studio. Generate a valid workflow JSON that matches the user's description.

## Valid Plugin IDs (ONLY use these):
- "http_fetch" — performs HTTP request
- "safe_http_fetch" — HTTP request that doesn't fail workflow on error
- "logic" — conditional routing based on rules
- "transform" — data transformation
[... complete list from plugin registry]

## Valid Action Kinds by Plugin:
- http_fetch: { "kind": "http", "url": "...", "method": "GET|POST|PUT|DELETE", "headers": {...}, "body": "..." }
- safe_http_fetch: { "kind": "safe_http", "url": "...", "method": "...", ... }
- logic: { "kind": "logic", "condition": "..." }
[... complete list]

## Rule Key Format (JSONPath):
- MUST start with "$" (e.g., "$.data.userId")
- MUST be a single expression (NO comma-separated paths)
- Valid examples: "$.data.userId", "$.items[0].name", "$..price"
- Invalid examples: "data.userId" (missing $), "$.data.userId, $.data.userName" (comma)

## Rule Types:
- "string" — for text comparisons
- "number" — for numeric comparisons
- "boolean" — for true/false
- "array" — for list operations

## Edge Format:
- MUST reference node IDs that exist in nodes array
- Example: { "source": "node_1", "target": "node_2" }

## Example Valid Workflow:
\`\`\`json
{
  "nodes": [
    {
      "id": "node_1",
      "type": "http_fetch",
      "position": { "x": 100, "y": 100 },
      "data": {
        "description": "Fetch user data",
        "ruleList": [
          {
            "key": "$.userId",
            "type": "string",
            "value": "123",
            "operator": "equals"
          }
        ],
        "action": {
          "kind": "http",
          "url": "https://api.example.com/users",
          "method": "GET"
        }
      }
    }
  ],
  "edges": []
}
\`\`\`

Generate ONLY valid JSON. Do not include markdown fences or explanations.
`;
```

---

### 3.3 Post-Parse Validation

**Implementation:**

```typescript
// validateGeneratedWorkflow.ts
interface ValidationError {
  field: string;
  message: string;
  severity: 'error' | 'warning';
}

export function validateGeneratedWorkflow(workflow: any): ValidationError[] {
  const errors: ValidationError[] = [];

  // Validate plugin IDs
  const validPluginIds = ['http_fetch', 'safe_http_fetch', 'logic', 'transform'];
  workflow.nodes?.forEach((node: any, idx: number) => {
    if (!validPluginIds.includes(node.type)) {
      errors.push({
        field: `nodes[${idx}].type`,
        message: `Invalid plugin ID "${node.type}". Valid IDs: ${validPluginIds.join(', ')}`,
        severity: 'error',
      });
    }
  });

  // Validate action kinds
  workflow.nodes?.forEach((node: any, idx: number) => {
    const action = node.data?.action;
    if (action) {
      const validKinds = getValidActionKindsForPlugin(node.type);
      if (!validKinds.includes(action.kind)) {
        errors.push({
          field: `nodes[${idx}].data.action.kind`,
          message: `Invalid action kind "${action.kind}" for plugin "${node.type}". Valid kinds: ${validKinds.join(', ')}`,
          severity: 'error',
        });
      }
    }
  });

  // Validate rule keys (JSONPath)
  workflow.nodes?.forEach((node: any, idx: number) => {
    node.data?.ruleList?.forEach((rule: any, ruleIdx: number) => {
      if (rule.key) {
        if (!rule.key.startsWith('$')) {
          errors.push({
            field: `nodes[${idx}].data.ruleList[${ruleIdx}].key`,
            message: `Rule key must start with "$" (JSONPath syntax). Got: "${rule.key}"`,
            severity: 'error',
          });
        }
        if (rule.key.includes(',')) {
          errors.push({
            field: `nodes[${idx}].data.ruleList[${ruleIdx}].key`,
            message: `Rule key must be a single JSONPath expression (no commas). Got: "${rule.key}"`,
            severity: 'error',
          });
        }
      }
    });
  });

  // Validate edges reference valid nodes
  const nodeIds = new Set(workflow.nodes?.map((n: any) => n.id) || []);
  workflow.edges?.forEach((edge: any, idx: number) => {
    if (!nodeIds.has(edge.source)) {
      errors.push({
        field: `edges[${idx}].source`,
        message: `Edge source "${edge.source}" does not exist in nodes array`,
        severity: 'error',
      });
    }
    if (!nodeIds.has(edge.target)) {
      errors.push({
        field: `edges[${idx}].target`,
        message: `Edge target "${edge.target}" does not exist in nodes array`,
        severity: 'error',
      });
    }
  });

  return errors;
}
```

**Integration in WorkflowGeneratorModal:**

```tsx
const handleGenerate = async () => {
  const response = await callAI(prompt);
  const workflow = JSON.parse(response);

  // Validate
  const errors = validateGeneratedWorkflow(workflow);

  if (errors.length > 0) {
    setValidationErrors(errors);
    // Show errors to user; let them fix or regenerate
  } else {
    // Load onto canvas
    loadWorkflow(workflow);
  }
};

// UI for validation errors
{validationErrors.length > 0 && (
  <Alert
    type="error"
    message="Generated workflow has validation errors"
    description={
      <ul>
        {validationErrors.map((err, idx) => (
          <li key={idx}>
            <strong>{err.field}:</strong> {err.message}
          </li>
        ))}
      </ul>
    }
    action={
      <Button onClick={handleRegenerate}>Regenerate</Button>
    }
  />
)}
```

---

### 3.4 Playwright Extension (Optional)

**Current tests (v23.0):**
- TC-GENERATOR-01: Modal opens
- TC-GENERATOR-02: Generate button clickable

**Enhanced test (v32.0):**

```typescript
test('TC-GENERATOR-03: Generated workflow structure valid', async ({ page }) => {
  await page.goto('/workflows/E2E_TEST_CANVAS');

  // Get reference workflow via Explain
  await page.locator('button:has-text("Explain")').click();
  const explanation = await page.locator('.explain-result').textContent();
  await page.locator('.ant-modal-close').click();

  // Generate workflow from explanation
  await page.locator('button:has-text("Generate")').click();
  await page.locator('textarea').fill(explanation);
  await page.locator('button:has-text("Generate")').click();

  // Wait for generation
  await page.waitForTimeout(5000);

  // Validate structure (not exact match, but valid)
  const nodes = await page.locator('.react-flow__node').count();
  expect(nodes).toBeGreaterThan(0);

  // Open first node; verify plugin ID valid
  await page.locator('.react-flow__node').first().click();
  const drawer = page.locator('.ant-drawer');
  await expect(drawer).toBeVisible();

  // Check plugin ID in drawer (if displayed)
  // Or: export workflow JSON and validate programmatically
});
```

---

## 4. Implementation Plan

### Phase 1: Measurement Loop (2-3 hours)

1. Set up test cases in `E2E_TEST_CANVAS`
2. Run Explain → Generate cycle for each test case
3. Document failure patterns in `docs/ai-generator-failure-patterns-v32.0.md`
4. Categorize errors (A/B/C/D/E)

---

### Phase 2: Enhanced System Prompt (2-3 hours)

1. Enumerate valid plugin IDs from plugin registry
2. Enumerate valid action kinds per plugin
3. Add JSONPath syntax rules
4. Add canonical examples (simple, medium, complex)
5. Update `WORKFLOW_GENERATOR_SYSTEM_PROMPT` constant
6. Test: run measurement loop again; compare failure rates

---

### Phase 3: Post-Parse Validation (3-4 hours)

1. Implement `validateGeneratedWorkflow()` function
2. Integrate in `WorkflowGeneratorModal`
3. Add UI for validation errors
4. Add "Regenerate" button
5. Test: trigger validation errors; verify user can see and fix

---

### Phase 4: Playwright Extension (2-3 hours, optional)

1. Implement TC-GENERATOR-03 (structure validation)
2. Run full E2E suite; verify no regressions
3. Document results in `e2e-test-report-v32.0-part5.md`

---

## 5. Testing Strategy

### 5.1 Manual Testing

- Run measurement loop (Explain → Generate) for 5-10 test cases
- Verify enhanced prompt reduces failure rate
- Verify validation catches common errors
- Verify user can regenerate after validation failure

### 5.2 Playwright Testing

- Optional: TC-GENERATOR-03 validates generated workflow structure
- Verify existing TC-GENERATOR-01/02 still pass

---

## 6. Risks & Mitigations

**Risk:** Enhanced prompt too long; exceeds model context limit.  
**Mitigation:** Keep prompt under 2000 tokens; prioritize most common errors.

**Risk:** Validation too strict; rejects valid edge cases.  
**Mitigation:** Use warnings for ambiguous cases; only block on clear errors.

**Risk:** Model still produces semantic errors despite better prompt.  
**Mitigation:** Validation catches errors; user can regenerate or fix manually.

---

## 7. Deployment

- **Branch:** `main` (workflow-ui)
- **Merge strategy:** One commit per phase for easier rollback
- **Rollback plan:** Revert to v23.0 prompt if quality degrades

---

## 8. References

- `pm-doc-v23.0.md` — v23.0 generator baseline
- `arch-doc-v23.0.md` — v23.0 technical design
- `docs/e2e-testing-guide.md` — E2E conventions
- JSONPath spec: https://goessner.net/articles/JsonPath/
