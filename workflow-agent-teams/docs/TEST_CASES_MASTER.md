# Workflow System — Master Test Case Tracker

**Last updated:** 2026-04-06  
**Environment:** Local (macOS, JDK 21, Node 20)  
**Services:**
- `workflow-operation-api` — port 8080 (Spring Boot 4.0.3)
- `workflow-online-api` — port 8081 (Spring Boot 4.0.3)
- `workflow-ui` — port 5173 (React/Vite/TypeScript)
- Database: Neon PostgreSQL (shared)

---

## Quick Summary

| Group | Total | PASS | FAIL | OBS |
|-------|-------|------|------|-----|
| TC-01 List/Search | 2 | 2 | — | — |
| TC-02 Create App | 2 | 2 | — | — |
| TC-03 Delete App | 3 | 3 | — | — |
| TC-10 AutoCopy | 5 | 5 | — | — |
| TC-11 History | 1 | 1 | — | — |
| TC-12 Async Exec | 1 | 1 | — | — |
| TC-13 Sync Exec | 1 | 1 | — | — |
| TC-14 Full Pipeline Sync | 1 | 1 | — | — |
| TC-15 Full Pipeline Async | 1 | 1 | — | — |
| TC-16 Duplicate Detection | 1 | 1 | — | — |
| TC-17 UI Accessible | 1 | 1 | — | — |
| TC-18 PATCH Entity Setting | 5 | 5 | — | — |
| TC-19 Records Filters | 3 | 3 | — | — |
| TC-N Negative Cases | 10 | 10 | — | — |
| TC-COPY Copy Feature | 7 | 7 | — | — |
| TC-ANZ All-Plugin E2E | 7 | 6 | — | 1 |
| TC-UI20 Canvas Overhaul v2.0 | 20 | 20 | — | — |
| TC-UI21 Design Overhaul v2.1 | 20 | 20 | — | — |
| TC-UI22 Mobile Responsive v2.2 | 25 | 25 | — | — |
| **Unit Tests** | **117** | **117** | **—** | **—** |
| **TOTAL** | **226** | **225** | **—** | **1** |

---

## TC-01: List & Search Applications

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-01-1 | List all applications | `GET /api/workflow/entity-setting` | 200, all apps returned | ✅ PASS |
| TC-01-2 | Fuzzy search by name | `GET /api/workflow/entity-setting?applicationName=TEST_APP_A` | 200, 1 result | ✅ PASS |

---

## TC-02: Create / Upsert Application

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-02-1 | Create new application | `POST /api/workflow?applicationName=NEW_APP` | 200, entity created, pluginList persisted | ✅ PASS |
| TC-02-2 | Re-POST same app updates workflow | `POST /api/workflow?applicationName=TEST_APP_A` | 200, idempotent upsert | ✅ PASS |

---

## TC-03: Delete Application

| TC-ID | Description | Endpoint | Expected | Result | Notes |
|-------|-------------|----------|----------|--------|-------|
| TC-03-1 | Delete app with existing records | `DELETE /api/workflow?applicationName=TEST_APP_DELETE` | 200, entity deleted, records retained | ✅ PASS | OP-03: WF-409-201 guard removed |
| TC-03-2 | Deleted app's records still queryable | `GET /api/workflow/records?applicationName=TEST_APP_DELETE` | 200, orphaned record visible | ✅ PASS | JPA Specification fix required |
| TC-03-3 | Delete non-existent app | `DELETE /api/workflow?applicationName=DOES_NOT_EXIST` | 400 WF-400-101 | ✅ PASS | |

---

## TC-10: AutoCopy

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-10-1 | Copy A → B | `POST /api/workflow/autoCopy?fromApplicationName=TEST_APP_A&toApplicationName=TEST_APP_B` | 200, workflow content copied | ✅ PASS |
| TC-10-2 | Entity settings metadata copied | (verify after TC-10-1) | asyncMode/retry/tracking fields match source | ✅ PASS |
| TC-10-3 | Workflow pluginList matches source | (verify after TC-10-1) | pluginList count equal | ✅ PASS |
| TC-10-4 | Same source and target | `POST /api/workflow/autoCopy?from=TEST_APP_A&to=TEST_APP_A` | 400 WF-400-301 | ✅ PASS |
| TC-10-5 | Non-existent source | `POST /api/workflow/autoCopy?from=NON_EXISTENT&to=TEST_APP_B` | 400 WF-400-302 | ✅ PASS |

---

## TC-11: Revision History

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-11-1 | Get history for app | `GET /api/workflow/entity-setting/history?applicationName=TEST_APP_A` | 200, ≥1 revision with revisionNumber/revisionType/revisionDate | ✅ PASS |

---

## TC-12: Async Workflow Execution

| TC-ID | Description | Endpoint | Expected | Result | Notes |
|-------|-------------|----------|----------|--------|-------|
| TC-12-1 | Fire-and-forget async dispatch | `POST /api/workflow?applicationName=TEST_APP_ASYNC&confirmationNumber=TC12-CONF-001` | 200 returned immediately, record created asynchronously | ✅ PASS | `asyncMode=true`; record GI_FAIL due to empty rule key in test data |

---

## TC-13: Sync Workflow Execution

| TC-ID | Description | Endpoint | Expected | Result | Notes |
|-------|-------------|----------|----------|--------|-------|
| TC-13-1 | Synchronous dispatch blocks until done | `POST /api/workflow?applicationName=TEST_APP_SYNC&confirmationNumber=TC13-CONF-001` | 200 after pipeline completes | ✅ PASS | `asyncMode=false` |

---

## TC-14: Full Execution Pipeline — Sync (GI_SUCCESS)

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-14-1 | CONSUMER step executes, record GI_SUCCESS | `POST /api/workflow?applicationName=TEST_APP_A&confirmationNumber=TC14-CONF-001` | 200, record id=5 overallStatus=GI_SUCCESS | ✅ PASS |

Payload: `{"messageInformation":[{"customerId":"C001","amount":"100"}]}`  
JSONPath rule `$.messageInformation.[?(@.customerId=~/.+?/)]` matched; CONSUMER HTTP GET to httpbin.org/get succeeded.

---

## TC-15: Full Execution Pipeline — Async (GI_SUCCESS)

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-15-1 | Async dispatch returns 200 immediately, record GI_SUCCESS after poll | `POST /api/workflow?applicationName=TEST_APP_A&confirmationNumber=TC15-CONF-001` | 200 immediate, GI_SUCCESS within 3s | ✅ PASS |

---

## TC-16: Duplicate Correlation ID Detection

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-16-1 | Re-use a previously used correlation ID | Same corrId as TC-14 | 400 M0002 | ✅ PASS |

Error: `"Duplicate records has been found per request correlation ID tc14-sync-001"`

---

## TC-17: UI Accessibility

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-17-1 | Frontend served correctly | `GET http://localhost:5173` | 200, HTML/React app | ✅ PASS |

---

## TC-18: PATCH Entity Setting

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-18-1 | Disable app | `PATCH /api/workflow/entity-setting?applicationName=TEST_APP_A` body `{"enabled":false}` | 200, enabled=false | ✅ PASS |
| TC-18-2 | Re-enable app | Same endpoint body `{"enabled":true}` | 200, enabled=true | ✅ PASS |
| TC-18-3 | Set asyncMode=true | body `{"asyncMode":true}` | 200, asyncMode=true, new Envers revision | ✅ PASS |
| TC-18-4 | Set retryProperties | body `{"retryProperties":"{\"maxAttempts\":3,...}"}` | 200, field updated | ✅ PASS |
| TC-18-5 | Workflow field not overwritten by PATCH | PATCH then GET workflow | workflow base64 unchanged | ✅ PASS |

---

## TC-19: Execution Records

| TC-ID | Description | Endpoint | Expected | Result |
|-------|-------------|----------|----------|--------|
| TC-19-1 | Multi-filter query with sort | `GET /api/workflow/records?applicationName=TEST_APP_A&overallStatus=GI_SUCCESS&sort=createdDateTime,desc` | 200, 3 records sorted desc | ✅ PASS |
| TC-19-2 | Single record detail with children | `GET /api/workflow/records/5` | 200, `{record, children:[]}` structure | ✅ PASS |
| TC-19-3 | Pagination | `GET /api/workflow/records?applicationName=TEST_APP_A&page=1&size=1` | page=1, 1 element, total=3 | ✅ PASS |

---

## TC-N: Negative / Edge Cases

| TC-ID | Scenario | Service | Expected | Result |
|-------|----------|---------|----------|--------|
| TC-N-01 | Delete non-existent app | operation | 400 WF-400-101 | ✅ PASS |
| TC-N-02 | GET workflow non-existent app | operation | 400 WF-400-101 | ✅ PASS |
| TC-N-06 | Records filter with future date (2030) | operation | 0 results | ✅ PASS |
| TC-N-07 | Records for non-existent app | operation | 0 results | ✅ PASS |
| TC-N-08 | GET record by non-existent ID | operation | 404 WF-404-000 | ✅ PASS |
| TC-N-09 | Execute non-existent application | online | 400 M0001 | ✅ PASS |
| TC-N-10 | Missing X-Request-Correlation-Id header | online | 400 440000 | ✅ PASS |
| TC-N-11 | autoCopy source = target | operation | 400 WF-400-301 | ✅ PASS |
| TC-N-12 | autoCopy non-existent source | operation | 400 WF-400-302 | ✅ PASS |
| TC-N-13 | Records sorted descending | operation | ids in desc order | ✅ PASS |

---

## TC-COPY: Copy Workflow Feature (UI + API)

Feature delivered in: `workflow-ui` + `workflow-operation-api` (commit `1bbfc3d` + `69db2b2`)

| TC-ID | Description | Layer | Expected | Result | Notes |
|-------|-------------|-------|----------|--------|-------|
| TC-COPY-01 | Happy path — copy to new name | API + UI | 200, pluginList matches source | ✅ PASS | `TEST_APP_A → TEST_APP_A_COPY` |
| TC-COPY-02 | Same source and target | API | 400 WF-400-301 | ✅ PASS | Error surfaced in UI modal |
| TC-COPY-03 | Non-existent source | API | 400 WF-400-302 | ✅ PASS | |
| TC-COPY-04 | TypeScript type-check | UI | `npx tsc --noEmit` 0 errors | ✅ PASS | |
| TC-COPY-05 | ESLint | UI | `npm run lint` 0 errors | ✅ PASS | 1 pre-existing warning in unrelated file |
| TC-COPY-06 | Copy to already-existing target | API | 400 WF-400-303 | ✅ PASS | Initially observed as 200 (upsert); **changed to reject** per product decision |
| TC-COPY-07 | Target unchanged after rejected copy | API | target workflow intact | ✅ PASS | Verified after WF-400-303 fix |

**Product decision recorded:** Copying to an existing target must be rejected with 400 WF-400-303 (not silently overwrite).  
**Fix:** `WorkflowAutoCopyController` — changed `size() > 1` to `!isEmpty()`.

---

## TC-ANZ: All-Plugin E2E — ANZ_PAYMENT_NOTIFY

**Scenario:** Bank payment notification pipeline exercising all 6 plugin types in sequence.  
**Workflow config:** `workflow-agent-teams/docs/anz-payment-notify-workflow.json`  
**Request:** `POST /api/workflow?applicationName=ANZ_PAYMENT_NOTIFY&confirmationNumber=PAY-2026-001`  
**asyncMode:** false (synchronous)

```json
{
  "customerId": "C001",
  "segment": "PREMIUM",
  "accountNumber": "123456789012",
  "customerName": "John Smith",
  "phoneNumber": "+61412345678",
  "email": "john.smith@example.com",
  "amount": "250.00",
  "merchantName": "Woolworths"
}
```

| TC-ID | Plugin | Description | Expected | Result | Notes |
|-------|--------|-------------|----------|--------|-------|
| TC-ANZ-01 | CONSUMER | Fetch customer profile — GET httpbin.org/json | HTTP call made, execution continues | ✅ PASS | Mock CRM endpoint |
| TC-ANZ-02 | CONSUMERWITHOUTERROR | Fetch loyalty points — GET httpbin.org/status/503 | 503 swallowed, execution continues | ✅ PASS | Confirmed by pipeline not halting |
| TC-ANZ-03 | IFELSE | PREMIUM segment check — rule `segment == "PREMIUM"` | Rule matched, `notificationChannels=ALL` merged | ✅ PASS | |
| TC-ANZ-04 | FUNCTION_V2 | Format account — `Functions.formatAccountNumber("123456789012")` | `formattedAccountNumber=123-456XXXXXX` in payload | ✅ PASS | Java reflection via `com.workflow.common.utils.Functions` |
| TC-ANZ-05 | FUNCTION_V3 | Mask name — `Functions.formatName("John Smith")` | `maskedName=John S***` in payload | ✅ PASS | Java reflection |
| TC-ANZ-06 | MESSAGE | SMS dispatch — POST httpbin.org/post (MockSMS) | SM_SUCCESS, child record created | ✅ PASS | Record ID 9 |
| TC-ANZ-07 | MESSAGE | Email dispatch — POST httpbin.org/post (MockEmail) | SM_SUCCESS, child record created | ✅ PASS | Record ID 10 |

**Records created:**

| Record ID | Provider | Status | confirmationNumber |
|-----------|----------|--------|--------------------|
| 9 | MockSMS | SM_SUCCESS | PAY-2026-001 |
| 10 | MockEmail | SM_SUCCESS | PAY-2026-001 |

**Observation — Missing parent GI record:**  
The parent GI record (INITIATION → GI_SUCCESS) was not persisted to the DB (sequence jumped from ID 7 to 9). The outer `try-catch` in `WorkflowDispatchService.runDispatchPipeline` caught a GI-phase exception and allowed dispatch to proceed. Both MESSAGE steps succeeded. Investigation of the GI exception (likely FUNCTION reflection or `outputParameter` serialization edge case) is a recommended follow-up.

---

## Unit Tests

| Suite | Tests | Result |
|-------|-------|--------|
| Base64UtilTest | 8 | ✅ ALL PASS |
| ApiErrorCatalogTest | 4 | ✅ ALL PASS |
| GlobalExceptionHandlerTest | 7 | ✅ ALL PASS |
| WorkflowDeleteControllerTest | 4 | ✅ ALL PASS |
| WorkflowAutoCopyControllerTest | 4 | ✅ ALL PASS |
| WorkflowAutoCopyIntegrationTest | 7 | ✅ ALL PASS |
| WorkflowEdgeCaseIntegrationTest + other integration | ~83 | ✅ ALL PASS |
| **Total** | **117** | **✅ ALL PASS** |

Last run: `mvn test` — BUILD SUCCESS

---

## Bugs Found & Fixed During Testing

| # | Bug | Fix | TC that caught it |
|---|-----|-----|-------------------|
| 1 | `WorkflowAutoCopyController` used `consumes` instead of `produces` — bodyless POST caused 415/500 | Changed to `produces` | TC-10-1 |
| 2 | `WorkflowRecordController` JPQL nullable params caused PostgreSQL type-inference error | Rewrote with JPA Specification pattern | TC-03-2, TC-19-1 |
| 3 | `WorkflowDeleteController` — FK violation when `WorkflowReport` rows existed before delete | Added cascade delete of reports before entity delete | TC-03-1 |
| 4 | `WorkflowOnlineController` JCEKS keystore mismatch caused 500 on all online-api calls | Regenerated JCEKS keystore; `SecureData` fixed to `KeyStore.getInstance("JCEKS")` | TC-12-1 |
| 5 | Integration test `deleteReturns409WhenReportsExist` asserted removed 409 behaviour | Updated to assert 200 + entity deleted | TC-03-1 |
| 6 | `WorkflowAutoCopyController` allowed copying to existing target (upsert) | Changed `size() > 1` → `!isEmpty()` to return 400 WF-400-303 | TC-COPY-06 |

---

## TC-UI20: Canvas Overhaul v2.0 — commit fdfc086

**Scope:** START node removal, node visual redesign (6 plugins), drawer form improvements  
**Date:** 2026-04-06  Build: fdfc086 + 871a615

| TC-ID | Description | Layer | Expected | Result | Notes |
|-------|-------------|-------|----------|--------|-------|
| TC-UI20-01 | `npx tsc --noEmit` (project references) | UI | 0 errors | ✅ PASS | Root composite tsc exits clean |
| TC-UI20-02 | `npx tsc -p tsconfig.app.json --noEmit` | UI | 0 errors | ✅ PASS | Fixed commit 871a615 — `Plugin.START` call sites removed; 0 errors confirmed |
| TC-UI20-03 | `npm run lint` | UI | 0 errors | ✅ PASS | 1 pre-existing warning unrelated to this change |
| TC-UI20-04 | `npm run build` | UI | Exits 0 | ✅ PASS | Vite/esbuild skips type errors; chunk size warning only |
| TC-UI20-05 | Dev server reachable | UI | HTML served | ✅ PASS | `GET http://localhost:5173/` returns React shell |
| TC-UI20-06 | `Plugin.START` removed from enum | Source | Not in `plugins.tsx` | ✅ PASS | Enum has exactly 6 members |
| TC-UI20-07 | Empty workflow returns `{nodes:[], edges:[]}` | Source | No startNode prepended | ✅ PASS | `workFlowToNodesAndEdges` returns empty arrays for null/empty pluginList |
| TC-UI20-08 | `begin-plugin.tsx` is empty | Source | No functional export | ✅ PASS | File only exports `{}` |
| TC-UI20-09 | Consumer plugin — w-56, blue accent, badge | Source | All 3 present | ✅ PASS | `w-56`, `bg-blue-500`, "Consumer" badge |
| TC-UI20-10 | SafeConsumer plugin — w-56, teal accent, badge | Source | All 3 present | ✅ PASS | `w-56`, `bg-teal-500`, "Safe Consumer" badge |
| TC-UI20-11 | IfElse plugin — w-56, amber accent, badge | Source | All 3 present | ✅ PASS | `w-56`, `bg-amber-500`, "If / Else" badge |
| TC-UI20-12 | Message plugin — w-56, purple accent, badge | Source | All 3 present | ✅ PASS | `w-56`, `bg-purple-500`, "Message" badge |
| TC-UI20-13 | FunctionV2 plugin — w-56, green accent, badge | Source | All 3 present | ✅ PASS | `w-56`, `bg-green-500`, "Function V2" badge |
| TC-UI20-14 | FunctionV3 plugin — w-56, emerald accent, badge | Source | All 3 present | ✅ PASS | `w-56`, `bg-emerald-500`, "Function V3" badge |
| TC-UI20-15 | `HttpCallForm.tsx` — section headers + tooltips + card panel | Source | All present | ✅ PASS | "Trigger Rules" + "HTTP Configuration" headers; tooltip on 9 fields; HTTP config in card panel |
| TC-UI20-16 | `LogicForm.tsx` — section headers + tooltips + card panel | Source | All present | ✅ PASS | "Trigger Rules" + "Logic Configuration" headers; tooltip on 4 fields; Logic config in card panel |
| TC-UI20-17 | Operation API — entity-setting list | API | 200 + apps array | ✅ PASS | 7 apps returned |
| TC-UI20-18 | Operation API — history endpoint | API | 200 + revisions | ✅ PASS | 9 revisions for TEST_APP_A |
| TC-UI20-19 | Operation API — records endpoint | API | 200 + paginated records | ✅ PASS | Records with overallStatus/createdDateTime |
| TC-UI20-20 | Built bundle free of START dead code | Bundle | No START strings | ✅ PASS | Fixed commit 871a615 — `Plugin.START` call sites and `const START` constant removed; `'start-node'` literals are intentional guards |

**Defects found:**

| DEF-ID | Severity | Description | Files |
|--------|----------|-------------|-------|
| DEF-UI-01 | High | FIXED (871a615) — `Plugin.START` references removed from `useWorkflowConnections.ts` and `workflow-drawer/index.tsx`; 0 tsc errors confirmed | `useWorkflowConnections.ts`, `workflow-drawer/index.tsx` |
| DEF-UI-02 | Low | FIXED (871a615) — `const START = 'start-node'` removed from mapper; remaining `'start-node'` literals are intentional guards | `workFlowMapper.ts` |

**Verdict: READY for deployment — all defects resolved (commits fdfc086 + 871a615).**

---

## TC-UI21: Design Overhaul v2.1 — commit 8d407bf

**Scope:** UI design overhaul — sophisticated professional aesthetic (17 files changed)  
**Date:** 2026-04-06  Build: 8d407bf2748ed3d7ac9e1ca977647f4a458c024a

| TC-ID | Description | Layer | Expected | Result | Notes |
|-------|-------------|-------|----------|--------|-------|
| TC-UI21-01 | `npx tsc -p tsconfig.app.json --noEmit` | UI | 0 errors | ✅ PASS | Clean exit |
| TC-UI21-02 | `npm run lint` | UI | 0 errors | ✅ PASS | 1 pre-existing warning, 0 errors |
| TC-UI21-03 | `npm run build` | UI | Exits 0 | ✅ PASS | 18.60 s; chunk-size advisory only |
| TC-UI21-04 | `PluginDisplayName` exported from `plugins.tsx` | Source | Exported map present | ✅ PASS | `Record<Plugin, string>` all 6 plugins |
| TC-UI21-05 | `PluginDisplayName` used in sider | Source | Import + usage confirmed | ✅ PASS | `workflow-sider/index.tsx` lines 4, 65 |
| TC-UI21-06 | No `Plugin.START` references in `src/` | Source | 0 matches | ✅ PASS | Grep returned no matches |
| TC-UI21-07 | All 6 node files — `h-[3px]` accent strip | Source | Present in all 6 | ✅ PASS | Line 22 across all plugin files |
| TC-UI21-08 | All 6 node files — `w-52` | Source | Present in all 6 | ✅ PASS | Line 13 across all plugin files |
| TC-UI21-09 | All 6 node files — `ring-2 ring-indigo-500` on select | Source | Present in all 6 | ✅ PASS | Line 16 across all plugin files |
| TC-UI21-10 | `defaultEdgeOptions.animated` is `false` | Source | `animated: false` | ✅ PASS | `canvas/index.tsx` line 47 |
| TC-UI21-11 | `defaultEdgeOptions` stroke `"#94A3B8"` | Source | Stroke and markerEnd color set | ✅ PASS | `canvas/index.tsx` lines 54–56 |
| TC-UI21-12 | Drawer uses `styles` prop (not deprecated `headerStyle`/`bodyStyle`) | Source | `styles={{...}}` present, no deprecated props | ✅ PASS | `workflow-drawer/index.tsx` line 72 |
| TC-UI21-13 | `__root.tsx` nav has `bg-zinc-900` | Source | Confirmed | ✅ PASS | `__root.tsx` line 65 |
| TC-UI21-14 | Ant Design theme `colorPrimary: "#4F46E5"`, `borderRadius: 6` | Source | Confirmed | ✅ PASS | `__root.tsx` lines 45–47 |
| TC-UI21-15 | Workflows list page `bg-zinc-50` background | Source | Confirmed | ✅ PASS | `workflows/index.tsx` line 176 |
| TC-UI21-16 | Background dots `gap:24 size:1 color:#CBD5E1` + `bg-zinc-50` | Source | Confirmed | ✅ PASS | `canvas/index.tsx` lines 114–119 |
| TC-UI21-17 | Form section headers `text-[10px] uppercase tracking-widest` | Source | Present in both forms | ✅ PASS | `HttpCallForm.tsx` lines 65/101; `LogicForm.tsx` lines 55/91 |
| TC-UI21-18 | Sidebar group renamed "Nodes" | Source | `label: "Nodes"` | ✅ PASS | `plugins.tsx` line 95 |
| TC-UI21-19 | `children.label` is `string` | Source | `label: string` in type | ✅ PASS | `PluginMenu` type `plugins.tsx` line 87 |
| TC-UI21-20 | Workflow header slimmer (`minHeight: 44`) | Source | Confirmed | ✅ PASS | `workflow-header/index.tsx` line 87 |

**Defects:** None.

**Verdict: READY — all 20 checks pass; no defects found.**

---

## TC-UI22: Mobile Responsive Adaptation v2.2 — commit 1f5dd40

**Scope:** Full mobile responsive adaptation — all list pages, canvas, drawer, header  
**Date:** 2026-04-06  Build: 1f5dd40

| TC-ID | Description | Layer | Expected | Result | Notes |
|-------|-------------|-------|----------|--------|-------|
| TC-UI22-01 | `npx tsc -p tsconfig.app.json --noEmit` | UI | 0 errors | ✅ PASS | Clean exit |
| TC-UI22-02 | `npm run lint` | UI | 0 errors | ✅ PASS | 0 errors; 1 pre-existing warning |
| TC-UI22-03 | `npm run build` | UI | Exits 0 | ✅ PASS | 19.83 s; chunk-size advisory only |
| TC-UI22-04 | `useIsMobile` hook created | Source | `/src/hooks/useIsMobile.ts` exists | ✅ PASS | resize listener, SSR-safe initializer |
| TC-UI22-05 | `useIsMobile` used in 7 consumer files | Source | Grep returns 7 files | ✅ PASS | All target files import the hook |
| TC-UI22-06 | Workflows list — mobile card view | Source | Card stack when `isMobile` | ✅ PASS | Table preserved on desktop |
| TC-UI22-07 | Workflows list — floating `+` FAB on mobile | Source | `fixed bottom-6 right-6 z-50` | ✅ PASS | Desktop uses inline button |
| TC-UI22-08 | Records list — mobile card view | Source | Card stack with tag, date | ✅ PASS | |
| TC-UI22-09 | Records list — compact filters on mobile | Source | Secondary filters hidden | ✅ PASS | App + Status filters kept |
| TC-UI22-10 | Record detail — single-column on mobile | Source | `column={isMobile ? 1 : 2}` | ✅ PASS | |
| TC-UI22-11 | Record detail — child table horizontal scroll | Source | `scroll={{ x: 600 }}` on mobile | ✅ PASS | |
| TC-UI22-12 | Canvas — sidebar hidden on mobile | Source | `{!isMobile && <WorkflowSider/>}` | ✅ PASS | Fully unmounted, not just hidden |
| TC-UI22-13 | `MobileAddNodeSheet` component created | Source | File exists in canvas dir | ✅ PASS | Lives inside ReactFlow tree |
| TC-UI22-14 | Bottom sheet with plugin grid | Source | `Drawer placement="bottom"` + grid | ✅ PASS | 3-col grid, all 6 plugins |
| TC-UI22-15 | `addNodeAtCenter` uses `useReactFlow()` | Source | `screenToFlowPosition` called | ✅ PASS | Canvas center at `w/2, h/2` |
| TC-UI22-16 | Floating `+` button bottom-left | Source | `fixed bottom-20 left-4 z-50` | ✅ PASS | |
| TC-UI22-17 | WorkflowDrawer full-width on mobile | Source | `width={isMobile ? "100%" : 420}` | ✅ PASS | |
| TC-UI22-18 | ReactFlow `panOnDrag` + `minZoom` set | Source | `panOnDrag={true}`, `minZoom={0.3}` | ✅ PASS | |
| TC-UI22-19 | MiniMap hidden — component | Source | `{!isMobile && <MiniMap />}` | ✅ PASS | |
| TC-UI22-20 | MiniMap hidden — CSS | Source | Media query in `index.css` | ✅ PASS | Double-guarded |
| TC-UI22-21 | Controls touch target 36px | Source | Media query in `index.css` | ✅ PASS | |
| TC-UI22-22 | Global header compact on mobile | Source | Brand name hidden on `<sm` | ✅ PASS | Menu items remain |
| TC-UI22-23 | Desktop layout unchanged | Source | All `isMobile` guards preserved | ✅ PASS | |
| TC-UI22-24 | No API / data model changes | Source | Only layout changes | ✅ PASS | All hooks/API calls identical |
| TC-UI22-25 | `git push` success | Git | Commit `1f5dd40` on `origin/main` | ✅ PASS | |

**Defects:** None.

**Verdict: READY — all 25 checks pass; no defects found. Desktop layout fully preserved.**

---

## Known Observations / Limitations

| # | Observation | Severity | Status |
|---|-------------|----------|--------|
| 1 | `enabled` flag not enforced by online-api — execution proceeds even when app is disabled | Low | Open — enforcement must be done upstream |
| 2 | Empty rule key (`ruleList: []`) causes GI_FAIL — save API should reject empty keys | Medium | Open — data validation gap |
| 3 | `workflowTransactionDetails` is AES-encrypted — cannot be inspected in tests without keystore | Accepted | By design |
| 4 | httpbin.org internet dependency — execution tests fail in air-gapped environments | Medium | Open — consider local mock server |
| 5 | Parent GI record not persisted for ANZ_PAYMENT_NOTIFY pipeline — outer try-catch masks GI exception | Medium | Open — needs investigation of FUNCTION_V2/V3 outputParameter serialization |
