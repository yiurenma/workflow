# Arch Doc v5.0 — Application & Record Tables: Vertical Scroll, Total Count, Translation Fields

**Feature:** `TODO-application-record-tables-scroll-count-translation`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Current State Analysis

### `workflows/index.tsx` — Applications Table

```tsx
<Table<WorkflowEntitySettingRow>
  rowKey={...}
  columns={columns}
  dataSource={data?.content ?? []}
  size="middle"
  pagination={{ current, pageSize, total, showSizeChanger: false, onChange }}
  className="bg-white rounded-lg shadow-sm border border-zinc-200"
  rowClassName={...}
/>
```

- **No `scroll` prop** → no vertical scroll; table grows unbounded vertically.
- **Pagination** uses `total: data?.totalElements` but `showTotal` is not set → count is shown only as "(page X of Y)" from the Ant Design default.

### `records/index.tsx` — Records Table

```tsx
<Table<WorkflowRecord>
  rowKey={...}
  columns={columns}
  dataSource={data?.content ?? []}
  pagination={{ current, pageSize, total, showSizeChanger: false, onChange }}
  className="bg-white rounded-lg shadow-sm"
  scroll={{ x: 1200 }}
/>
```

- **Only horizontal scroll** (`x: 1200`); no `y` constraint → no vertical scroll.
- `showTotal` not set on pagination.
- **Mobile filter bar**: `!isMobile` guard hides Confirmation No., Tracking No., Customer ID, From, To filters on small screens.

---

## Proposed Changes

### 1. Add vertical scroll to both tables

Ant Design's `<Table>` supports `scroll={{ x: N, y: N }}`. Setting `y` to a CSS `calc()` expression fixes height relative to viewport, allowing the table body to scroll while the header stays pinned.

**Best value for `y`:**  
Use `"calc(100vh - 320px)"` for the Applications table (header + search bar above ~280px).  
Use `"calc(100vh - 360px)"` for the Records table (header + filter bar above ~320px).

These values are tuned to not overflow on standard 768px+ viewports; on shorter viewports the table body may be small but still scrollable. We do NOT change the canvas page — it is a separate route (`/$applicationName`).

### 2. Add `showTotal` to pagination

Ant Design pagination accepts `showTotal: (total) => string`. Add:

```tsx
showTotal: (total) => `${total} total`,
```

This renders "X total" next to the pagination controls — no extra DOM required.

### 3. Mobile filter bar for Records — add "More filters" collapsible section

Currently Confirmation No., Tracking No., Customer ID, From, To are hidden on mobile. Approach: wrap them in a collapsible `<details>`/state-toggled `<Flex>` block that the user can expand. This is simpler than a drawer and avoids new dependencies.

Implementation: add a `[showMoreFilters, setShowMoreFilters]` boolean state. Render a "More filters" `<Button type="text">` on mobile that toggles it. When open, show the extra filter inputs stacked vertically.

### 4. No canvas regression

The canvas route is `src/routes/workflows/$applicationName.tsx` (or similar). Both table pages are in:
- `src/routes/workflows/index.tsx`  
- `src/routes/records/index.tsx`

Changes are scoped entirely to these two files. Canvas layout is not touched.

---

## Files to Change

| File | Change |
|------|--------|
| `src/routes/workflows/index.tsx` | Add `scroll={{ y: "calc(100vh - 320px)" }}` to `<Table>`; add `showTotal` to pagination |
| `src/routes/records/index.tsx` | Add `scroll={{ x: 1200, y: "calc(100vh - 360px)" }}` to `<Table>`; add `showTotal` to pagination; add mobile "More filters" toggle for hidden filter inputs |

---

## Security / Risk

- Pure presentational change; no API changes.
- `scroll.y` is a CSS string — no injection risk.
- No new dependencies.
- Regression risk: very low; canvas route untouched.

---

## Trade-offs

| Option | Pros | Cons |
|--------|------|------|
| Fixed pixel `y` (e.g. 500) | Simple | Breaks on small/large viewports |
| `calc(100vh - Npx)` | Adapts to viewport | Requires correct offset tuning |
| CSS on container div | Framework-agnostic | Interacts oddly with Ant Table internals |

**Decision:** `calc(100vh - Npx)` via Ant Design `scroll.y`. Widely used pattern in the Ant Design community; works correctly with sticky table headers.

---

*Status: Draft*
