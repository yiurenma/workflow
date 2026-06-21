# Arch Doc v11.0 — Mobile Overflow Menu: Ghost-Click Fix

**Status:** Draft  
**Label:** `TODO-mobile-app-overflow-history-copy-modal-not-navigation`  
**Date:** 2026-04-07

---

## 1. Root Cause Analysis

### Current code structure (`workflows/index.tsx` — mobile card)

```tsx
<div
  onClick={() => navigate({ to: "/workflows/$applicationName", ... })}  // ← problem
>
  {/* card content */}
  <Button onClick={(e) => e.stopPropagation()} />  {/* ⋯ button */}
  <Dropdown menu={cardMenu(record)} trigger={["click"]}>
    ...
  </Dropdown>
</div>
```

`e.stopPropagation()` on the ⋯ button prevents the *button click* from reaching the card. But after the user taps a **dropdown menu item**:

1. The menu item's Ant Design RC-Trigger portal receives the touchstart/touchend.
2. The portal unmounts after the action runs.
3. The browser fires a synthetic `click` event ~300 ms later at the original touch coordinates — now hitting the underlying card `<div>` (the portal is gone).
4. The card's `onClick` → `navigate(...)` fires.

This is classic *ghost click* after portal teardown on mobile WebKit/Blink.

---

## 2. Fix Strategy

**Isolate the navigation trigger to a dedicated inner element** rather than the entire card `<div>`. The actions area (dropdown, settings button) sits *outside* the navigation-trigger element and does not propagate clicks to it.

### Proposed structure

```tsx
<div className="bg-white rounded-lg ...">
  {/* Navigation zone — only this area navigates */}
  <div
    className="flex-1 min-w-0 pr-3 cursor-pointer"
    onClick={() => navigate({ to: "/workflows/$applicationName", ... })}
  >
    {/* name, tags, description, date */}
  </div>

  {/* Actions zone — no navigation trigger on parent */}
  <div className="flex items-center gap-1 shrink-0">
    <Button onClick={() => setSettingsTarget(record)}>Settings</Button>
    <Dropdown menu={cardMenu(record)} trigger={["click"]} placement="bottomRight">
      <Button icon={<EllipsisOutlined />} onClick={(e) => e.stopPropagation()} />
    </Dropdown>
    <RightOutlined />
  </div>
</div>
```

Because the actions zone is outside the navigation `<div>`, ghost clicks that land on the card background (after the portal unmounts) never reach a navigate handler.

---

## 3. Why This Works

- The navigation `onClick` lives only in the info column `<div>`.
- The actions column `<div>` has **no** `onClick` at the card level.
- After the dropdown portal unmounts, any ghost click fires on the outer card `<div>` — which also has **no** `onClick` in the new layout.
- There is no need to add `preventDefault`, `stopPropagation`, or any touch-event hacks.

---

## 4. Alternative Approaches Considered

| Approach | Problem |
|----------|---------|
| `setTimeout(() => setHistoryTarget(...), 300)` delay | Fragile; timing-dependent; doesn't fix root cause |
| `onTouchEnd` + `preventDefault` on menu items | Requires custom menu item wrappers; brittle across Ant Design versions |
| `pointer-events: none` on overlay | Would block menu interactions |
| `stopPropagation` on the entire Dropdown | Ant Design Dropdown renders in portal; stopPropagation on portal root doesn't reach card |

---

## 5. Scope of Change

| Item | Detail |
|------|--------|
| File | `workflow-ui/src/routes/workflows/index.tsx` |
| Change type | Restructure mobile card JSX — move `onClick` from outer card `<div>` to inner info `<div>` |
| Risk | Low — desktop table view unchanged; mobile FAB, Settings, drawer, modals all unaffected |
| Regression | Run mobile card test cases (TC-MOB-OVF-*) |

---

## 6. Data Flow (unchanged)

```
Tap ⋯ button → Dropdown opens → Tap menu item → cardMenu onClick() → setState()
                                                                    ↓
                                                     HistoryDrawer / Modal / Modal.confirm()
```

Navigate is now only triggered by tapping the info area of the card.

---

## 7. Security / Accessibility

- No new data exposure; no API changes.
- The info area is a `<div>` with `cursor-pointer` styling — for full accessibility it should ideally be a `<button>` or `<a>`, but this is not required for the current fix scope. A future a11y pass can address this separately.
