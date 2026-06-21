# Arch Doc — Node Editor Cross-Audience Clarity

**Label**: `TODO-node-editor-cross-audience-clarity`  
**Version**: 4.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc v4.0 (US-31)

---

## 1. Scope

Pure frontend change to `workflow-ui` — `workflow-drawer/` subtree only.  
No backend, no API, no env changes.

---

## 2. Changes to `NodeSection`

Add two optional props:

```typescript
type NodeSectionProps = {
  title: string;
  children: React.ReactNode;
  variant?: "default" | "inset";
  subtitle?: string;          // plain-English one-liner for business users
  headingTooltip?: string;    // persisted field map for developers
};
```

Render:

```tsx
<div className={...}>
  <div className="flex items-center gap-1 mb-1">
    <p className="text-[10px] font-semibold text-zinc-500 uppercase tracking-widest">
      {title}
    </p>
    {headingTooltip && (
      <Tooltip title={headingTooltip}>
        <InfoCircleOutlined className="text-[10px] text-zinc-300 cursor-help" />
      </Tooltip>
    )}
  </div>
  {subtitle && (
    <p className="text-xs text-zinc-400 mb-3">{subtitle}</p>
  )}
  {children}
</div>
```

Both `subtitle` and `headingTooltip` are optional — no-op when not provided, so existing usages need no change.

---

## 3. Changes to `HttpCallForm` and `LogicForm`

Pass subtitle and headingTooltip for all three panels:

```tsx
<NodeSection
  title="Node Description"
  subtitle="The name shown on the canvas. What is this step called?"
  headingTooltip="Persisted as: plugin.description"
>

<NodeSection
  title="Rules"
  subtitle="Run only when… — conditions that must all match before this step executes."
  headingTooltip="Persisted as: plugin.ruleList[].key + ruleList[].remark"
>

<NodeSection
  title="Action"
  variant="inset"
  subtitle="What the system does when this step runs."
  headingTooltip="Persisted as: plugin.action (type, provider, httpRequest*, elseLogic, …)"
>
```

---

## 4. Affected Files

| File | Change |
|---|---|
| `workflow-drawer/NodeSection.tsx` | Add `subtitle`, `headingTooltip` props; render subtitle + tooltip icon |
| `workflow-drawer/forms/HttpCallForm.tsx` | Pass subtitle + headingTooltip to all 3 NodeSection calls |
| `workflow-drawer/forms/LogicForm.tsx` | Pass subtitle + headingTooltip to all 3 NodeSection calls |

No other files touched.

---

## 5. Security / Trade-offs

| Concern | Decision |
|---|---|
| Tooltip content hardcoded | Acceptable — field names are stable; not user-facing data |
| Ant Design `Tooltip` + `InfoCircleOutlined` already in use in project | No new dependencies |
| Subtitle adds ~1 line of text per panel | Acceptable visual overhead; clearly secondary styling |

---

*End of Arch Doc v4.0 — Draft, awaiting approval.*
