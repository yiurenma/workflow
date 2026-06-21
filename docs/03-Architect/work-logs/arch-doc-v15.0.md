# Arch Doc v15.0 — Canvas Mobile: Draggable Add-Node FAB

**Label:** `TODO-mobile-canvas-add-node-fab-draggable`
**Date:** 2026-04-08

---

## Scope

Single file change: `workflow-ui/src/routes/workflows/-components/worflow-canvas/MobileAddNodeSheet.tsx`

---

## Reference Implementation

The applications list FAB in `src/routes/workflows/index.tsx` uses:
- `localStorage` key `workflow_fab_pos` for position persistence
- Pointer events (`onPointerDown`, `onPointerMove`, `onPointerUp`) on the button element
- Edge-snap logic: on release, move x to `left: margin` or `right: margin` based on which side of screen the pointer is closer to
- Drag-vs-tap threshold: `Math.abs(dx) + Math.abs(dy) < 5` → treat as tap

The canvas FAB uses localStorage key `workflow_canvas_fab_pos` (different key to avoid collision).

---

## Implementation Plan

### State

```ts
const FAB_POS_KEY = 'workflow_canvas_fab_pos';
const EDGE_MARGIN = 16; // px from screen edge

const loadPos = (): { x: number; y: number } => {
  try {
    const raw = localStorage.getItem(FAB_POS_KEY);
    if (raw) return JSON.parse(raw);
  } catch { /* ignore */ }
  return { x: EDGE_MARGIN, y: window.innerHeight - 96 }; // default: bottom-left
};

const [pos, setPos] = useState(loadPos);
const dragRef = useRef<{ startX: number; startY: number; origX: number; origY: number; moved: boolean } | null>(null);
```

### Pointer Event Handlers

```ts
const onPointerDown = (e: React.PointerEvent<HTMLButtonElement>) => {
  e.currentTarget.setPointerCapture(e.pointerId);
  dragRef.current = { startX: e.clientX, startY: e.clientY, origX: pos.x, origY: pos.y, moved: false };
};

const onPointerMove = (e: React.PointerEvent<HTMLButtonElement>) => {
  if (!dragRef.current) return;
  const dx = e.clientX - dragRef.current.startX;
  const dy = e.clientY - dragRef.current.startY;
  if (!dragRef.current.moved && Math.abs(dx) + Math.abs(dy) >= 5) {
    dragRef.current.moved = true;
  }
  setPos({ x: dragRef.current.origX + dx, y: dragRef.current.origY + dy });
};

const onPointerUp = (e: React.PointerEvent<HTMLButtonElement>) => {
  if (!dragRef.current) return;
  const { moved } = dragRef.current;
  dragRef.current = null;
  if (!moved) {
    setOpen(true); // tap — open sheet
    return;
  }
  // Edge snap
  const w = window.innerWidth;
  const snapX = e.clientX < w / 2 ? EDGE_MARGIN : w - EDGE_MARGIN - 48; // 48 = button width
  const snapY = Math.min(Math.max(e.clientY, EDGE_MARGIN), window.innerHeight - EDGE_MARGIN - 48);
  const newPos = { x: snapX, y: snapY };
  setPos(newPos);
  localStorage.setItem(FAB_POS_KEY, JSON.stringify(newPos));
};
```

### Button Rendering

Replace `className="fixed bottom-20 left-4 ..."` with inline `style`:

```tsx
<button
  style={{ position: 'fixed', left: pos.x, top: pos.y, touchAction: 'none' }}
  onPointerDown={onPointerDown}
  onPointerMove={onPointerMove}
  onPointerUp={onPointerUp}
  className="z-50 w-12 h-12 rounded-full bg-indigo-600 text-white shadow-lg flex items-center justify-center text-xl hover:bg-indigo-700 active:scale-95 transition-all"
  aria-label="Add node"
>
  <PlusOutlined />
</button>
```

The `onClick` handler is **removed** (replaced by `onPointerUp` tap logic) to prevent double-open.

---

## Diff Summary

| File | Change |
|------|--------|
| `MobileAddNodeSheet.tsx` | Add `pos` state + `dragRef`, replace `onClick`/`className` with pointer handlers + `style` |

---

## No New Dependencies

Uses only React hooks and browser pointer events — no new packages.
