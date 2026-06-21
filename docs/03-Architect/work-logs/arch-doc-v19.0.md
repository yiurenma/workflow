# Arch Doc v19.0 — Canvas: Straight-Line Layout Recovery

**Label:** `TODO-canvas-straight-line-workflow-recovery`
**Date:** 2026-04-08

---

## Scope

| File | Action |
|------|--------|
| `workflow-ui/src/routes/workflows/-components/worflow-canvas/index.tsx` | Add `StraightenButton` component rendered inside `<ReactFlow>` |

No backend changes. No new packages.

---

## Layout Algorithm

```ts
const VERTICAL_SPACING = 120; // px between node centres
const X_CENTER = 300;         // fixed X for all nodes

function straightenNodes(nodes: Node[]): Node[] {
  // Sort by current Y position to preserve rough top-to-bottom order
  const sorted = [...nodes].sort((a, b) => a.position.y - b.position.y);
  return sorted.map((node, i) => ({
    ...node,
    position: { x: X_CENTER, y: i * VERTICAL_SPACING },
  }));
}
```

---

## Button Placement

Place a custom panel inside `<ReactFlow>` using the `<Panel>` component from `@xyflow/react`:

```tsx
import { Panel, useReactFlow } from '@xyflow/react';

function StraightenButton({ setNodes }: { setNodes: (fn: (nds: Node[]) => Node[]) => void }) {
  const { fitView } = useReactFlow();
  const handleClick = () => {
    setNodes((nds) => straightenNodes(nds));
    setTimeout(() => fitView({ duration: 400 }), 50);
  };
  return (
    <Panel position="top-right">
      <Button size="small" onClick={handleClick} title="Straighten layout">
        ⇕ Straighten
      </Button>
    </Panel>
  );
}
```

Rendered inside `WorkflowEditor` (both desktop and mobile):
```tsx
<ReactFlow ...>
  <Controls />
  <StraightenButton setNodes={setNodes} />
  ...
</ReactFlow>
```

---

## Edge Preservation

Edges are not changed — they reference nodes by ID, so they auto-reroute after repositioning.

---

## Risk

Low. Purely in-memory; user must still click Save to persist positions. No backend interaction.
