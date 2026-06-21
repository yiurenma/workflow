# Arch Doc — Artboard Node Editor: Three-Panel Layout + Smart Text Formatting

**Label**: `TODO-artboard-node-editor-3panel-json-format`  
**Version**: 3.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc v3.0 (US-30)

---

## 1. Scope

This document covers the architectural approach for US-30: reworking the node editor drawer in `workflow-ui` to use a three-panel layout and adding smart JSON formatting on blur for JSON-bearing text fields.

**Affected components**: `workflow-ui` only — `workflow-drawer/` subtree.  
**No backend changes** — layout and formatting are purely frontend.

---

## 2. Current State Analysis

### 2.1 Existing structure

```
workflow-drawer/
  index.tsx            ← Drawer wrapper, routes to HttpCallForm or LogicForm
  forms/
    HttpCallForm.tsx   ← HTTP-call nodes (CONSUMER, MESSAGE, etc.)
    LogicForm.tsx      ← Logic nodes (IF_ELSE, FUNCTION, FUNCTION_V3)
```

Both forms already have an implicit three-section structure in their content, but sections are **not visually distinct**:

- `description` field — top, unlabelled as a section
- `ruleList` block — preceded by an all-caps label `"Trigger Rules"` (inconsistent styling)
- HTTP/Logic config block — inside a `border-zinc-100 bg-zinc-50` box labelled `"HTTP Configuration"` / `"Logic Configuration"`

The three panels exist conceptually but the first two sections lack the visual weight of the third, and the section labels use different styles.

### 2.2 JSON fields (targets for smart formatting)

| Field name | Form | Content type |
|---|---|---|
| `httpRequestHeaders` | HttpCallForm | JSON object |
| `httpRequestBody` | HttpCallForm | JSON template (may use `<<<$.path>>>` variables) |
| `trackingNumberSchemaInHttpResponse` | HttpCallForm | JSON template |
| `elseLogic` | LogicForm | JSON object or FunctionObject |

---

## 3. Component Architecture

### 3.1 New shared component: `NodeSection`

Extract a shared `NodeSection` wrapper component to enforce consistent panel styling across both forms:

```typescript
// workflow-drawer/NodeSection.tsx
type NodeSectionProps = {
  title: string;
  children: React.ReactNode;
  variant?: "default" | "inset"; // "inset" = shaded background for Action panel
};

const NodeSection: React.FC<NodeSectionProps> = ({ title, children, variant = "default" }) => (
  <div className={cn(
    "mb-4",
    variant === "inset" && "border border-zinc-100 rounded-lg p-3 bg-zinc-50"
  )}>
    <p className="text-[10px] font-semibold text-zinc-500 uppercase tracking-widest mb-3">
      {title}
    </p>
    {children}
  </div>
);
```

Usage in `HttpCallForm` and `LogicForm`:

```tsx
<NodeSection title="Node Description">
  <Form.Item name="description" ...>...</Form.Item>
</NodeSection>

<NodeSection title="Rules">
  {/* Form.List ruleList */}
</NodeSection>

<NodeSection title="Action" variant="inset">
  {/* HTTP or Logic config fields */}
</NodeSection>
```

### 3.2 New hook: `useJsonFormat`

Encapsulate the blur-triggered JSON formatting logic in a hook to keep form components clean:

```typescript
// workflow-drawer/useJsonFormat.ts

/**
 * Returns an onBlur handler that pretty-prints the field value
 * if it is valid JSON, otherwise leaves it unchanged.
 * Calls form.setFieldValue + onValuesChange to keep state consistent.
 */
function useJsonFormat(
  form: FormInstance,
  onValuesChange?: (values: unknown) => void
): (fieldName: string) => React.FocusEventHandler<HTMLTextAreaElement> {
  return (fieldName) => (e) => {
    const raw = e.target.value;
    if (!raw.trim()) return;
    try {
      const parsed = JSON.parse(raw);
      const formatted = JSON.stringify(parsed, null, 2);
      if (formatted !== raw) {
        form.setFieldValue(fieldName, formatted);
        // Propagate change so canvas receives updated value
        onValuesChange?.(form.getFieldsValue());
      }
    } catch {
      // Not valid JSON — leave unchanged, no user feedback
    }
  };
}
```

Applied on each JSON `TextArea`:

```tsx
const formatOnBlur = useJsonFormat(form, onValuesChange);

<Input.TextArea
  rows={3}
  onBlur={formatOnBlur("httpRequestHeaders")}
  ...
/>
```

### 3.3 Data flow

```
User edits JSON field → types → leaves field (blur event)
  │
  └─ useJsonFormat handler
       ├─ try JSON.parse(value)
       │    ├─ success → JSON.stringify(parsed, null, 2) → form.setFieldValue → onValuesChange
       │    └─ fail   → no-op (value unchanged)
       └─ (no UI feedback — silent)
```

`onValuesChange` propagates to `WorkflowDrawer` → `useWorkflowForm` → canvas node data, so the formatted string is stored and eventually saved to the backend.

---

## 4. Security Analysis

| Concern | Detail | Decision |
|---|---|---|
| JSON parsing of user input | `JSON.parse` on user-entered text — safe; no code execution; purely data | Accepted |
| No `eval` | `JSON.parse` is not `eval`; cannot execute arbitrary code | By design |
| Template strings (`<<<$.path>>>`) | `JSON.parse` will fail on fields containing `<<<` variables → leaves unchanged → no corruption | Handled by try/catch |
| XSS in formatted output | Formatted output goes into a controlled textarea value prop, not `dangerouslySetInnerHTML` | Not applicable |

---

## 5. Implementation Plan

### 5.1 New files

| File | Purpose |
|---|---|
| `workflow-ui/src/routes/workflows/-components/workflow-drawer/NodeSection.tsx` | Shared panel wrapper component |
| `workflow-ui/src/routes/workflows/-components/workflow-drawer/useJsonFormat.ts` | Blur-triggered JSON formatting hook |

### 5.2 Changed files

| File | Changes |
|---|---|
| `workflow-drawer/forms/HttpCallForm.tsx` | Wrap sections in `NodeSection`; add `onBlur={formatOnBlur(...)}` to JSON fields |
| `workflow-drawer/forms/LogicForm.tsx` | Wrap sections in `NodeSection`; add `onBlur={formatOnBlur("elseLogic")}` |

### 5.3 No changes to

- `workflow-drawer/index.tsx` — Drawer wrapper unchanged
- Any backend repo — no changes
- `workflow-header/index.tsx` — not affected

---

## 6. Trade-offs

| Trade-off | Rationale |
|---|---|
| Blur-triggered vs. real-time formatting | Blur is less disruptive — does not interrupt typing. Real-time formatting would re-format on every keystroke, which is jarring for partial input. |
| Silent formatting (no toast) | JSON formatting is a background enhancement, not a user-visible action. Notifying the user on every blur would be noisy. |
| No XML/YAML formatting | JSON is the only structured format used by the backend. Adding more parsers increases complexity for minimal gain. |
| `NodeSection` component vs. inline styling | A shared component enforces visual consistency and reduces the risk of drift between `HttpCallForm` and `LogicForm`. |
| Template strings left unchanged | `<<<$.path>>>` syntax causes `JSON.parse` to throw → try/catch leaves the value as-is → no user data loss. |

---

## 7. Architectural Decisions

| # | Decision | Resolution |
|---|---|---|
| AD-30-1 | Blur vs. keystroke formatting | **Blur** — less disruptive, does not interfere with partial input |
| AD-30-2 | Shared component vs. inline | **`NodeSection` shared component** — enforces consistency between both forms |
| AD-30-3 | Hook vs. inline handler | **`useJsonFormat` hook** — keeps form components clean; testable in isolation |
| AD-30-4 | Feedback on formatting | **Silent** — no toast or indicator; formatting is a background quality-of-life improvement |
| AD-30-5 | Scope of JSON fields | **Named fields only** (`httpRequestHeaders`, `httpRequestBody`, `trackingNumberSchemaInHttpResponse`, `elseLogic`) — not applied to URL fields or description |

---

*End of Arch Doc v3.0 — Artboard node editor 3-panel + JSON format — Draft, awaiting approval.*
