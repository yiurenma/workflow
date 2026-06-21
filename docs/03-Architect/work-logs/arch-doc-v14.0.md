# Arch Doc v14.0 — Mobile Records: Ant Design Pagination on narrow viewport

**Label:** `TODO-mobile-records-pagination-invisible`
**Date:** 2026-04-08

---

## Scope

Single file change: `workflow-ui/src/routes/records/index.tsx`

---

## Current Mobile Pagination Code

```tsx
{/* Mobile pagination */}
{(data?.totalElements ?? 0) > 0 && (
  <div className="flex flex-col gap-1 pt-2">
    <span className="text-xs text-slate-400 text-center">
      {data?.totalElements ?? 0} total · Page {page + 1} of {Math.max(1, Math.ceil((data?.totalElements ?? 0) / PAGE_SIZE))}
    </span>
    {(data?.totalElements ?? 0) > PAGE_SIZE && (
      <div className="flex justify-between items-center">
        <Button disabled={page === 0} onClick={() => setPage(p => Math.max(0, p - 1))} size="small">
          Previous
        </Button>
        <Button disabled={(page + 1) * PAGE_SIZE >= (data?.totalElements ?? 0)} onClick={() => setPage(p => p + 1)} size="small">
          Next
        </Button>
      </div>
    )}
  </div>
)}
```

Problems:
1. Custom Prev/Next buttons have class `.ant-btn` but NOT `.ant-pagination` — TC-REC-03 Desktop query fails.
2. Buttons only render when `totalElements > PAGE_SIZE` — if exactly 1 page of results, nothing is shown at all.
3. Inconsistent with desktop which uses Ant Design `<Pagination>`.

---

## Fix

Replace the custom mobile pagination block with an Ant Design `<Pagination size="small">`:

```tsx
{/* Mobile pagination — use same Ant Design Pagination as desktop */}
{(data?.totalElements ?? 0) > 0 && (
  <div className="flex flex-col items-center gap-1 pt-2">
    <span className="text-xs text-slate-400">
      {data?.totalElements ?? 0} total
    </span>
    <Pagination
      current={page + 1}
      pageSize={PAGE_SIZE}
      total={data?.totalElements ?? 0}
      showSizeChanger={false}
      hideOnSinglePage={false}
      onChange={(p) => setPage(p - 1)}
      size="small"
    />
  </div>
)}
```

`Pagination` is already imported in the file (used in the desktop branch) — no new import needed.

---

## Diff Summary

| File | Change |
|------|--------|
| `src/routes/records/index.tsx` | Replace custom Prev/Next block (lines 249–273) with `<Pagination size="small">` |

---

## Test Impact

TC-REC-03 currently uses dual-check (`.ant-pagination` OR `Previous` button). After this fix:
- `.ant-pagination` is rendered on mobile too.
- The `Previous` button fallback is no longer needed but the dual-check remains valid.
- Optionally simplify TC-REC-03 to only check `.ant-pagination`.

---

## No New Dependencies

`Pagination` is already imported from `antd`.
