# Arch Doc v7.0 — Q2 Mobile: App Card Actions + Desktop View Entry

**Feature:** `TODO-q2-mobile-apps-actions-desktop-entry-constraints`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Current State

`src/routes/workflows/index.tsx` — mobile card section:

```tsx
<div className="flex items-center gap-2 shrink-0">
  <Button type="text" size="small" onClick={(e) => { e.stopPropagation(); setSettingsTarget(record); }}>
    Settings
  </Button>
  <RightOutlined className="text-zinc-300 text-xs" />
</div>
```

- Only Settings + arrow visible on mobile.
- Copy / Delete modals already exist on the page but are only reachable from the desktop table's action column.
- `isMobile` from `useIsMobile()` drives which layout renders; currently binary.

---

## Proposed Changes

### 1. Overflow menu on mobile cards (US-36)

Use Ant Design `<Dropdown>` with a `menu` prop. The trigger is an `<EllipsisOutlined>` button placed in the card's action area next to Settings.

```tsx
import { Dropdown } from "antd";
import { EllipsisOutlined } from "@ant-design/icons";

const cardMenu = (record) => ({
  items: [
    { key: "copy",    label: "Copy",   onClick: () => { setCopySource(record.applicationName); setCopyTargetName(""); } },
    { key: "history", label: "History", onClick: () => setHistoryTarget(record.applicationName) },
    { key: "delete",  label: <span className="text-red-500">Delete</span>,
      onClick: () => Modal.confirm({ ... /* same as desktop */ }) },
  ],
});

<Dropdown menu={cardMenu(record)} trigger={["click"]} placement="bottomRight">
  <Button type="text" size="small" icon={<EllipsisOutlined />} onClick={(e) => e.stopPropagation()} />
</Dropdown>
```

The Copy and Delete modals (`copySource`, `deleteApplication`) are already wired at the page level — no new state needed.

### 2. Desktop view toggle (US-37)

Introduce a `desktopOverride` boolean stored in `localStorage` key `"workflow_list_desktop_view"`.

```tsx
const [desktopOverride, setDesktopOverride] = useState(
  () => localStorage.getItem("workflow_list_desktop_view") === "true"
);

// Effective layout decision
const showDesktop = !isMobile || desktopOverride;
```

Replace all `isMobile ?` branches with `!showDesktop ?`. This means:
- On desktop: `showDesktop = true` always → table view (unchanged)
- On mobile default: `showDesktop = false` → card view (unchanged)
- On mobile with toggle: `showDesktop = true` → full table view

The toggle button is shown only on mobile:

```tsx
{isMobile && (
  <Button size="small" type="text" onClick={() => {
    const next = !desktopOverride;
    setDesktopOverride(next);
    localStorage.setItem("workflow_list_desktop_view", String(next));
  }}>
    {desktopOverride ? "Mobile view" : "Desktop view"}
  </Button>
)}
```

Placed in the header row alongside the search bar.

### 3. No canvas regression

Only `workflows/index.tsx` is changed. Canvas route (`$applicationName.tsx`) is untouched. The `isMobile` hook is still used for other decisions (padding, FAB button visibility).

---

## Files to Change

| File | Change |
|------|--------|
| `src/routes/workflows/index.tsx` | Add Dropdown overflow menu to mobile cards; add desktopOverride state + localStorage; replace `isMobile` layout branches with `showDesktop` |

---

## Security / Risk

- `localStorage` value is a simple string "true"/"false" — no injection risk.
- `Dropdown` uses Ant Design's built-in rendering — no XSS.
- Delete confirmation modal is unchanged.
- No new dependencies.

---

*Status: Draft*
