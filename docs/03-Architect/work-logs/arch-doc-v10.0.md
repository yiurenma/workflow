# Arch Doc v10.0 — Mobile: Add-Application FAB Draggable

**Feature:** `TODO-mobile-add-application-fab-draggable`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Current State

```tsx
{isMobile && (
  <button
    onClick={openCreateDialog}
    className="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full bg-indigo-600 ..."
    aria-label="New application"
  >
    <PlusOutlined />
  </button>
)}
```

Fixed position, hardcoded corner. No drag support.

---

## Proposed Implementation

### State

```tsx
const FAB_POS_KEY = "workflow_fab_pos";

function loadFabPos(): { x: number; y: number } {
  try {
    const s = localStorage.getItem(FAB_POS_KEY);
    if (s) return JSON.parse(s);
  } catch {}
  // Default: bottom-right (expressed as distance from bottom/right edges)
  return { x: 24, y: 24 };
}

const [fabPos, setFabPos] = useState<{ x: number; y: number }>(loadFabPos);
```

`x` = distance from right edge, `y` = distance from bottom edge.

### Drag Logic (pointer events — works for both touch and mouse)

```tsx
const isDragging = useRef(false);
const dragStart  = useRef<{ px: number; py: number; fx: number; fy: number } | null>(null);

const onPointerDown = (e: React.PointerEvent) => {
  e.currentTarget.setPointerCapture(e.pointerId);
  isDragging.current = false;
  dragStart.current = { px: e.clientX, py: e.clientY, fx: fabPos.x, fy: fabPos.y };
};

const onPointerMove = (e: React.PointerEvent) => {
  if (!dragStart.current) return;
  const dx = e.clientX - dragStart.current.px;
  const dy = e.clientY - dragStart.current.py;
  if (Math.abs(dx) > 4 || Math.abs(dy) > 4) isDragging.current = true;
  if (!isDragging.current) return;
  const newX = Math.max(8, dragStart.current.fx - dx);
  const newY = Math.max(8, dragStart.current.fy - dy);
  setFabPos({ x: newX, y: newY });
};

const onPointerUp = (e: React.PointerEvent) => {
  if (!isDragging.current) {
    openCreateDialog(); // tap
  } else {
    // Snap to nearest horizontal edge
    const snapX = fabPos.x < window.innerWidth / 2 ? window.innerWidth - 56 - 8 : 24;
    const snapped = { x: snapX, y: Math.max(8, Math.min(fabPos.y, window.innerHeight - 80)) };
    setFabPos(snapped);
    localStorage.setItem(FAB_POS_KEY, JSON.stringify(snapped));
  }
  isDragging.current = false;
  dragStart.current = null;
};
```

### Render

```tsx
<button
  onPointerDown={onPointerDown}
  onPointerMove={onPointerMove}
  onPointerUp={onPointerUp}
  style={{ bottom: fabPos.y, right: fabPos.x, position: "fixed" }}
  className="z-50 w-14 h-14 rounded-full bg-indigo-600 text-white shadow-lg flex items-center justify-center text-2xl touch-none"
  aria-label="New application"
>
  <PlusOutlined />
</button>
```

`touch-none` (Tailwind) sets `touch-action: none` — prevents browser scroll hijacking during drag.

---

## Files to Change

| File | Change |
|------|--------|
| `src/routes/workflows/index.tsx` | Replace static FAB with draggable version; add state + pointer handlers |

---

## Risk

- `setPointerCapture` ensures `onPointerMove` fires even if the pointer leaves the button — essential for fast drags.
- `isDragging` ref (not state) avoids re-renders during move.
- `touch-none` is critical on iOS Safari — without it, the page scroll intercepts the drag.
- Desktop unaffected (FAB only renders when `isMobile`).

---

*Status: Draft*
