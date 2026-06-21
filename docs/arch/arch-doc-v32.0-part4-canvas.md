# Architecture — Workflow Studio Unified Program Part 4: Canvas Enhancements (v32.0)

**Document version:** 32.0 Part 4  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Canvas node palette drag-and-drop + descriptions + node copy/delete  
**Repository:** `workflow-ui`

---

## 1. Technical Context

### 1.1 Current Canvas Implementation

**Files:**
- `workflow-ui/src/routes/workflows/[name]/index.tsx` — Main canvas component
- `workflow-ui/src/components/NodePalette.tsx` (or equivalent) — Left palette
- `workflow-ui/src/components/WorkflowDrawer.tsx` — Node editor drawer
- `workflow-ui/src/hooks/useWorkflowState.ts` — Canvas state management

**Current behavior:**
- Click palette item → node added at default position
- No drag-and-drop from palette
- No descriptions on palette items
- No copy/delete actions in drawer or context menu

---

## 2. Solution Design

### 2.1 Drag-and-Drop from Palette

**Approach:** Use ReactFlow's built-in drag-and-drop API.

**Implementation:**

```tsx
// NodePalette.tsx
import { useDrag } from 'react-dnd';

const PaletteItem = ({ nodeType, title, description }) => {
  const onDragStart = (event: React.DragEvent) => {
    event.dataTransfer.setData('application/reactflow', nodeType);
    event.dataTransfer.effectAllowed = 'move';
  };

  return (
    <div
      draggable
      onDragStart={onDragStart}
      className="palette-item cursor-grab active:cursor-grabbing"
    >
      <div className="font-semibold">{title}</div>
      <div className="text-sm text-gray-600">{description}</div>
    </div>
  );
};
```

**Canvas drop handler:**

```tsx
// Canvas component
import { useCallback } from 'react';
import { useReactFlow } from 'reactflow';

const Canvas = () => {
  const reactFlowInstance = useReactFlow();
  const [nodes, setNodes] = useState([]);

  const onDrop = useCallback(
    (event: React.DragEvent) => {
      event.preventDefault();

      const nodeType = event.dataTransfer.getData('application/reactflow');
      if (!nodeType) return;

      // Get drop position relative to canvas
      const position = reactFlowInstance.screenToFlowPosition({
        x: event.clientX,
        y: event.clientY,
      });

      // Create new node
      const newNode = {
        id: `node_${Date.now()}`,
        type: nodeType,
        position,
        data: {
          description: '',
          ruleList: [],
          action: getDefaultActionForType(nodeType),
        },
      };

      setNodes((nds) => [...nds, newNode]);
    },
    [reactFlowInstance]
  );

  const onDragOver = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, []);

  return (
    <ReactFlow
      nodes={nodes}
      onDrop={onDrop}
      onDragOver={onDragOver}
      // ... other props
    />
  );
};
```

---

### 2.2 Node Type Descriptions

**Data structure:**

```typescript
// nodeTypes.ts
export const NODE_TYPES = [
  {
    id: 'http_fetch',
    title: 'HTTP fetch',
    description: 'Performs an HTTP request and brings response data into the workflow',
    tooltip: 'Supports GET, POST, PUT, DELETE methods with headers and body',
    icon: '🌐',
  },
  {
    id: 'safe_http_fetch',
    title: 'Safe HTTP fetch',
    description: 'Same as HTTP fetch, but HTTP failure does not fail the entire workflow',
    tooltip: 'Use when you want to continue workflow execution even if the HTTP request fails',
    icon: '🛡️',
  },
  {
    id: 'logic',
    title: 'Logic',
    description: 'Evaluates conditional rules and routes execution based on results',
    tooltip: 'Use JSONPath expressions to define conditions',
    icon: '🔀',
  },
  // ... more node types
];
```

**Palette rendering:**

```tsx
// NodePalette.tsx
import { Tooltip } from 'antd';

const NodePalette = () => {
  return (
    <div className="node-palette">
      {NODE_TYPES.map((nodeType) => (
        <Tooltip key={nodeType.id} title={nodeType.tooltip} placement="right">
          <div
            draggable
            onDragStart={(e) => onDragStart(e, nodeType.id)}
            className="palette-item"
          >
            <div className="flex items-center gap-2">
              <span className="text-2xl">{nodeType.icon}</span>
              <div>
                <div className="font-semibold">{nodeType.title}</div>
                <div className="text-sm text-gray-600">{nodeType.description}</div>
              </div>
            </div>
          </div>
        </Tooltip>
      ))}
    </div>
  );
};
```

**Mobile "Add node" sheet:**

```tsx
// MobileAddNodeSheet.tsx
const MobileAddNodeSheet = ({ open, onClose, onAddNode }) => {
  return (
    <Drawer open={open} onClose={onClose} placement="bottom">
      <div className="space-y-2">
        {NODE_TYPES.map((nodeType) => (
          <div
            key={nodeType.id}
            onClick={() => {
              onAddNode(nodeType.id);
              onClose();
            }}
            className="p-4 border rounded cursor-pointer hover:bg-gray-50"
          >
            <div className="flex items-center gap-2">
              <span className="text-2xl">{nodeType.icon}</span>
              <div>
                <div className="font-semibold">{nodeType.title}</div>
                <div className="text-sm text-gray-600">{nodeType.description}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </Drawer>
  );
};
```

---

### 2.3 Copy Node (JSON + Duplicate)

**Implementation:**

```tsx
// WorkflowDrawer.tsx or NodeContextMenu.tsx
import { message } from 'antd';

const copyNodeToClipboard = (node: Node) => {
  const payload = {
    id: node.id,
    type: node.type,
    description: node.data.description,
    ruleList: node.data.ruleList,
    action: node.data.action,
    // Include all persisted fields
  };

  navigator.clipboard.writeText(JSON.stringify(payload, null, 2));
  message.success('Node configuration copied to clipboard');
};

const duplicateNodeOnCanvas = (node: Node, nodes: Node[], setNodes: Function) => {
  const newNode = {
    ...node,
    id: `node_${Date.now()}`, // New ID
    position: {
      x: node.position.x + 50, // Offset position
      y: node.position.y + 50,
    },
    data: {
      ...node.data, // Clone data
    },
  };

  setNodes([...nodes, newNode]);
  message.success('Node duplicated');
};
```

**Drawer header buttons:**

```tsx
// WorkflowDrawer.tsx
<div className="drawer-header flex justify-between items-center p-4 border-b">
  <h3>NODE CONFIGURATION</h3>
  <div className="flex gap-2">
    <Button
      icon={<CopyOutlined />}
      onClick={() => copyNodeToClipboard(selectedNode)}
    >
      Copy JSON
    </Button>
    <Button
      icon={<CopyOutlined />}
      onClick={() => duplicateNodeOnCanvas(selectedNode, nodes, setNodes)}
    >
      Duplicate
    </Button>
    <Button
      icon={<DeleteOutlined />}
      danger
      onClick={() => handleDeleteNode(selectedNode.id)}
    >
      Delete
    </Button>
  </div>
</div>
```

**Node context menu (right-click):**

```tsx
// NodeContextMenu.tsx
import { Menu } from 'antd';

const NodeContextMenu = ({ node, position, onClose }) => {
  const items = [
    {
      key: 'copy-json',
      label: 'Copy JSON',
      icon: <CopyOutlined />,
      onClick: () => {
        copyNodeToClipboard(node);
        onClose();
      },
    },
    {
      key: 'duplicate',
      label: 'Duplicate',
      icon: <CopyOutlined />,
      onClick: () => {
        duplicateNodeOnCanvas(node, nodes, setNodes);
        onClose();
      },
    },
    {
      key: 'delete',
      label: 'Delete',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: () => {
        handleDeleteNode(node.id);
        onClose();
      },
    },
  ];

  return (
    <Menu
      items={items}
      style={{ position: 'absolute', left: position.x, top: position.y }}
    />
  );
};

// In Canvas component
const [contextMenu, setContextMenu] = useState(null);

const onNodeContextMenu = useCallback((event: React.MouseEvent, node: Node) => {
  event.preventDefault();
  setContextMenu({
    node,
    position: { x: event.clientX, y: event.clientY },
  });
}, []);

<ReactFlow
  nodes={nodes}
  onNodeContextMenu={onNodeContextMenu}
  // ... other props
/>

{contextMenu && (
  <NodeContextMenu
    node={contextMenu.node}
    position={contextMenu.position}
    onClose={() => setContextMenu(null)}
  />
)}
```

---

### 2.4 Delete Node with Edge Cleanup

**Implementation:**

```tsx
const handleDeleteNode = (nodeId: string) => {
  const node = nodes.find((n) => n.id === nodeId);
  if (!node) return;

  // Check if confirmation needed
  const hasConfig = node.data.ruleList?.length > 0 || node.data.action;
  const connectedEdges = edges.filter(
    (e) => e.source === nodeId || e.target === nodeId
  );
  const needsConfirm = hasConfig || connectedEdges.length > 1;

  if (needsConfirm) {
    Modal.confirm({
      title: 'Delete Node',
      content: `This node has ${connectedEdges.length} connected edge(s) and configured rules/actions. Are you sure you want to delete it?`,
      okText: 'Delete',
      okButtonProps: { danger: true },
      onOk: () => performDelete(nodeId),
    });
  } else {
    performDelete(nodeId);
  }
};

const performDelete = (nodeId: string) => {
  // Remove node
  setNodes((nds) => nds.filter((n) => n.id !== nodeId));

  // Remove all connected edges
  setEdges((eds) => eds.filter((e) => e.source !== nodeId && e.target !== nodeId));

  message.success('Node deleted');
};
```

---

## 3. Implementation Plan

### Phase 1: Drag-and-Drop (3-4 hours)

1. Add `draggable` attribute to palette items
2. Implement `onDragStart` handler
3. Implement canvas `onDrop` and `onDragOver` handlers
4. Test: drag from palette, drop on canvas, verify node created at drop position
5. Verify existing click-to-add still works

---

### Phase 2: Node Descriptions (2-3 hours)

1. Define `NODE_TYPES` data structure with descriptions
2. Update palette rendering to show title + subtitle
3. Add tooltips for extended descriptions
4. Update mobile "Add node" sheet with same descriptions
5. Test: verify all node types have descriptions, mobile matches desktop

---

### Phase 3: Copy & Delete (4-5 hours)

1. Implement `copyNodeToClipboard()` function
2. Implement `duplicateNodeOnCanvas()` function
3. Implement `handleDeleteNode()` with confirmation logic
4. Add buttons to drawer header
5. Implement node context menu (right-click)
6. Test: copy JSON, duplicate, delete with/without confirmation

---

### Phase 4: Integration & Testing (2-3 hours)

1. Test all features together
2. Test on Desktop Chrome 1280px + Mobile Chrome 390×844
3. Run full E2E suite; verify no regressions
4. Document results in `ui-test-report-v32.0-part4.md`

---

## 4. Testing Strategy

### 4.1 Manual Testing

- **Drag-and-drop:**
  - Drag each node type from palette to canvas
  - Verify node created at drop position
  - Verify existing click-to-add still works

- **Descriptions:**
  - Verify all node types have title + subtitle
  - Hover over "?" icon; verify tooltip appears
  - Open mobile "Add node" sheet; verify same descriptions

- **Copy & Delete:**
  - Copy JSON to clipboard; paste into text editor; verify full payload
  - Duplicate node; verify new ID, offset position, same config
  - Delete node with no config/edges; verify no confirmation
  - Delete node with config/edges; verify confirmation shown

### 4.2 Playwright Testing

- Optional: Add test case for drag-and-drop (if stable)
- Optional: Add test case for copy/duplicate/delete
- Verify existing canvas tests still pass

---

## 5. Risks & Mitigations

**Risk:** Drag-and-drop conflicts with canvas pan/zoom.  
**Mitigation:** Use `event.dataTransfer` to distinguish drag-from-palette vs canvas pan.

**Risk:** Context menu doesn't close when clicking outside.  
**Mitigation:** Add global click listener to close context menu.

**Risk:** Duplicate node overlaps source node (offset too small).  
**Mitigation:** Use larger offset (50px) or smart positioning (find empty space).

---

## 6. Deployment

- **Branch:** `main` (workflow-ui)
- **Merge strategy:** One commit per phase for easier rollback
- **Rollback plan:** Revert individual commits if regressions detected

---

## 7. References

- ReactFlow drag-and-drop: https://reactflow.dev/examples/interaction/drag-and-drop
- ReactFlow context menu: https://reactflow.dev/examples/interaction/context-menu
- Ant Design Menu: https://ant.design/components/menu
- Ant Design Modal: https://ant.design/components/modal
