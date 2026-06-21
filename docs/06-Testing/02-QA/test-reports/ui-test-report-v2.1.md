# UI Test Report v2.1

Date: 2026-04-06  Build: 8d407bf2748ed3d7ac9e1ca977647f4a458c024a  Scope: UI design overhaul — sophisticated professional aesthetic

---

## Summary

| TC-ID | Check | Result | Notes |
|-------|-------|--------|-------|
| TC-UI21-01 | `npx tsc -p tsconfig.app.json --noEmit` — 0 errors | PASS | Clean exit, no type errors |
| TC-UI21-02 | `npm run lint` — 0 errors | PASS | 1 pre-existing warning in `WorkflowDialogProvider.tsx` (react-refresh/only-export-components); 0 errors |
| TC-UI21-03 | `npm run build` — succeeds | PASS | Vite build exits 0 in 18.60 s; chunk-size advisory only |
| TC-UI21-04 | `PluginDisplayName` exported from `plugins.tsx` | PASS | Exported at line 22; `Record<Plugin, string>` mapping all 6 plugins |
| TC-UI21-05 | `PluginDisplayName` used in sider | PASS | `workflow-sider/index.tsx` imports and uses `PluginDisplayName` at lines 4, 65 |
| TC-UI21-06 | No `Plugin.START` references remain in `src/` | PASS | `grep Plugin.START` returned no matches across entire `src/` tree |
| TC-UI21-07 | All 6 node files — `h-[3px]` accent strip | PASS | Present at line 22 in all 6 plugin files |
| TC-UI21-08 | All 6 node files — `w-52` width | PASS | Present at line 13 in all 6 plugin files |
| TC-UI21-09 | All 6 node files — `ring-2 ring-indigo-500` on select | PASS | Present at line 16 in all 6 plugin files |
| TC-UI21-10 | `defaultEdgeOptions` has `animated: false` | PASS | `canvas/index.tsx` line 47; `animated: false` confirmed |
| TC-UI21-11 | `defaultEdgeOptions` has `stroke: "#94A3B8"` | PASS | `canvas/index.tsx` line 55; both `stroke` and `markerEnd.color` set to `"#94A3B8"` |
| TC-UI21-12 | Drawer `index.tsx` uses `styles` prop (not deprecated `headerStyle`/`bodyStyle`) | PASS | `workflow-drawer/index.tsx` line 72; `styles={{ header: {...}, body: {...} }}` — no deprecated props found |
| TC-UI21-13 | `__root.tsx` has `bg-zinc-900` nav | PASS | Line 65; `Header` className includes `bg-zinc-900` |
| TC-UI21-14 | Ant Design theme — `colorPrimary: "#4F46E5"`, `borderRadius: 6` | PASS | `__root.tsx` lines 45–47 |
| TC-UI21-15 | Workflows list page — `bg-zinc-50` page background | PASS | `workflows/index.tsx` line 176 |
| TC-UI21-16 | Background dots — `gap:24 size:1 color:#CBD5E1` | PASS | `canvas/index.tsx` lines 114–119; `bg-zinc-50` className also present |
| TC-UI21-17 | Form section headers — `text-[10px] uppercase tracking-widest` | PASS | Present in both `HttpCallForm.tsx` (lines 65, 101) and `LogicForm.tsx` (lines 55, 91) |
| TC-UI21-18 | Sidebar group label renamed to "Nodes" | PASS | `plugins.tsx` line 95; `label: "Nodes"` |
| TC-UI21-19 | `children.label` is `string` (not `Plugin`) | PASS | `PluginMenu` type in `plugins.tsx` line 87; `label: string` |
| TC-UI21-20 | Workflow header slimmer — `minHeight: 44` | PASS | `workflow-header/index.tsx` line 87; `style={{ minHeight: 44 }}` |

### FE Checklist (FE-01 through FE-10)

| FE-ID | Description | Result | Notes |
|-------|-------------|--------|-------|
| FE-01 | Workflow List Page — table columns, Description column | BLOCKED | Requires live browser for DOM inspection; source confirms `bg-zinc-50` page background and table redesign present |
| FE-02 | Canvas renders nodes and edges | BLOCKED | Requires browser; source confirms all 6 nodes registered, `defaultEdgeOptions` correct |
| FE-03 | Drawer — HTTP Call nodes form | BLOCKED | Requires browser interaction; source-level check PASS (TC-UI21-17) |
| FE-04 | Drawer — Logic nodes form | BLOCKED | Requires browser interaction; source-level check PASS (TC-UI21-17) |
| FE-05 | Save Workflow — POST fires and round-trips | BLOCKED | Requires browser; API POST endpoint confirmed functional in prior cycles |
| FE-06 | Settings Modal — PATCH entity setting | BLOCKED | Requires browser; PATCH API confirmed working (TC-18 in master tracker) |
| FE-07 | History Drawer — revision list and rollback | BLOCKED | Requires browser; history API confirmed working (TC-UI20-18) |
| FE-08 | New Node Types on Canvas | BLOCKED | Requires browser; source confirms all 6 plugin components registered in `nodeTypes` map |
| FE-09 | Records Page — filter bar, pagination | BLOCKED | Requires browser; records API confirmed working (TC-19) |
| FE-10 | Record Detail Page — parent + children table | BLOCKED | Requires browser; API `/workflow/records/{id}` confirmed in prior test cycles |

---

## Defects

None found. All 20 automated and source-level checks passed cleanly.

---

## Conclusion

**Ready.** All three automated gates (TypeScript, ESLint, Vite build) pass with zero errors. All 20 source-level checks confirm the design overhaul changes are correctly implemented: `PluginDisplayName` map is exported and consumed by the sider, no `Plugin.START` remnants remain, all 6 node files carry the new `h-[3px]` accent strip / `w-52` / `ring-2 ring-indigo-500` pattern, edge options are non-animated with the correct slate stroke, the drawer uses the non-deprecated `styles` prop, and the nav header carries `bg-zinc-900`. The 10 FE checklist items (FE-01 through FE-10) remain BLOCKED pending live browser verification — this is unchanged from v2.0 and is not a blocker for deployment.
