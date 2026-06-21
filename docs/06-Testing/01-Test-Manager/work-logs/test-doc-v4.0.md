# Test Doc — Node Editor Cross-Audience Clarity

**Label**: `TODO-node-editor-cross-audience-clarity`  
**Version**: 4.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc v4.0 + Arch Doc v4.0

---

## 1. Layout / Content Tests

| TC-ID | Check | Method | Expected |
|-------|-------|--------|----------|
| TC-31-L01 | Subtitle present under "Node Description" (HttpCallForm) | Browser | "The name shown on the canvas. What is this step called?" visible in `text-xs text-zinc-400` below heading |
| TC-31-L02 | Subtitle present under "Rules" (HttpCallForm) | Browser | "Run only when…" subtitle visible |
| TC-31-L03 | Subtitle present under "Action" (HttpCallForm) | Browser | "What the system does when this step runs." visible |
| TC-31-L04 | All three subtitles present in LogicForm | Browser | Same subtitles as HttpCallForm for the same panels |
| TC-31-L05 | Info icon (ⓘ) visible next to each panel heading | Browser | Small icon next to heading; hover shows tooltip |
| TC-31-L06 | Tooltip "Node Description" → shows `plugin.description` | Browser | Hover icon → tooltip text contains "plugin.description" |
| TC-31-L07 | Tooltip "Rules" → shows `plugin.ruleList` | Browser | Tooltip text contains "ruleList" |
| TC-31-L08 | Tooltip "Action" → shows `plugin.action` | Browser | Tooltip text contains "plugin.action" |
| TC-31-L09 | Subtitle styling: lighter than heading | Browser | Subtitle visually secondary to the bold heading |
| TC-31-L10 | Mobile (≤375px): no layout breakage with subtitles | Browser | Subtitles wrap correctly; no horizontal overflow |

---

## 2. Section Isolation Tests (QA regression)

| TC-ID | Scenario | Steps | Expected |
|-------|----------|-------|----------|
| TC-31-Q01 | Change Description only, save, reload | 1. Change Step Name. 2. Save. 3. Reopen node. | Only description changed; ruleList and action unchanged |
| TC-31-Q02 | Change Rules only, save, reload | 1. Add a rule. 2. Save. 3. Reopen node. | Rule added; description and action unchanged |
| TC-31-Q03 | Change Action only, save, reload | 1. Change External URL. 2. Save. 3. Reopen node. | URL changed; description and ruleList unchanged |
| TC-31-Q04 | Empty ruleList saves correctly | 1. Remove all rules. 2. Save. 3. Reopen. | ruleList is empty; no error |
| TC-31-Q05 | Multi-rule list saves correctly | 1. Add 3 rules. 2. Save. 3. Reopen. | All 3 rules present in correct order |

---

## 3. Regression Tests

| TC-ID | Check | Expected |
|-------|-------|----------|
| TC-31-R01 | Existing NodeSection usages without subtitle/headingTooltip still render correctly | No subtitle or icon shown; no error |
| TC-31-R02 | All existing form fields still present and functional | Unchanged |
| TC-31-R03 | `onValuesChange` still fires correctly after subtitle addition | Canvas sync unaffected |

---

## UAT Script for Human

Open https://workflow-ui-gamma.vercel.app/workflows/ANZ_PAYMENT_NOTIFY, click any node:

1. Confirm **three panel headings** visible: "NODE DESCRIPTION", "RULES", "ACTION"
2. Confirm **subtitle text** appears under each heading in lighter grey
3. Hover the small **ⓘ icon** next to each heading — confirm tooltip shows field name
4. For TC-31-Q01: change the Step Name field → Save → reopen node → confirm only the name changed

---

*End of Test Doc v4.0 — Draft, awaiting approval.*
