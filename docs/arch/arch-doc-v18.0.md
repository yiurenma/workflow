# Arch Doc v18.0 — Canvas Mobile Header: Save button inaccessible (overflow)

**Label:** `TODO-canvas-mobile-header-save-overflow`
**Date:** 2026-04-08

---

## Scope

Single file change: `workflow-ui/src/routes/workflows/-components/workflow-header/index.tsx`

---

## Current Problem

```tsx
<Space size="small">
  <Button>Explain</Button>    {/* added earlier */}
  <Button>JsonPath</Button>   {/* added v16.0 — caused overflow */}
  <Button>Run</Button>
  <Button type="primary">Save</Button>
</Space>
```

On 390 px, all four buttons + the app name + back arrow overflow.

---

## Fix: Responsive toolbar using `useIsMobile`

The component already imports `useIsMobile` (indirectly via the route — actually `WorkflowHeader` doesn't import it yet, but `$applicationName.tsx` does). Add the import and split rendering:

### Desktop (≥ 768 px) — unchanged
```tsx
<Space size="small">
  <Button icon={<BulbOutlined />} size="small" onClick={explainFlow} ...>Explain</Button>
  <Button size="small" onClick={() => setJsonPathOpen(true)} ...>JsonPath</Button>
  <Button size="small" onClick={runFlow} ...>Run</Button>
  <Button size="small" type="primary" onClick={saveFlow} ...>Save</Button>
</Space>
```

### Mobile (< 768 px) — overflow dropdown
```tsx
<Space size="small">
  <Dropdown
    menu={{
      items: [
        { key: 'explain', label: 'Explain', icon: <BulbOutlined />, onClick: explainFlow, disabled: isLoading },
        { key: 'jsonpath', label: 'JsonPath', onClick: () => setJsonPathOpen(true) },
        { key: 'run', label: 'Run', onClick: runFlow, disabled: isLoading },
      ]
    }}
    trigger={['click']}
  >
    <Button size="small" icon={<EllipsisOutlined />} />
  </Dropdown>
  <Button size="small" type="primary" onClick={saveFlow} disabled={isLoading || saveWorkflow.isPending} loading={saveWorkflow.isPending}>
    Save
  </Button>
</Space>
```

### New imports needed

```ts
import { EllipsisOutlined } from "@ant-design/icons";
import { Dropdown } from "antd";
import { useIsMobile } from "@/hooks/useIsMobile";
```

`Dropdown` is already in `antd` — no new package required.

---

## Diff Summary

| File | Change |
|------|--------|
| `workflow-header/index.tsx` | Add `useIsMobile`, `Dropdown`, `EllipsisOutlined`; split toolbar into mobile/desktop branches |

---

## Risk

Low. The change only alters button layout on mobile; desktop is untouched. All actions remain accessible.
