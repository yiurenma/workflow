# Arch Doc v22.0 — Mobile: Node Editor Blank Space Below Content

**Label:** `TODO-mobile-node-editor-blank-space-below`
**Date:** 2026-04-08

---

## Root Cause Analysis

Current `WorkflowDrawer` (`workflow-drawer/index.tsx`) on mobile:
- `placement="right"`, `width="100%"` — full-width right-side drawer
- Body has `className="pb-20"` — 80 px bottom padding
- No explicit height constraint; the Ant Design Drawer defaults to `height: 100%` of the viewport

On iOS Safari, `100vh` includes the browser chrome (address bar + bottom toolbar), making the drawer taller than the **visible** viewport. Content renders at the top but the drawer body extends into the invisible area — creating a large blank region before the home-indicator safe area.

---

## Fix

**Switch to `placement="bottom"` on mobile** — a bottom sheet is more natural for mobile node editing and avoids the `100vh` problem entirely.

```tsx
<Drawer
  placement={isMobile ? 'bottom' : 'right'}
  height={isMobile ? 'auto' : undefined}
  width={isMobile ? undefined : 420}
  styles={{
    body: {
      padding: '16px',
      paddingBottom: isMobile
        ? 'calc(16px + env(safe-area-inset-bottom))'
        : '16px',
      maxHeight: isMobile ? '80dvh' : undefined,
      overflowY: 'auto',
    },
    header: { padding: '12px 16px' },
  }}
  ...
>
```

Key points:
- `height="auto"` lets the drawer size to its content (no blank space)
- `maxHeight: 80dvh` caps it so it never covers the full screen (`dvh` = dynamic viewport height, respects Safari chrome)
- `env(safe-area-inset-bottom)` prevents content being hidden behind the home indicator
- `overflowY: 'auto'` keeps the body scrollable when content is tall

---

## Scope

| File | Change |
|------|--------|
| `workflow-drawer/index.tsx` | Conditional `placement`, `height`, `styles.body` based on `isMobile` |

No new dependencies. `useIsMobile` already imported in the parent component.

---

## Risk

Low. Visual/layout change only on mobile. Desktop drawer unchanged.
