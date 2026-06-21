# Architecture — Workflow Studio Unified Program Part 3: Node Editor Enhancements (v32.0)

**Document version:** 32.0 Part 3  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Node editor rule key validation + resizable drawer + read-first mode  
**Repository:** `workflow-ui`

---

## 1. Technical Context

### 1.1 Current Node Editor Implementation

**File:** `workflow-ui/src/components/WorkflowDrawer.tsx` (or equivalent)

**Current behavior:**
- Opens in edit mode by default (all fields are inputs)
- Fixed width (no resize handle)
- Rule key field accepts any text (no validation)

**Gaps:**
- Users can save invalid JSONPath expressions
- Long content requires horizontal scrolling
- No quick-skim view for read-only inspection

---

## 2. Solution Design

### 2.1 Rule Key JSONPath Validation

**Approach:** Client-side validation using `jsonpath-plus` library (already used for JsonPath modal).

**Validation logic:**

```typescript
import { JSONPath } from 'jsonpath-plus';

function validateRuleKey(value: string): { valid: boolean; error?: string } {
  if (!value.trim()) {
    return { valid: true }; // Empty is allowed if rules optional
  }

  // Check for comma-separated paths (not allowed)
  if (value.includes(',')) {
    return {
      valid: false,
      error: 'Rule key must be a single JSONPath expression (no comma-separated paths)',
    };
  }

  // Validate JSONPath syntax
  try {
    JSONPath({ path: value, json: {} }); // Dry-run parse
    return { valid: true };
  } catch (err) {
    return {
      valid: false,
      error: `Invalid JSONPath syntax: ${err.message}`,
    };
  }
}
```

**Integration:**

```tsx
// In WorkflowDrawer or RuleForm component
const [ruleKeyError, setRuleKeyError] = useState<string | undefined>();

const handleRuleKeyBlur = (value: string) => {
  const result = validateRuleKey(value);
  setRuleKeyError(result.error);
};

<Input
  value={ruleKey}
  onChange={(e) => setRuleKey(e.target.value)}
  onBlur={(e) => handleRuleKeyBlur(e.target.value)}
  status={ruleKeyError ? 'error' : undefined}
/>
{ruleKeyError && (
  <div className="text-red-600 text-sm mt-1">{ruleKeyError}</div>
)}
```

**Save blocking:**

```tsx
const canSave = !ruleKeyError && /* other validations */;

<Button
  type="primary"
  onClick={handleSave}
  disabled={!canSave}
>
  Save
</Button>
```

---

### 2.2 Resizable Drawer

**Approach:** Use `react-resizable-panels` or custom drag handler.

**Option A: react-resizable-panels (recommended)**

```tsx
import { Panel, PanelGroup, PanelResizeHandle } from 'react-resizable-panels';

<PanelGroup direction="horizontal">
  <Panel defaultSize={70} minSize={20}>
    {/* Canvas */}
    <ReactFlow ... />
  </Panel>
  
  <PanelResizeHandle className="w-1 bg-gray-300 hover:bg-blue-500 cursor-col-resize" />
  
  <Panel defaultSize={30} minSize={20} maxSize={80}>
    {/* Drawer content */}
    <div className="h-full overflow-y-auto p-4">
      {drawerContent}
    </div>
  </Panel>
</PanelGroup>
```

**Option B: Custom drag handler**

```tsx
const [drawerWidth, setDrawerWidth] = useState(400);
const [isDragging, setIsDragging] = useState(false);

const handleMouseDown = () => setIsDragging(true);

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging) return;
  const newWidth = window.innerWidth - e.clientX;
  setDrawerWidth(Math.max(400, Math.min(newWidth, window.innerWidth * 0.8)));
};

const handleMouseUp = () => {
  setIsDragging(false);
  localStorage.setItem('workflow_drawer_width', String(drawerWidth));
};

useEffect(() => {
  if (isDragging) {
    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }
}, [isDragging]);

<Drawer
  open={open}
  width={drawerWidth}
  onClose={handleClose}
>
  <div
    className="absolute left-0 top-0 bottom-0 w-1 bg-gray-300 hover:bg-blue-500 cursor-col-resize"
    onMouseDown={handleMouseDown}
  />
  {drawerContent}
</Drawer>
```

**Persistence:**

```typescript
// On mount
useEffect(() => {
  const saved = localStorage.getItem('workflow_drawer_width');
  if (saved) setDrawerWidth(Number(saved));
}, []);

// On resize
const handleResize = (newWidth: number) => {
  setDrawerWidth(newWidth);
  localStorage.setItem('workflow_drawer_width', String(newWidth));
};
```

---

### 2.3 Read-Only View Mode

**Approach:** Two-mode drawer with toggle.

**State management:**

```tsx
type DrawerMode = 'view' | 'edit';

const [mode, setMode] = useState<DrawerMode>('view');
const [draft, setDraft] = useState<NodeConfig | null>(null);

// On node select
const handleNodeSelect = (node: Node) => {
  setMode('view'); // Always open in view mode
  setDraft(null);
};

// Switch to edit
const handleEdit = () => {
  setDraft({ ...selectedNode }); // Clone current config
  setMode('edit');
};

// Save
const handleSave = () => {
  // Validate
  if (ruleKeyError) return;
  
  // Save draft to node
  updateNode(selectedNode.id, draft);
  setMode('view');
  setDraft(null);
};

// Cancel
const handleCancel = () => {
  setDraft(null);
  setMode('view');
};
```

**View mode rendering:**

```tsx
{mode === 'view' && (
  <div className="space-y-4">
    <div className="flex justify-between items-center">
      <h3>NODE CONFIGURATION</h3>
      <Button onClick={handleEdit}>Edit</Button>
    </div>
    
    <div>
      <h4>Description</h4>
      <p className="whitespace-pre-wrap">{selectedNode.description}</p>
    </div>
    
    <div>
      <h4>Rules</h4>
      <pre className="bg-gray-100 p-2 rounded overflow-x-auto">
        {JSON.stringify(selectedNode.ruleList, null, 2)}
      </pre>
    </div>
    
    <div>
      <h4>Action</h4>
      <pre className="bg-gray-100 p-2 rounded overflow-x-auto">
        {JSON.stringify(selectedNode.action, null, 2)}
      </pre>
    </div>
  </div>
)}
```

**Edit mode rendering:**

```tsx
{mode === 'edit' && (
  <div className="space-y-4">
    <div className="flex justify-between items-center">
      <h3>EDIT NODE</h3>
      <div className="space-x-2">
        <Button onClick={handleCancel}>Cancel</Button>
        <Button type="primary" onClick={handleSave} disabled={!canSave}>
          Done
        </Button>
      </div>
    </div>
    
    <div>
      <label>Description</label>
      <Input.TextArea
        value={draft.description}
        onChange={(e) => setDraft({ ...draft, description: e.target.value })}
      />
    </div>
    
    <div>
      <label>Rules</label>
      {/* Rule form fields with validation */}
    </div>
    
    <div>
      <label>Action</label>
      {/* Action form fields */}
    </div>
  </div>
)}
```

---

### 2.4 Long Content Handling

**Scrolling:**

```tsx
<Drawer
  open={open}
  width={drawerWidth}
  bodyStyle={{ height: '100%', overflow: 'hidden' }}
>
  <div className="h-full flex flex-col">
    <div className="flex-shrink-0 p-4 border-b">
      {/* Header with Edit/Done/Cancel buttons */}
    </div>
    
    <div className="flex-1 overflow-y-auto p-4">
      {/* Scrollable content */}
    </div>
  </div>
</Drawer>
```

**Reflow on resize:**

```tsx
// Content automatically reflows when drawer width changes
// Use `word-break: break-word` for long strings
<pre className="whitespace-pre-wrap break-words">
  {JSON.stringify(data, null, 2)}
</pre>
```

**Virtualization (if needed):**

```tsx
import { FixedSizeList } from 'react-window';

// Only if content >1000 lines
{lines.length > 1000 ? (
  <FixedSizeList
    height={600}
    itemCount={lines.length}
    itemSize={20}
    width="100%"
  >
    {({ index, style }) => (
      <div style={style}>{lines[index]}</div>
    )}
  </FixedSizeList>
) : (
  <pre>{lines.join('\n')}</pre>
)}
```

---

## 3. Implementation Plan

### Phase 1: Rule Key Validation (2-3 hours)

1. Add `validateRuleKey()` function using `jsonpath-plus`
2. Integrate validation in rule form component
3. Add error display + save blocking
4. Manual test: valid paths, invalid paths, comma-separated paths
5. Optional: Add Playwright test case

---

### Phase 2: Resizable Drawer (3-4 hours)

1. Choose approach (react-resizable-panels vs custom)
2. Implement drag handle + resize logic
3. Add min/max width constraints
4. Add localStorage persistence
5. Manual test: drag to resize, persist across sessions
6. Mobile: document deferral or implement touch drag

---

### Phase 3: Read-Only View Mode (4-5 hours)

1. Add `mode` state ('view' | 'edit')
2. Implement view mode rendering (formatted text, pretty JSON)
3. Implement edit mode rendering (existing form fields)
4. Add Edit / Done / Cancel buttons
5. Manual test: open in view, switch to edit, save, cancel
6. Test long content scrolling + reflow

---

### Phase 4: Integration & Testing (2-3 hours)

1. Coordinate with drawer close fix (Part 1 §1) if overlapping
2. Test all drawer interactions:
   - Open drawer (view mode)
   - Close drawer (× button, outside click, ESC)
   - Resize drawer (drag handle)
   - Switch to edit mode
   - Save with valid/invalid rule keys
   - Cancel edit
3. Run full E2E suite; verify no regressions

---

## 4. Testing Strategy

### 4.1 Manual Testing

- **Rule key validation:**
  - Valid: `$.data.userId`, `$.items[0].name`, `$..price`
  - Invalid: `$.data.userId, $.data.userName` (comma-separated), `not a path`, `$.data.[invalid`
  - Empty: should be allowed if rules optional

- **Resizable drawer:**
  - Drag handle to widen/narrow drawer
  - Verify min/max width constraints
  - Refresh page; verify width persists

- **Read-only view:**
  - Open node; verify view mode (no input fields)
  - Click Edit; verify edit mode (input fields)
  - Make changes; click Done; verify saved
  - Make changes; click Cancel; verify discarded

### 4.2 Playwright Testing

- Optional: Add test case for rule key validation
- Optional: Add test case for drawer resize (if stable)
- Verify existing drawer tests still pass

---

## 5. Risks & Mitigations

**Risk:** Resizable drawer conflicts with ReactFlow canvas interactions.  
**Mitigation:** Use high z-index for drag handle; test pointer events don't leak to canvas.

**Risk:** Read-only view breaks existing workflows that expect edit mode on open.  
**Mitigation:** User testing; add preference toggle if needed.

**Risk:** JSONPath validation too strict; rejects valid edge cases.  
**Mitigation:** Test against real-world rule keys; adjust validation logic if needed.

---

## 6. Deployment

- **Branch:** `main` (workflow-ui)
- **Merge strategy:** One commit per phase for easier rollback
- **Rollback plan:** Revert individual commits if regressions detected

---

## 7. References

- JSONPath spec: https://goessner.net/articles/JsonPath/
- `jsonpath-plus`: https://www.npmjs.com/package/jsonpath-plus
- `react-resizable-panels`: https://www.npmjs.com/package/react-resizable-panels
- Ant Design Drawer: https://ant.design/components/drawer
