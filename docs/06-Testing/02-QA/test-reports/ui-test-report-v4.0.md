# UI Test Report v4.0 — Node Editor Cross-Audience Clarity

**Feature:** Node editor cross-audience clarity — subtitles + developer tooltips (US-31)  
**Commit:** TBD (see workflow-ui main after merge)  
**Date:** 2026-04-06  
**Tester:** QA Agent (Claude Sonnet 4.6)  
**Test Doc:** `test-doc-v4.0.md`

---

## Source Verification

| TC-ID | Check | Result | Evidence |
|-------|-------|--------|----------|
| TC-31-L01 | Subtitle "The name shown on the canvas…" in HttpCallForm | ✅ PASS | `HttpCallForm.tsx:62` — `subtitle="The name shown on the canvas. What is this step called?"` |
| TC-31-L02 | Subtitle "Run only when…" in HttpCallForm | ✅ PASS | `HttpCallForm.tsx:77` — subtitle on Rules NodeSection |
| TC-31-L03 | Subtitle "What the system does…" in HttpCallForm | ✅ PASS | `HttpCallForm.tsx:114` — subtitle on Action NodeSection |
| TC-31-L04 | All three subtitles present in LogicForm | ✅ PASS | `LogicForm.tsx:52,67,104` — identical subtitles |
| TC-31-L05 | InfoCircleOutlined icon rendered when headingTooltip set | ✅ PASS | `NodeSection.tsx:37-41` — conditional render of Tooltip + InfoCircleOutlined |
| TC-31-L06 | Tooltip Node Description → `plugin.description` | ✅ PASS | `headingTooltip="Persisted as: plugin.description"` in both forms |
| TC-31-L07 | Tooltip Rules → `plugin.ruleList` | ✅ PASS | `headingTooltip="Persisted as: plugin.ruleList[].key + ruleList[].remark"` |
| TC-31-L08 | Tooltip Action → `plugin.action` | ✅ PASS | `headingTooltip="Persisted as: plugin.action (type, provider, httpRequest*, elseLogic, …)"` |
| TC-31-L09 | Subtitle styling `text-xs text-zinc-400` | ✅ PASS | `NodeSection.tsx:44` — `className="text-xs text-zinc-400 mb-3"` |
| TC-31-R01 | NodeSection without props still renders (backward-compat) | ✅ PASS | Both `subtitle` and `headingTooltip` are optional (`?`) — no-op when absent |
| TC-31-R02 | `Typography` import removed (no unused imports) | ✅ PASS | Grep confirms `Typography` removed from both form imports |
| TC-31-R03 | All existing form fields still present | ✅ PASS | No field names or form structure changed |

**Source checks: 12/12 PASS**

---

## Browser Tests — UAT Required

| TC-ID | Check | Status |
|-------|-------|--------|
| TC-31-L10 | Mobile ≤375px no layout breakage | Browser TBD |
| TC-31-Q01–Q05 | Section isolation save/load | Browser TBD |

---

## UAT Script for Human

Open **https://workflow-ui-gamma.vercel.app/workflows/ANZ_PAYMENT_NOTIFY** — hard refresh (`Ctrl+Shift+R`) — click any node on the canvas:

| Step | Action | What to check |
|------|--------|---------------|
| 1 | Open drawer | Under "NODE DESCRIPTION" heading: grey subtitle "The name shown on the canvas. What is this step called?" |
| 2 | Look at "RULES" heading | Grey subtitle "Run only when…" visible below heading |
| 3 | Look at "ACTION" heading | Grey subtitle "What the system does when this step runs." visible |
| 4 | Hover the ⓘ icon next to "NODE DESCRIPTION" | Tooltip shows: "Persisted as: plugin.description" |
| 5 | Hover the ⓘ icon next to "RULES" | Tooltip shows: "Persisted as: plugin.ruleList[].key + ruleList[].remark" |
| 6 | Hover the ⓘ icon next to "ACTION" | Tooltip shows: "Persisted as: plugin.action (…)" |
| 7 | Change Step Name only → Save → reopen node | Only name changed; rules and action fields unchanged |

---

*End of UI Test Report v4.0 — Source checks PASS; Browser UAT pending human execution.*
