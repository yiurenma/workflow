# Architecture Document v31.0

**Document version:** 31.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-node-editor-draggable-width-readonly-default`

## Overview

Technical approach for adding resizable width, read-first mode, and edit gate to node editor drawer.

## Current Implementation

**Location:** `workflow-ui/src/components/WorkflowDrawer.tsx` (or similar)

Current drawer uses Ant Design `<Drawer>` with fixed width and immediate edit mode.

## Proposed Changes

### 1. Resizable Width

**Approach:** Add a draggable resize handle on the left edge of the drawer.

**Implementation:**
```tsx
const [drawerWidth, setDrawerWidth] = useState(() => {
  const saved = localStorage.getItem('node_editor_width');
  return saved ? parseInt(saved) : 480; // default 480px
});

const MIN_WIDTH = 320;
const MAX_WIDTH_PERCENT = 0.8;

const handleMouseDown = (e: React.MouseEvent) => {
  const startX = e.clientX;
  const startWidth = drawerWidth;
  
  const handleMouseMove = (e: MouseEvent) => {
    const delta = startX - e.clientX; // drag left = wider
    const newWidth = Math.max(MIN_WIDTH, 
      Math.min(window.innerWidth * MAX_WIDTH_PERCENT, startWidth + delta));
    setDrawerWidth(newWidth);
  };
  
  const handleMouseUp = () => {
    localStorage.setItem('node_editor_width', drawerWidth.toString());
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
  };
  
  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);
};

<Drawer width={drawerWidth}>
  <div className="resize-handle" onMouseDown={handleMouseDown} />
  {/* drawer content */}
</Drawer>
```

**Mobile:** Defer touch drag or use fixed width on mobile.

### 2. Read-First Mode

**Approach:** Add `mode` state: `'view' | 'edit'`

**Implementation:**
```tsx
const [mode, setMode] = useState<'view' | 'edit'>('view');

// View mode component
const NodeView = ({ node }) => (
  <div>
    <h3>Description</h3>
    <p>{node.description || 'No description'}</p>
    
    <h3>Rules</h3>
    <pre>{JSON.stringify(node.ruleList, null, 2)}</pre>
    
    <h3>Action</h3>
    <pre>{JSON.stringify(node.action, null, 2)}</pre>
  </div>
);

// Edit mode component (existing form)
const NodeEdit = ({ node, onChange }) => (
  <Form>
    <Form.Item label="Description">
      <Input.TextArea value={node.description} onChange={...} />
    </Form.Item>
    {/* existing form fields */}
  </Form>
);

return (
  <Drawer>
    {mode === 'view' ? (
      <>
        <Button onClick={() => setMode('edit')}>Edit</Button>
        <NodeView node={selectedNode} />
      </>
    ) : (
      <>
        <Button onClick={handleDone}>Done</Button>
        <Button onClick={handleCancel}>Cancel</Button>
        <NodeEdit node={selectedNode} onChange={...} />
      </>
    )}
  </Drawer>
);
```

### 3. Edit Gate Controls

**Done button:** Save changes and switch back to view mode
**Cancel button:** Discard changes and switch back to view mode

**State management:**
```tsx
const [editingNode, setEditingNode] = useState(null);

const handleEdit = () => {
  setEditingNode({ ...selectedNode }); // clone for editing
  setMode('edit');
};

const handleDone = () => {
  // Save editingNode changes to selectedNode
  onNodeUpdate(editingNode);
  setMode('view');
};

const handleCancel = () => {
  setEditingNode(null); // discard changes
  setMode('view');
};
```

### 4. Content Reflow

**Approach:** Use CSS `word-wrap` and `overflow-wrap` for text reflow.

```css
.node-view-content {
  width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.node-view-content pre {
  white-space: pre-wrap;
  word-break: break-all;
}
```

## API Changes

None required. This is a frontend-only change.

## Testing Strategy

1. Manual testing: resize drawer, verify width persists
2. Manual testing: open drawer in view mode, click Edit, verify form appears
3. Manual testing: edit content, click Done, verify changes saved
4. Manual testing: edit content, click Cancel, verify changes discarded
5. Playwright E2E: add test cases for view/edit mode switching

## Risks

- Low risk, isolated to drawer component
- May need to adjust layout for narrow widths
- Mobile touch drag may be complex, consider deferring

## Success Criteria

- Drawer width adjustable from 320px to 80% viewport
- Width persists in localStorage
- View mode shows formatted content
- Edit button switches to form mode
- Done/Cancel buttons work correctly
- Content reflows on resize
