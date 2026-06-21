# Arch Doc v23.0 — Canvas AI: Workflow Generator

**Label:** `TODO-canvas-ai-workflow-json-copilot-replace`
**Date:** 2026-04-08

---

## Scope

| File | Action |
|------|--------|
| `workflow-header/WorkflowGeneratorModal.tsx` | New component |
| `workflow-header/index.tsx` | Add "Generate" button + state + `<WorkflowGeneratorModal>` |

No backend changes. No new packages (reuses existing `callAI` and `jsonpath-plus` already installed).

---

## System Prompt Constant

Named constant `WORKFLOW_GENERATOR_SYSTEM_PROMPT` in `WorkflowGeneratorModal.tsx`:

```
You are a workflow engine configuration expert.
This platform routes business events through a plugin pipeline.
Each plugin has: a description, a list of JSONPath rules (all must match to execute),
and an action (HTTP fetch, condition/logic, message dispatch, or consumer).

Given the user's requirements, output a SINGLE valid JSON object — no markdown,
no commentary, no code fences — matching this exact shape:
{
  "pluginList": [
    {
      "id": <integer>,
      "description": "<step name>",
      "linkingIdOfRuleListAndAction": "",
      "ruleList": [{ "key": "<$.jsonpath>", "remark": "<plain English>" }],
      "action": { "provider": "SYSTEM", "type": "<CONSUMER|FUNCTION_V2|FUNCTION_V3|IFELSE|MESSAGE>", "remark": "" },
      "uiMap": { "id": "<unique string>", "type": "<same type>", "position": { "x": <number>, "y": <number> } }
    }
  ],
  "uiMapList": []
}
Rules:
- uiMap.id must be unique per plugin
- Lay nodes in a vertical line: x=300, y=i*120 (i = 0-based index)
- uiMapList edges are empty [] — user will wire connections manually
- Output JSON only. No explanation. No markdown fences.
```

---

## API Call

Reuse `callAI` with these changes:
- **Messages:** `[{ role: "system", content: WORKFLOW_GENERATOR_SYSTEM_PROMPT }, { role: "user", content: userMessage }]`
- **`max_tokens`: 4096** (not 1024)
- `userMessage` = `"Application: ${appName}\n\nRequirements:\n${requirements}\n\nCurrent workflow (optional context):\n${currentJson}"`

For Anthropic API: system prompt goes in the top-level `"system"` field (not in messages array) per Anthropic spec.

---

## Response Validation

```ts
function validateWorkFlow(raw: unknown): WorkFlow {
  if (typeof raw !== 'object' || raw === null) throw new Error('Not an object');
  const wf = raw as WorkFlow;
  if (!Array.isArray(wf.pluginList)) throw new Error('Missing pluginList');
  for (const p of wf.pluginList) {
    const ui = p.uiMap as { id?: unknown; position?: unknown } | undefined;
    if (!ui || typeof ui.id !== 'string') throw new Error(`Plugin missing uiMap.id`);
    if (!ui.position) throw new Error(`Plugin missing uiMap.position`);
  }
  return wf;
}
```

Strip markdown fences before parsing:
```ts
const stripped = responseText.replace(/^```(?:json)?\n?/m, '').replace(/\n?```$/m, '').trim();
const parsed = JSON.parse(stripped);
```

---

## UI Flow

1. User clicks **Generate** → modal opens with requirements textarea + optional current JSON (pre-filled, editable)
2. User clicks **Generate** button in modal → loading spinner
3. On success → **preview step**: "Generated N nodes. Apply to canvas?" with a summary list
4. On confirm → `setNodes(mapped.nodes); setEdges(mapped.edges); fitView()`
5. On any error → clear error message; canvas unchanged; offer Retry

---

## Error Cases

| Error | User message |
|-------|-------------|
| Invalid JSON / parse error | "AI returned invalid JSON. Try simpler requirements." |
| Validation failure (missing uiMap.id etc.) | "Generated workflow has invalid structure. Try again." |
| API 4xx/5xx | "API error: \<status\>. Check token or try later." |
| Empty / truncated response | "Response was empty or incomplete. Try fewer steps." |
| User cancels confirm | Canvas unchanged |

---

## Risk

Medium. Replacing canvas content is destructive if unsaved changes exist — the confirm dialog must make this clear. Recommend adding "unsaved changes" warning if `nodes.length > 0` before applying.
