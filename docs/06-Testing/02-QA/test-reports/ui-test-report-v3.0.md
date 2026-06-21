# UI Test Report v3.0 — Artboard Node Editor: Three-Panel Layout + JSON Formatting

**Feature:** Artboard node editor 3-panel layout + smart JSON formatting (US-30)  
**Commit:** c5fb9d1 (`feat(node-editor): three-panel layout + smart JSON formatting (US-30)`)  
**Date:** 2026-04-06  
**Tester:** QA Agent (Claude Sonnet 4.6)  
**Branch:** `claude/complete-todo-tasks-QIAHA`  
**Repo:** `workflow-ui`  
**Test Doc:** `test-doc-v3.0.md`

---

## Summary

Three-panel layout and smart JSON formatting implemented across `HttpCallForm` and `LogicForm`. All source-level checks pass. Layout/visual tests require real-browser verification (marked **Browser TBD** — see section 3).

---

## 1. Layout Test Cases

| TC-ID | Check | Method | Result | Notes |
|-------|-------|--------|--------|-------|
| TC-30-L01 | Three panels present and ordered (HttpCallForm): Node Description → Rules → Action | Source | ✅ PASS | Lines 59–177: NodeSection titles `"Node Description"`, `"Rules"`, `"Action"` in order |
| TC-30-L02 | Three panels present and ordered (LogicForm): Node Description → Rules → Action | Source | ✅ PASS | Lines 49–127: Same NodeSection order |
| TC-30-L03 | Headings consistent across node types (same component, same CSS class) | Source | ✅ PASS | Both forms import and use `NodeSection` — identical heading markup (`text-[10px] font-semibold text-zinc-500 uppercase tracking-widest`) |
| TC-30-L04 | Mobile layout: no mobile-breaking CSS in NodeSection | Source | ✅ PASS | `NodeSection` uses only `mb-4`, `border`, `rounded-lg`, `p-3`, `bg-zinc-50` — all Tailwind utilities that work at any viewport width; no fixed-width or overflow-x properties |

**Note:** TC-30-L01/L02/L04 also require real-browser visual verification on a mobile viewport (≤375px). Tagged as **Browser TBD**.

---

## 2. JSON Formatting Test Cases

Verified by tracing the `useJsonFormat` hook logic (`useJsonFormat.ts`):

```
blur → raw = e.target.value
     → if empty: return (no-op)               → TC-30-J08 ✅
     → try JSON.parse(raw)
          success → JSON.stringify(parsed, null, 2)
                    if formatted !== raw:
                      form.setFieldValue → onValuesChange  → TC-30-J01..J04, TC-30-R01 ✅
                    else: no-op (already formatted)        → TC-30-J09 ✅
          catch  → return (no-op, value unchanged)         → TC-30-J05, TC-30-J06*, TC-30-J07 ✅
```

*TC-30-J06: `<<<$.path>>>` inside a valid JSON string value — `JSON.parse` succeeds (template is inside a quoted string), so the field is pretty-printed with the template preserved.  
TC-30-J07: bare `<<<$.someField>>>` at top level — `JSON.parse` throws → no-op ✅

| TC-ID | Scenario | Result | Evidence |
|-------|----------|--------|----------|
| TC-30-J01 | Valid JSON pretty-printed on blur (httpRequestHeaders) | ✅ PASS | `formatOnBlur("httpRequestHeaders")` on TextArea `onBlur`; `JSON.stringify(parsed, null, 2)` applied |
| TC-30-J02 | Valid JSON pretty-printed on blur (httpRequestBody) | ✅ PASS | `formatOnBlur("httpRequestBody")` on TextArea `onBlur` |
| TC-30-J03 | Valid JSON with template `<<<$.id>>>` pretty-printed (trackingNumberSchemaInHttpResponse) | ✅ PASS | Template inside string → `JSON.parse` succeeds → pretty-printed; template preserved in string value |
| TC-30-J04 | Valid JSON pretty-printed on blur (elseLogic — LogicForm) | ✅ PASS | `formatOnBlur("elseLogic")` on TextArea `onBlur` |
| TC-30-J05 | Invalid JSON left unchanged (no corruption) | ✅ PASS | `try/catch` — malformed input causes parse to throw → `return` (no `setFieldValue`) |
| TC-30-J06 | `<<<` template in JSON string value — preserved | ✅ PASS | Template inside `"..."` is valid JSON string → parsed → `JSON.stringify` re-emits template verbatim |
| TC-30-J07 | Bare `<<<$.field>>>` (top-level, not valid JSON) — left unchanged | ✅ PASS | `JSON.parse` throws → catch → no-op |
| TC-30-J08 | Empty field — no action on blur | ✅ PASS | `if (!raw.trim()) return` — first guard in handler |
| TC-30-J09 | Already-formatted JSON — no second change, `setFieldValue` not called | ✅ PASS | `if (formatted !== raw)` guard — no-op when value is already pretty-printed |
| TC-30-J10 | Non-JSON fields (description, URL fields, remark) — NO `onBlur` formatting | ✅ PASS | `grep onBlur HttpCallForm.tsx` → 3 matches (headers, body, schema only); `grep onBlur LogicForm.tsx` → 1 match (elseLogic only); no onBlur on description/URL/remark |

---

## 3. Regression Test Cases

| TC-ID | Check | Method | Result | Notes |
|-------|-------|--------|--------|-------|
| TC-30-R01 | `onValuesChange` propagates after JSON formatting | Source | ✅ PASS | `useJsonFormat` calls `onValuesChange?.(form.getFieldsValue())` after `setFieldValue` |
| TC-30-R02 | All existing HttpCallForm fields present | Source | ✅ PASS | All 11 field names confirmed: `description`, `ruleList`, `provider`, `type`, `remark`, `httpRequestMethod`, `httpRequestUrlWithQueryParameter`, `internalHttpRequestUrlWithQueryParameter`, `httpRequestHeaders`, `httpRequestBody`, `trackingNumberSchemaInHttpResponse` |
| TC-30-R02 | All existing LogicForm fields present | Source | ✅ PASS | All 6 field names confirmed: `description`, `ruleList`, `provider`, `type`, `remark`, `elseLogic` |
| TC-30-R03 | `ruleList` add/remove (Form.List) unchanged | Source | ✅ PASS | `Form.List name="ruleList"` with `add`/`remove` callbacks unchanged; wrapped in `NodeSection` only — no logic change |
| TC-30-R04 | Drawer open/close behaviour unchanged | Source | ✅ PASS | `workflow-drawer/index.tsx` not modified; drawer lifecycle and node-selection routing unchanged |

---

## 4. Build / Static Analysis

| Check | Result | Notes |
|-------|--------|-------|
| New files exist | ✅ PASS | `NodeSection.tsx`, `useJsonFormat.ts` created at correct paths |
| Imports resolve correctly | ✅ PASS | Both forms import `NodeSection` from `"../NodeSection"` and `useJsonFormat` from `"../useJsonFormat"` — paths match actual file locations |
| No `onBlur` on non-JSON fields | ✅ PASS | Grep confirms 3 onBlur in HttpCallForm (json fields only) + 1 in LogicForm (elseLogic only) |
| `workflow-drawer/index.tsx` unchanged | ✅ PASS | No modifications — drawer wrapper, form routing, and mobile width unchanged |
| `npm install` completed | ✅ PASS | Background install completed exit code 0 after commit |

---

## 5. Items Requiring Browser Verification (Browser TBD)

These checks cannot be completed by source inspection alone. Must be verified in a real browser after deployment to Vercel preview or local dev server:

| TC-ID | Item |
|-------|------|
| TC-30-L01/L02 | Panels visually distinct — confirm headings, borders, and spacing render correctly in browser |
| TC-30-L04 | Mobile viewport (≤375px) — confirm no horizontal overflow or layout breakage |
| TC-30-J01 | Real blur interaction — confirm formatting triggers on actual user blur event |
| TC-30-R03 | Canvas sync — confirm ruleList add/remove propagates to canvas node data |

---

## Files Changed

| File | Change |
|------|--------|
| `src/routes/workflows/-components/workflow-drawer/NodeSection.tsx` | New — shared 3-panel section wrapper |
| `src/routes/workflows/-components/workflow-drawer/useJsonFormat.ts` | New — blur-triggered JSON format hook |
| `src/routes/workflows/-components/workflow-drawer/forms/HttpCallForm.tsx` | Updated — 3 panels + `onBlur` on 3 JSON fields |
| `src/routes/workflows/-components/workflow-drawer/forms/LogicForm.tsx` | Updated — 3 panels + `onBlur` on `elseLogic` |

---

## Defects

None found.

---

## Verdict

**Source checks: ALL PASS (18/18). Browser visual verification: 4 items TBD (TC-30-L01, L02, L04, J01, R03 — require real-browser run on dev/staging).**

Human approval required before merging to `main`.
