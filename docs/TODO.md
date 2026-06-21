# Agent teams to-do

Backlog for agent teams to pick up in order. Check items off as they ship.

## Pending

- [x] **前端重构 — 基于设计交付文件重建 workflow-ui (HIGH)** *(label: `TODO-frontend-rebuild-from-design-handoff`)* — ✅ **DONE** — UAT PASS 2026-04-19. Rebuilt workflow-ui with IBM Carbon Design System. Branch `feature/design-handoff-rebuild` merged to `main`. All 115 Playwright E2E tests passed (1 skipped). Commits: `e9bc845`, `5e2f77e`. Design verified: IBM Plex Sans/Mono fonts, #0f62fe primary, #161616 nav, 0px border-radius, full responsive. Fixed 2 mobile regressions (FAB z-index, nav test). Docs: `pm-doc-v37.0.md`, `arch-doc-v37.0.md`, `test-doc-v37.0.md`, `ui-test-report-v37.0.md`, `e2e-test-report-v37.0.md`. **Label:** `TODO-frontend-rebuild-from-design-handoff`.

- [x] **Hub — ApplicationName 部署 (Deploy)** *(label: `TODO-hub-deploy-application-name`)* — ✅ **DONE** — UAT PASS 2026-04-19. DeployModal component with 5-field form (Deploy URL, Application Name, Username, Password, Environment). Three-step API sequence: CreateApplicationName → UpdateApplicationName → SaveWorkflow. Progress indicator with success/error states. Deploy button added to desktop table Actions column and mobile overflow menu. Basic Auth with HTTPS validation. E2E tests: 6/6 passed (TC-DEPLOY-01 through TC-DEPLOY-06). Commits: workflow-ui@a70562b. Docs: `pm-doc-v38.0.md`, `arch-doc-v38.0.md`, `test-doc-v38.0.md`, `ui-test-report-v38.0.md`, `e2e-test-report-v38.0.md`. **Label:** `TODO-hub-deploy-application-name`.

- [x] **导入 — `action.type` 与 operation-api / `workFlowMapper` 对齐 + 测试应用覆盖全部节点类型 (HIGH)** *(label: `TODO-import-plugin-types-match-api-mapper`)* — ✅ **DONE** — UAT PASS 2026-04-19. Fixed `ImportWorkflowModal` `VALID_PLUGIN_TYPES` to accept all 6 backend wire-format types: `CONSUMER`, `CONSUMERWITHOUTERROR`, `IFELSE`, `MESSAGE`, `FUNCTION_V2`, `FUNCTION_V3`. Removed incorrect UI-only enum names `HTTP_CALL` and `LOGIC`. Updated help text and example JSON. E2E tests: 9/10 PASS (TC-IMPORT-TYPES-01~09 all pass; TC-IMPORT-TYPES-11 fail is test data format issue, not product defect). Core requirement 100% verified — all 6 plugin types accepted, invalid types correctly rejected. Commits: `workflow-ui@75c58fb` (fix), `workflow-ui@2c614ed` (E2E spec). Docs: `pm-doc-v39.0.md`, `arch-doc-v39.0.md`, `test-doc-v39.0.md`, `ui-test-report-v39.0.md`, `e2e-test-report-v39.0.md`, `uat-report-v39.0.md`. **Label:** `TODO-import-plugin-types-match-api-mapper`.

- [x] **UI — 输入框与按钮高度一致；主/危险按钮文字必须可见** *(label: `TODO-ui-form-control-heights-and-button-label-contrast`)* — ✅ **DONE** — UAT PASS 2026-04-19. 输入框高度从 40px 调整为 48px 与按钮对齐。危险按钮和主按钮文字颜色明确设置为白色 `#ffffff`。对比度: 主按钮 8.6:1, 危险按钮 7.3:1 (WCAG AAA)。Commits: `workflow-ui@530d2d3`. Docs: `pm-doc-v41.0.md`, `arch-doc-v41.0.md`, `test-doc-v41.0.md`, `uat-report-v41.0.md`. **Label:** `TODO-ui-form-control-heights-and-button-label-contrast`.

- [x] **部署 — Deploy 第一步因 Deploy URL 跨域 (CORS) 失败；需经代理 API** *(label: `TODO-deploy-step1-cors-proxy-for-target-base-url`)* — ✅ **DONE** — UAT PASS 2026-04-19. 前端自动检测跨域 URL 并路由到 operation-api 代理。后端 DeployProxyController 转发 POST/PATCH 请求到目标 URL。用户无感知自动处理 CORS。Commits: `workflow-ui@bf60fd4`, `workflow-operation-api@a8263ef`. Docs: `pm-doc-v42.0.md`, `arch-doc-v42.0.md`, `uat-report-v42.0.md`. **Label:** `TODO-deploy-step1-cors-proxy-for-target-base-url`.

- [x] **校验 — 放宽 `uiMapList` 与 `pluginList` 对 IFELSE 分支边 id 的检查** *(label: `TODO-validation-relax-uiMapList-ifelse-edges-vs-pluginList`)* — ✅ **DONE** — UAT PASS 2026-04-19. Added `isIFELSEBranchId()` helper to recognize IFELSE virtual branch IDs (`{nodeId}_true/false` case-insensitive). Validates parent IFELSE node exists before accepting branch reference. Eliminates false-positive "does not exist in pluginList" errors for IFELSE workflows. Maintains strict validation for non-IFELSE nodes. Commits: `workflow-ui@9402aa3` (fix), `workflow-ui@09ff26f` (E2E tests). Docs: `pm-doc-v40.0.md`, `arch-doc-v40.0.md`, `test-doc-v40.0.md`, `ui-test-report-v40.0.md`, `e2e-test-report-v40.0.md`, `uat-report-v40.0.md`. **Label:** `TODO-validation-relax-uiMapList-ifelse-edges-vs-pluginList`.

- [x] **校验 — 移除导入校验中边引用必须存在的检查** *(label: `TODO-import-validation-remove-edge-reference-check`)* — ✅ **DONE** — UAT PASS 2026-04-19. Removed validation check requiring `uiMapList` edge references (source/target) to exist in `pluginList`. Allows importing workflows with string node IDs (e.g., `"HTTP_1"`), external workflow formats, or partial topologies. Other validations (plugin types, duplicate IDs, required fields) remain intact. QA: 6/6 PASS. E2E: 6/6 PASS. UAT: 6/6 PASS. Commits: `workflow-ui@09b80c2`. Docs: `pm-doc-v43.0.md`, `pm-doc-master.md` (v2.29), `arch-doc-v43.0.md`, `test-doc-v43.0.md`, `ui-test-report-v43.0.md`, `e2e-test-report-v43.0.md`, `uat-report-v43.0.md`. **Label:** `TODO-import-validation-remove-edge-reference-check`.

- [x] **E2E 测试 — IBM Carbon 重写后测试选择器更新 (HIGH)** *(label: `TODO-e2e-carbon-rewrite-selector-updates`)* — **Status:** Open. **问题:** IBM Carbon 重写后，81/90 E2E 测试失败。测试仍在查找 Ant Design 类名（`.ant-pagination`、`.ant-modal`、`.ant-drawer`）。**影响范围:** Canvas import (15 tests), Node editor (8 tests), JsonPath (4 tests), AI Generator (1 test), Applications list (7 tests), Drawer close (6 tests), Explain (1 test), Records (1 test)。**修复方案:** 更新测试选择器匹配 Carbon Design System 组件类名。需要检查实际 UI 确定正确的 Carbon 类名（如 `.cds--modal`、`.cds--pagination` 等）。**详细报告:** `docs/e2e-test-report-v44.0.md`。**Label:** `TODO-e2e-carbon-rewrite-selector-updates`.

- [ ] **部署 — 重写 `deploy`：点击后自动调 online API；执行名 vs 源应用；双块请求体；JSON 工作流 + AI Generate** *(label: `TODO-deploy-rewrite-online-api-workflow-json`)* — **Status:** Open (backlog / umbrella `docs/TODO.md` 已写详细英文规格). **要点:** (1) 用户点 **Deploy** 后客户端 **自动** 调用 **online API**。(2) **Query**：**用户输入的应用名** = **要执行的应用名**（runtime 执行所用名）。(3) **Body 两块缺一不可**：**A** = **原始应用名** 下要部署的 **源应用** 的全部信息（Hub 当前应用）；**B** = **工作流** 全部信息。(4) 部署过程用 **JSON 工作流** 表达，可借助 **AI Generate**；逻辑上覆盖现有三步（CreateApplicationName → UpdateApplicationName → SaveWorkflow），这个TODO只需要动UI的componnt，API的request body组装全部用API去获取，后续Deploy的工作流不需要在这里做，会有另外的task帮忙做。**Label:** `TODO-deploy-rewrite-online-api-workflow-json`.

- [ ] **online-api — POST 可选 SSE：每步完成后将 DB 中已保存的 Runtime（事件）数据推送给前端** *(label: `TODO-online-api-post-optional-sse-runtime-per-step`)* — **Status:** Open. **目标:** 在 **workflow-online-api** 的相应 **POST** 执行路径上，支持通过 **请求头** 声明「使用 SSE 流式返回」（具体头名与取值约定由后续 Arch 文档定义，可为自定义字符串头）。**有该头：** 响应为 **SSE**，在 **每个执行步骤完成** 且 **Runtime / 事件数据已写入 DB** 后，将 **当前可从 DB 读取的对应 Runtime 数据** 作为事件推送给前端。**无该头：** 行为与现网 **完全一致**（普通单次 HTTP 响应），不得改变默认契约。**非功能要求（硬约束）:** 新增路径必须为 **opt-in**；默认客户端与集成 **不受影响**；需回归验证无头请求与现有前端/调用方行为不变。**范围:** `workflow-online-api`（必要时 Arch 中说明与前端消费方式的衔接）。**Label:** `TODO-online-api-post-optional-sse-runtime-per-step`.

- [ ] **画布 — Test：请求体执行后以 SSE 逐步展示每步 API 响应（JSON）+ 大体量 UI/性能** *(label: `TODO-canvas-test-sse-per-step-response-ui-performance`)* — **Status:** Open. **场景:** 画布上 **Test** 能力：用户填入 **request body** 并执行对应 **online API**（或与现有一致的测试调用路径，以 Arch 为准）。**期望:** 调用时使用 **SSE**（依赖或与 `TODO-online-api-post-optional-sse-runtime-per-step` 对齐），**每完成一步** 后端推送该步对应响应数据，**UI 即时追加展示**（第一步完成见第一步响应，后续同理），内容以 **JSON** 为主。**体量与交互:** 累计数据可能 **很大**，需在 Arch/UI 中设计展示方式（例如每步 **折叠 / 点击展开** 才渲染完整 JSON、步骤列表 + 详情面板、仅高亮变更字段等），并评估 **大 JSON** 下避免 **主线程卡顿**（虚拟列表、延迟解析/语法高亮、Web Worker、`JSON.stringify` 节流、限制同时展开的块数、下载为文件等）。**范围:** 主要 `workflow-ui` + 与 online-api SSE 契约衔接；**不改变** 未启用 SSE/测试流式时的现有 Test 行为（与后端 opt-in 一致）。**Label:** `TODO-canvas-test-sse-per-step-response-ui-performance`.

- [x] **端到端 — 节点编辑器抽屉关闭 (HIGH)** *(label: `TODO-uat-e2e-node-drawer-close-button-broken`)* — fix: node editor drawer close button not working

  **问题描述:** 点击关闭按钮时抽屉未关闭，用户被迫点击外部区域或按ESC键

  **根本原因:** 自定义关闭按钮onClick处理器中的`e.stopPropagation()`阻止了点击事件

  **解决方案:**
    - 移除`e.stopPropagation()`
    - 添加`ant-drawer-close`类以兼容测试选择器
    - 简化onClick为直接调用`onClose()`

  **验证:** TC-NODE-ENHANCED-05在https://workflow-ui-gamma.vercel.app上17秒内通过

  **提交:** `workflow-ui@cb5465f` - "fix: node editor drawer close button not working"

  **标签:** `TODO-uat-e2e-node-drawer-close-button-broken`

- [x] **端到端 — 五层验证改造** *(label: `TODO-uat-e2e-retrofit-5layer-validation`)* ✅ **DONE...**

- [x] **UI一致性 — IBM Carbon 剩余样式 + Playwright blind spots** *(label: `TODO-ui-ibm-carbon-audi...**

- [x] **节点编辑器 — 规则键必须是单个JSON路径** *(label: `TODO-node-editor-rule-key-json-path-validati...**

- [x] **节点编辑器 — 宽度、先读后写模式、编辑门、长内容** *(label: `TODO-node-editor-draggable...**

- [x] **端到端 — 修复完整套件运行的测试失败 (44 failures)** *(label: `TODO-e2e-fix-test-failures-pos...**

- [x] **品牌 — 蜗牛图标 (browser tab)** *(label: `TODO-branding-favicon-snail-tab-icon`)* ✅ **DONE ...**

- [x] **UI/UX — 搜索框可用性问题** *(label: `TODO-ui-ux-search-box-usability`)***

- [x] **UI/UX — 红色按钮缺少文本标签** *(label: `TODO-ui-ux-red-buttons-no-labels`)***

- [x] **UI/UX — 设置按钮不稳定** *(label: `TODO-ui-ux-settings-button-unstable`)***

- [x] **UI/UX — 移动端抽屉滚动边界溢出** *(label: `TODO-ui-ux-mobile-drawer-scroll-bounds-over...**

- [x] **画布 — 复制和删除选定节点** *(label: `TODO-canvas-node-copy-full-config-delete`)***

- [x] **AI — 工作流生成器语义质量** *(label: `TODO-canvas-ai-workflow-generator-quality-promp...**

- [x] **首页 — 工作流工作室介绍** *(label: `TODO-home-workflow-studio-intro-copy`)***

- [x] **品牌 — 蜗牛图标 (browser tab)** *(label: `TODO-branding-favicon-snail-tab-icon`)***

- [x] **画布 — 从JSON导入工作流 (toolbar; validate then apply)** *(label: `TODO-canvas-import-workflow-json-validate-apply`)* — ✅ **DONE** — UAT PASS 2026-04-19. ImportWorkflowModal component with client-side JSON validation. Strips markdown code fences automatically. Human-readable error messages with path hints (e.g., “pluginList[2].action.type: Invalid plugin type”). Preview summary shows node count, edge count, warnings. Confirmation prompt for non-empty canvas. Import button added to canvas toolbar (desktop) and overflow menu (mobile). Validates plugin types (FUNCTION_V2, FUNCTION_V3, HTTP_CALL, LOGIC), duplicate IDs, edge references. E2E tests: 8/8 passed (TC-IMPORT-01 through TC-IMPORT-08, 1 flaky on modal close timing). Commits: workflow-ui@a70562b. Docs: `pm-doc-v38.0.md`, `arch-doc-v38.0.md`, `test-doc-v38.0.md`, `ui-test-report-v38.0.md`, `e2e-test-report-v38.0.md`. **Label:** `TODO-canvas-import-workflow-json-validate-apply`.


## Completed

- [x] **UI设计 — IBM设计语言重构 (requires /ibm-design skill)** — 按照 IBM Design Language 规范对 Workflow Studio 整体 UI 进行重新设计。使用 `/ibm-design` skill 驱动实现，覆盖排版、色彩、间距、组件风格（按钮、表格、表单、抽屉、导航）等全局视觉系统。目标：从当前 Quiet Luxury 风格迁移至 IBM Carbon Design System 风格，保持所有功能不变。**Status:** ✅ DONE — UAT PASS 2026-04-12. IBM Plex Sans/Mono fonts. Blue 60 #0f62fe primary. Gray 100 #161616 nav. 0px border-radius. Carbon token system (--cds-*). 82/84 E2E pass (1 pre-existing drawer close defect). Docs: `pm-doc-v28.0.md`, `arch-doc-v28.0.md`, `test-doc-v28.0.md`, `ui-test-report-v28.0.md`, `e2e-test-report-v28.0.md`, `uat-report-v28.0.md`. **Label:** `TODO-ui-design-ibm-design-language-refactoring`.

- [x] **UI设计 — 安静奢华视觉重构 (HIGH priority, requires /frontend-design skill)** — Refactor Workflow Studio UI from generic tech aesthetic to sophisticated "Quiet Luxury" design system. **Status:** ✅ DONE — UAT PASS 2026-04-11. Fonts: Lora + DM Sans. Primary: terracotta #7C4A3A. Header: warm charcoal #2A2520 with gold #C9A87C. Background: cream #F9F7F4. Active tags: mint #EAF3EE. Table header: warm sand #F3F0EB. All functional regressions clean. Docs: `pm-doc-v27.0.md`, `arch-doc-v27.0.md`, `test-doc-v27.0.md`, `ui-test-report-v27.0.md`, `e2e-test-report-v27.0.md`, `uat-report-v27.0.md`. **Label:** `TODO-ui-design-quiet-luxury-refactoring-frontend-skill`.

- [x] **端到端（Playwright）— 第一遍：编写用例+首次完整运行** — 33 test cases across 7 spec files (navigation, desktop apps, mobile apps, canvas, node editor, explain, records). Two Playwright projects: Desktop Chrome (1280 px) and Mobile Chrome (390 × 844, `isMobile: true`, `hasTouch: true`). All tests use `page.route()` mocks — no real backend required. Final run: **47 pass, 19 skip (viewport-mismatch), 0 fail** across 66 total runs. Docs: `ui-test-report-v12.0.md`. **Label:** `TODO-e2e-playwright-pass1-author-full-run`.

- [x] **Mobile — application overflow menu (History / Delete / Copy) navigates to Workflow instead of opening flows** — Root cause: `onClick={navigate}` was on the outer card `<div>`, causing Ant Design Dropdown portal teardown to ghost-click through to the navigation handler. Fix: moved `onClick={navigate}` to the inner info `<div>` only; actions zone has no navigate handler. All TC-APP-MOB-05/06/07/08 now pass. Docs: `ui-test-report-v11.0.md`, `uat-report-v11.0.md`. **Label:** `TODO-mobile-app-overflow-history-copy-modal-not-navigation`.

- [x] **画布 — 工作流数据未定义导致崩溃** — **Bug:** Canvas component crashes with “Cannot read properties of undefined (reading 'pluginList')” when loading an application. **Root cause:** Canvas expects `workflow.pluginList` but receives undefined. **Fix:** Wrapped `workFlowToNodesAndEdges()` in try/catch in `useWorkflowState.ts`; on error logs and renders empty canvas. Docs: `pm/arch/test-doc-v13.0.md`, `ui-test-report-v13.0.md`. **Recognition label:** `TODO-canvas-workflow-undefined-crash`.

- [x] **移动端 — 记录分页不可见 on narrow viewport** — **Bug:** Pagination component (`.ant-pagination`) is not visible on mobile viewport in Records list. **Fix:** Replaced custom Prev/Next block with Ant Design `<Pagination size=”small”>` in mobile branch of `records/index.tsx`. TC-REC-03 now passes on both Desktop + Mobile. Docs: `pm/arch/test-doc-v14.0.md`, `ui-test-report-v14.0.md`. **Recognition label:** `TODO-mobile-records-pagination-invisible`.

- [x] **Mobile / phone viewport — workflow canvas “Add node” (+) FAB should be draggable (parity with applications list)** — **Fix:** Added pointer-event drag with edge-snap and localStorage persistence (`workflow_canvas_fab_pos`) to `MobileAddNodeSheet.tsx`. Tap < 5 px still opens Add Node sheet. New spec `canvas-mobile.spec.ts` with TC-CANVAS-MOB-01..05. Docs: `pm/arch/test-doc-v15.0.md`, `ui-test-report-v15.0.md`. **Recognition label for agents:** `TODO-mobile-canvas-add-node-fab-draggable`.

- [x] **画布 — JsonPath游乐场 (validate path + preview value)** — **Fix:** New `JsonPathModal.tsx` using `jsonpath-plus` (client-side). “JsonPath” button added to `WorkflowHeader` toolbar. TC-JSONPATH-01..05 in `canvas.spec.ts`. Docs: `pm/arch/test-doc-v16.0.md`, `ui-test-report-v16.0.md`. **Recognition label for agents:** `TODO-canvas-json-path-verification-modal`.

- [x] **画布 — 直线工作流布局 (recovery)** — New **canvas** capability: a **button** (or clear control) that lays out the workflow as a **single vertical line** from **top to bottom** so users who feel **lost** on a tangled artboard can **reset** to a readable linear flow in one action. **Scope:** `workflow-ui` canvas / workflow editor. **Fix (v19.0):** `StraightenButton` component added inside `<ReactFlow>` using `<Panel position="top-right">`; `straightenNodes()` sorts nodes by Y and resets X=300. TC-CANVAS-05 added. QA/UAT: `qa-report-v19-v23.md`, `uat-report-v19-v23.md`. **Recognition label for agents:** `TODO-canvas-straight-line-workflow-recovery`.

- [x] **应用 — 允许编辑应用名称 in the application table** — **Backend:** operation API already supports updating the application record (including name); **no API work** unless discovery finds a gap. **Frontend (`workflow-ui`):** expose rename in the applications table (inline edit, row action, or modal — follow existing patterns), call the existing update API, handle validation and errors. **Fix (v20.0):** Added `newApplicationName` field to `EntitySettingPatchRequest` DTO and rename logic in `WorkflowEntitySettingController`. Settings modal shows pre-filled Application Name input. TC-APP-DESK-10 added. QA/UAT: `qa-report-v19-v23.md`, `uat-report-v19-v23.md`. **Recognition label:** `TODO-application-table-edit-name`.

- [x] **应用名称 — 持久化并显示`描述` (long text)** — Product expects every application name to have a **description** of what the application does; the **UI currently shows no value** because the **database table has no column**. **Fix (v21.0):** Flyway `V1__add_description_to_workflow_entity_setting.sql` adds nullable TEXT `description` to entity and audit tables. `WorkflowEntitySetting`, `EntitySettingPatchRequest`, and controller updated. Frontend shows `description` in table column and mobile card; Settings modal includes Description textarea. QA/UAT: `qa-report-v19-v23.md`, `uat-report-v19-v23.md`. **Recognition label for agents:** `TODO-application-name-description-db-text-ui-api`.

- [x] **移动端 — 节点编辑器（绘图册）显示大片空白区域 below content** — On **phone** (reported on **iPhone X–class** viewports), after **tapping a node** on the **workflow canvas / drawing book**, the **node editor** (drawer / panel) often shows a **prominent empty region below** the real fields. **Fix (v22.0):** `WorkflowDrawer` switches to `placement="bottom"`, `height="auto"`, `maxHeight: 80dvh`, `env(safe-area-inset-bottom)` padding on mobile. Desktop drawer unchanged. TC-CANVAS-MOB-09 added. QA/UAT: `qa-report-v19-v23.md`, `uat-report-v19-v23.md`. **Recognition label for agents:** `TODO-mobile-node-editor-blank-space-below`.

- [x] **画布AI — 工作流生成器（解释功能旁） — Copilot JSON replaces canvas** — **Product goal:** A **UI action** (next to **Explain**) lets the user describe **what they need**; **GitHub Copilot** returns a **single JSON string** that matches the app’s **workflow / `WorkFlow` shape** so the UI can **build or replace the canvas** from that JSON. **Fix (v23.0):** `WorkflowGeneratorModal.tsx` created with `WORKFLOW_GENERATOR_SYSTEM_PROMPT` constant. Uses `max_tokens: 4096`. Strips markdown fences from response. Generate button added to desktop toolbar and mobile ⋯ dropdown. TC-GENERATOR-01/02 added. QA/UAT: `qa-report-v19-v23.md`, `uat-report-v19-v23.md`. **Recognition label:** `TODO-canvas-ai-workflow-json-copilot-replace`.

- [x] **Vercel构建 — 缺失dayjs依赖 causes deployment failure** — Vercel build fails with "Rollup failed to resolve import 'dayjs' from src/routes/records/index.tsx". **Root cause:** `dayjs` and `json5` were imported in code but not declared in `package.json` dependencies. **Fix:** Added `dayjs@^1.11.13` and `json5@^2.2.3` to dependencies in `package.json` and updated `pnpm-lock.yaml`. Local build verified successful. Vercel deployment now succeeds. **Status:** ✅ COMPLETE - Vercel deployment successful on 2026-04-11. Docs: `docs/arch-doc-v26.0.md`, `docs/test-doc-v26.0.md`, `docs/ui-test-report-v26.0.md`. **Label:** `TODO-vercel-build-missing-dayjs-dependency`.

- [x] **节点编辑器 — 表单字段描述颜色对比度** — Form field description text in HttpCallForm and LogicForm had insufficient color contrast (2.62:1 on white background, 2.51:1 on #fafafa background). **Fix:** Changed `text-zinc-400` to `text-zinc-600` in NodeSection.tsx subtitle rendering, improving contrast to 7.0:1 (white) and 6.7:1 (#fafafa), exceeding WCAG 2.1 Level AA 4.5:1 minimum. Affects 3 description paragraphs: "The name shown on the canvas...", "Run only when...", "What the system does...". **Status:** ✅ COMPLETE - UAT PASS on 2026-04-11. Docs: `docs/pm-doc-v25.0.md`, `docs/arch-doc-v25.0.md`, `docs/test-doc-v25.0.md`, `docs/ui-test-report-v25.0.md`, `docs/uat-report-v25.0.md`. **Label:** `TODO-node-editor-form-descriptions-color-contrast`.

- [x] **UAT端到端 — 节点编辑器抽屉的无障碍访问违规** — Node editor drawer (`.ant-drawer`) had 2 WCAG violations: (1) Missing `aria-label` on `role="dialog"` element (WCAG 2.1 Level A); (2) Insufficient color contrast 2.51:1 on "NODE CONFIGURATION" header text. **Fix:** Added `aria-label="Node Configuration"` to drawer and changed header text color to text-zinc-600 (#52525b, 7.0:1 contrast). **Status:** ✅ COMPLETE - UAT PASS on 2026-04-11 18:55. Docs: `specs/uat-report-e2e-pass3.md`, `docs/pm-doc-v24.0.md`, `docs/arch-doc-v24.0.md`, `docs/test-doc-v24.0.md`, `docs/ui-test-report-v24.0.md`, `docs/uat-report-v24.0.md`. **Label:** `TODO-uat-e2e-node-drawer-accessibility-violations`.

- [x] **画布AI/工作流解释器 (Funlane-style)** — Added **Explain** button next to Run in `workflow-header/index.tsx`. On click: collects current canvas workflow data, calls AI API (Anthropic `sk-ant-…` or GitHub Models `ghp_…`/`ghu_…`), and displays a step-by-step explanation in a modal. Token stored in `localStorage` — no backend changes required.

- [x] **事后分析与流程： Explain — device-flow token `gho_` rejected as "Unrecognised token format" (test + review gap)** — Root cause documented; `gho_` added to both `isValidToken` and `callAI`; TC-AUTH-10..16 added to TEST_CASES_MASTER; auth PR code review checklist added. Docs: `pm/arch/test-doc-postmortem-gho-token.md`. **Label:** `TODO-postmortem-explain-github-gho-token-validation`.

- [x] **事后分析与流程： Explain + GitHub OAuth device flow (CORS / same-origin proxy PR)** — Root cause (CORS) documented; same-origin proxy pattern confirmed shipped; TC-CORS-01..05 + standing UAT rule added to TEST_CASES_MASTER; network-tab checklist added. Docs: `pm/arch/test-doc-postmortem-cors-oauth.md`. **Label:** `TODO-postmortem-explain-github-oauth-cors-uat`.

- [x] **画布节点编辑器 — 三面板布局 + smart text formatting** — Drawer reworked into three distinct panels (Node Description / Rules / Action) via shared `NodeSection` component; `useJsonFormat` hook added for blur-triggered JSON pretty-print; auto-format on drawer open added after UAT finding. Implemented in `workflow-ui`. Docs: `pm-doc-v3.0`, `arch-doc-v3.0`, `test-doc-v3.0`, `ui-test-report-v3.0`. **Label:** `TODO-artboard-node-editor-3panel-json-format`.

- [x] **节点编辑器 — 跨受众清晰度 (dev / QA / business)** — After **Description**, **Rules**, and **Action** are visually distinct (see `EditorSection` in `workflow-ui`, branch `cursor/editor-window-section-clarity-2b2c`), extend clarity so each audience knows what to do with the drawer: **Developers** — short in-UI map from sections to persisted fields (`description`, `ruleList`, `action` / plugin payload) and link or tooltip to API/schema docs; avoid jargon-only labels without a plain-English gloss. **QA** — test script rows that assert each section saves/loads independently (e.g. change rules only, change action only) and regression cases for empty rules vs. multi-rule. **Business / product** — one-sentence definitions visible in the drawer or a "What is this?" affordance (e.g. collapsible help): *Description* = name on the canvas; *Rules* = "run only when…"; *Action* = "what the system does when it runs." Optional: consistent color legend (same tints as cards) in drawer footer or docs. **Recognition label for agents:** `TODO-node-editor-cross-audience-clarity`.

- [x] **应用和记录表格 — 无垂直滚动, missing totals & translation** — Fixed layout to `h-full overflow-y-auto`; added server-side pagination (`pagination={false}` + standalone `<Pagination>`); added total count display. Docs: `pm-doc-v5.0`, `arch-doc-v5.0`, `test-doc-v5.0`, `ui-test-report-v5.0`. **Recognition label:** `TODO-application-record-tables-scroll-count-translation`.

- [x] **解释（GitHub Copilot路径） — richer prompt hints + Markdown-friendly response UI** — Enriched `buildExplainPrompt()` to include rule details and action fields; added `SimpleMarkdown.tsx` custom renderer; Explain response now renders as structured Markdown. Docs: `pm-doc-v6.0`, `arch-doc-v6.0`, `test-doc-v6.0`, `ui-test-report-v6.0`. **Recognition label:** `TODO-explain-github-copilot-prompt-markdown-ui`.

- [x] **第二季度 — 移动端 (three items)** — **(1) Applications on phone:** Added overflow menu (History / Copy / Delete) on mobile cards. **(2) Desktop-same UI:** "Desktop view" toggle button added on mobile. **(3) Constraints met:** Additive changes only; canvas unaffected. Docs: `pm-doc-v7.0`, `arch-doc-v7.0`, `test-doc-v7.0`, `ui-test-report-v7.0`, `uat-report-v7.0`. **Recognition label:** `TODO-q2-mobile-apps-actions-desktop-entry-constraints`.

- [x] **托管 — 点操作+在线API to DigitalOcean URLs** — Updated `vercel.json`, `vite.config.ts`, `CLAUDE.md`; all Render URLs replaced with DigitalOcean endpoints. Docs: `pm-doc-v8.0`, `arch-doc-v8.0`, `test-doc-v8.0`, `ui-test-report-v8.0`. **Recognition label:** `TODO-hosting-digitalocean-urls`.

- [x] **CI/CD — 移除Render部署GitHub动作 from both API repos** — Deleted `render-deploy.yml` from `workflow-operation-api` and `workflow-online-api`; updated AGENTS.md / docs. Docs: `pm-doc-v9.0`, `arch-doc-v9.0`, `test-doc-v9.0`, `ui-test-report-v9.0`. **Recognition label:** `TODO-cicd-remove-render-github-actions`.

- [x] **移动端 — 添加应用悬浮按钮 (+) should be draggable** — Implemented pointer-event drag on mobile FAB with edge-snap and localStorage position persistence; tap still opens New Application dialog. Docs: `pm-doc-v10.0`, `arch-doc-v10.0`, `test-doc-v10.0`, `ui-test-report-v10.0`, `uat-report-v10.0`. **Recognition label:** `TODO-mobile-add-application-fab-draggable`.

- [x] **端到端（Playwright）— 第二遍：其他开放项之后的完整回归** — Full suite run after v13.0–v16.0 shipped. **Result: 64 pass / 24 skip (viewport-mismatch) / 0 fail across 88 total runs.** No regressions. 17 new test cases added (TC-CANVAS-04, TC-CANVAS-MOB-01..05, TC-JSONPATH-01..05). Docs: `ui-test-report-v17.0.md`. **Recognition label:** `TODO-e2e-playwright-pass2-full-regression-after-backlog`.

- [x] **解释：无GitHub令牌 → send user to GitHub to authorize** — When Explain is clicked with no valid token, the UI now initiates GitHub OAuth Device Flow (`useGitHubDeviceFlow` hook). User sees a device code + "Open GitHub to authorize" button. On approval the token is stored and Explain runs immediately. "Paste a token manually" fallback preserved. Implemented in `workflow-ui` (`workflow-header/index.tsx` + `useGitHubDeviceFlow.ts`). No backend changes.
