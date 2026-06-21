# Test Doc — Artboard Node Editor: Three-Panel Layout + Smart Text Formatting

**Label**: `TODO-artboard-node-editor-3panel-json-format`  
**Version**: 3.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc v3.0 (US-30) + Arch Doc v3.0

---

## 1. Scope

Test coverage for the reworked node editor drawer:
- Three-panel layout in `HttpCallForm` and `LogicForm`
- Smart JSON formatting on blur in JSON-bearing text fields
- Regression: existing form functionality unchanged

---

## 2. Layout Test Cases

### TC-30-L01 — Three panels visible and ordered correctly (HttpCallForm)

| Field | Value |
|---|---|
| **ID** | TC-30-L01 |
| **Precondition** | Select an HTTP-call node (CONSUMER, MESSAGE, or CONSUMER_WITHOUT_ERROR) on the canvas. |
| **Steps** | Open the node editor drawer. Observe the layout. |
| **Expected result** | Three clearly distinct sections appear in this order: (1) **Node Description**, (2) **Rules**, (3) **Action**. Each has a visible section heading. |
| **Pass criteria** | All three headings present. Correct order. Visual separation between sections (border, background, or spacing). |
| **Priority** | P1 |

---

### TC-30-L02 — Three panels visible and ordered correctly (LogicForm)

| Field | Value |
|---|---|
| **ID** | TC-30-L02 |
| **Precondition** | Select a Logic node (IF_ELSE, FUNCTION, or FUNCTION_V3). |
| **Steps** | Open the node editor drawer. Observe the layout. |
| **Expected result** | Same three-panel structure as TC-30-L01. Section headings present. Correct order. |
| **Priority** | P1 |

---

### TC-30-L03 — Section headings are consistent across node types

| Field | Value |
|---|---|
| **ID** | TC-30-L03 |
| **Steps** | Open HttpCallForm. Note the heading text and styling. Close. Open LogicForm. Compare. |
| **Expected result** | Heading text and visual style are identical for panels 1 (Node Description) and 2 (Rules). Panel 3 heading may differ (Action vs. HTTP Configuration vs. Logic Configuration — consistent with the node type). |
| **Priority** | P2 |

---

### TC-30-L04 — Mobile layout: three panels render correctly on small viewport

| Field | Value |
|---|---|
| **ID** | TC-30-L04 |
| **Precondition** | DevTools mobile emulation (e.g. 375px width) or real mobile device. |
| **Steps** | Select a node. Open drawer (full-width). Scroll through panels. |
| **Expected result** | All three panels are visible. No layout breakage. Section headings readable. No horizontal overflow. |
| **Priority** | P2 |

---

## 3. JSON Formatting Test Cases

### TC-30-J01 — Valid JSON is pretty-printed on blur (httpRequestHeaders)

| Field | Value |
|---|---|
| **ID** | TC-30-J01 |
| **Precondition** | HTTP-call node editor open. `httpRequestHeaders` field empty. |
| **Steps** | 1. Type `{"Content-Type":"application/json","X-Custom":"value"}` (minified). 2. Click outside the field (blur). |
| **Expected result** | Field value becomes: `{\n  "Content-Type": "application/json",\n  "X-Custom": "value"\n}` (2-space indent, prettified). |
| **Priority** | P1 |

---

### TC-30-J02 — Valid JSON is pretty-printed on blur (httpRequestBody)

| Field | Value |
|---|---|
| **ID** | TC-30-J02 |
| **Steps** | 1. Enter `{"customerId":"123","amount":99.5}` in `httpRequestBody`. 2. Blur. |
| **Expected result** | Pretty-printed JSON with 2-space indent. |
| **Priority** | P1 |

---

### TC-30-J03 — Valid JSON is pretty-printed on blur (trackingNumberSchemaInHttpResponse)

| Field | Value |
|---|---|
| **ID** | TC-30-J03 |
| **Steps** | 1. Enter `{"ref":"<<<$.data.id>>>"}` in `trackingNumberSchemaInHttpResponse`. 2. Blur. |
| **Expected result** | Field value becomes `{\n  "ref": "<<<$.data.id>>>"\n}`. The `<<<` template variable is preserved inside the string value. |
| **Priority** | P1 |

---

### TC-30-J04 — Valid JSON is pretty-printed on blur (elseLogic — LogicForm)

| Field | Value |
|---|---|
| **ID** | TC-30-J04 |
| **Precondition** | Logic node editor open. |
| **Steps** | 1. Enter `{"messageInformation":{"channel":"SMS"}}` in `elseLogic`. 2. Blur. |
| **Expected result** | Pretty-printed: `{\n  "messageInformation": {\n    "channel": "SMS"\n  }\n}`. |
| **Priority** | P1 |

---

### TC-30-J05 — Invalid JSON is left unchanged (no corruption)

| Field | Value |
|---|---|
| **ID** | TC-30-J05 |
| **Steps** | 1. Enter `{broken json` in `httpRequestHeaders`. 2. Blur. |
| **Expected result** | Field retains `{broken json` — exactly as entered. No error message. No data change. |
| **Pass criteria** | User input preserved. No error UI. |
| **Priority** | P1 |

---

### TC-30-J06 — `<<<` template strings do not corrupt on blur

| Field | Value |
|---|---|
| **ID** | TC-30-J06 |
| **Steps** | 1. Enter `{"customerId": "<<<$.messageInformation.customerId>>>"}` in `httpRequestBody`. 2. Blur. |
| **Expected result** | Field is pretty-printed (valid JSON). Template strings inside string values are preserved verbatim. |
| **Priority** | P1 |

---

### TC-30-J07 — Top-level template string (invalid JSON) is left unchanged

| Field | Value |
|---|---|
| **ID** | TC-30-J07 |
| **Steps** | 1. Enter `<<<$.someField>>>` (not valid JSON) in `httpRequestBody`. 2. Blur. |
| **Expected result** | Field value unchanged — `<<<$.someField>>>`. No error. |
| **Priority** | P1 |

---

### TC-30-J08 — Empty field: no action on blur

| Field | Value |
|---|---|
| **ID** | TC-30-J08 |
| **Steps** | 1. Leave `httpRequestHeaders` empty. 2. Click in and out (blur with no input). |
| **Expected result** | Field remains empty. No error. No change to form state. |
| **Priority** | P2 |

---

### TC-30-J09 — Already-formatted JSON: no change on second blur

| Field | Value |
|---|---|
| **ID** | TC-30-J09 |
| **Steps** | 1. Blur a JSON field once (pretty-printed). 2. Click inside and blur again without editing. |
| **Expected result** | No change. `onValuesChange` is NOT triggered (or triggers with the same value). Form state stable. |
| **Priority** | P2 |

---

### TC-30-J10 — Non-JSON fields do NOT format on blur

| Field | Value |
|---|---|
| **ID** | TC-30-J10 |
| **Steps** | 1. Enter `{"not": "expected"}` in the `description` field. 2. Blur. |
| **Expected result** | `description` field value is unchanged — no JSON formatting applied. |
| **Priority** | P2 |

---

## 4. Regression Test Cases

### TC-30-R01 — `onValuesChange` propagates after JSON formatting

| Field | Value |
|---|---|
| **ID** | TC-30-R01 |
| **Steps** | 1. Open node editor. 2. Enter valid JSON in `httpRequestHeaders`. 3. Blur. 4. Save workflow. |
| **Expected result** | The formatted JSON string is saved (not the original minified version). On reload, the field shows the formatted string. |
| **Priority** | P1 |

---

### TC-30-R02 — All existing fields still present and functional

| Field | Value |
|---|---|
| **ID** | TC-30-R02 |
| **Steps** | Open both `HttpCallForm` and `LogicForm`. Verify all fields listed in their respective `FormValues` types are present and editable. |
| **Expected result** | All fields present: `description`, `ruleList`, `provider`, `type`, `remark`, `httpRequestMethod`, `httpRequestUrlWithQueryParameter`, `internalHttpRequestUrlWithQueryParameter`, `httpRequestHeaders`, `httpRequestBody`, `trackingNumberSchemaInHttpResponse` (HTTP); `description`, `ruleList`, `provider`, `type`, `remark`, `elseLogic` (Logic). |
| **Priority** | P1 |

---

### TC-30-R03 — ruleList add/remove still works

| Field | Value |
|---|---|
| **ID** | TC-30-R03 |
| **Steps** | 1. Open node with existing rules. 2. Add a rule. 3. Remove a rule. |
| **Expected result** | Rule list updates correctly. Canvas reflects change after form fires `onValuesChange`. |
| **Priority** | P1 |

---

### TC-30-R04 — Drawer open/close unchanged

| Field | Value |
|---|---|
| **ID** | TC-30-R04 |
| **Steps** | 1. Click a node to open drawer. 2. Click X / outside to close. 3. Click a different node. |
| **Expected result** | Drawer opens and closes correctly. Form re-initialises with the newly selected node's data. |
| **Priority** | P1 |

---

## 5. Test Environment Notes

- All layout tests (TC-30-L*) must be run in a real browser (Chrome or Firefox).
- JSON formatting tests (TC-30-J*) may be covered by unit tests using React Testing Library + `fireEvent.blur`.
- TC-30-R01 requires a functional backend (or mock API) to verify save/reload round-trip.

---

*End of Test Doc v3.0 — Artboard node editor 3-panel + JSON format — Draft, awaiting approval.*
