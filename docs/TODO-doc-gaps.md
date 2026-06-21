# 文档审计缺口待办（TODO-doc-gaps）

来源：`docs/doc-implementation-audit-v1.0.md`（2026-06-21）。本文件是审计发现的"文档↔实现"缺口清单，**独立于** submodule 内的 `workflow-agent-teams/TODO.md`（按约定本轮不改动 submodule）。确认采纳后可由 PM 折叠进正式 backlog。

格式与 `TODO.md` 一致：`label`（kebab-case）、状态、规格、可追溯到的文档/AC。

## A. 实现缺口（审计确认未实现/未完成）

- [ ] **online-api — POST 可选 SSE：每步完成后推送 DB 已存 Runtime** *(label: `TODO-online-api-post-optional-sse-runtime-per-step`)* — **Status:** Open（审计确认缺失）。**证据：** `workflow-online-api` 全仓零 `SseEmitter`/`ServerSentEvent`/`text/event-stream`；`WorkflowOnlineController.postWorkflow()` 返回 `ResponseEntity<Void>` 单次响应（`WorkflowOnlineController.java:115,252`）。**规格：** 请求头声明启用 SSE 时返回 `text/event-stream`，每步执行且 runtime 写入 DB 后推送当前可读 runtime；无该头时行为与现网完全一致（opt-in，硬约束）。**追溯：** GAP-1 / 基线 REC-US-13~16。

- [ ] **画布 — Test：SSE 逐步展示每步 API 响应(JSON) + 大体量 UI/性能** *(label: `TODO-canvas-test-sse-per-step-response-ui-performance`)* — **Status:** Open（依赖上条）。**证据：** UI 无 SSE 消费；现有 Test 走单次 `onlineApi.postWorkflow`。**规格：** Test 用 SSE，每步推送后 UI 即时追加；大 JSON 需折叠/虚拟列表/Web Worker 等防主线程卡顿；未启用 SSE 时行为不变。**追溯：** GAP-2 / CV-US-17。

- [ ] **部署 — 重写 Deploy：自动调 online API、执行名 vs 源应用、双块请求体、JSON 工作流 + AI Generate** *(label: `TODO-deploy-rewrite-online-api-workflow-json`)* — **Status:** Open。**证据：** 现 `DeployModal.tsx` 走 operation-api 三步（Create/Update/Save），未改为 online-api 单调用 + 双块 body。**规格：** 点击 Deploy 客户端自动调 online API；query 用户输入应用名=运行执行名；body 必含 A=源应用全部信息 + B=工作流全部信息；过程用 JSON 工作流表达，可借 AI Generate；仅改 UI component，body 组装全部经 API 获取。**追溯：** GAP-3 / APP-US-52。

- [ ] **E2E 测试 — IBM Carbon 重写后选择器更新** *(label: `TODO-e2e-carbon-rewrite-selector-updates`)* — **Status:** Open。**证据：** `e2e-test-report-v44.0.md` 记录 81/90 失败，测试仍查 `.ant-pagination`/`.ant-modal`/`.ant-drawer`；UI 已重建为 Carbon `.cds--*`。**影响：** Canvas import(15)、Node editor(8)、JsonPath(4)、AI Generator(1)、Applications(7)、Drawer close(6)、Explain(1)、Records(1)。**规格：** 将 E2E 选择器更新为 Carbon 类名/role。**追溯：** GAP-4 / CV-US-38,47,48。

## B. 文档↔实现偏差（建议文档澄清，非阻断）

- [ ] **Arch 澄清 — Deploy 逻辑三步与真实端点对应表** *(label: `TODO-doc-clarify-deploy-step-endpoint-mapping`)* — **Status:** Open（文档修订）。**问题：** `pm-doc-master` APP-AC-52-D4 及多版 arch 用 `CreateApplicationName/UpdateApplicationName/SaveWorkflow` 作为"路径"，但无同名端点；实际为 `POST/PATCH /api/workflow/entity-setting` + `POST /api/workflow`（`DeployModal.tsx` + `DeployProxyController`）。**规格：** 在最新 arch 文档加"逻辑三步 ↔ 真实端点"对应表，避免误读。**追溯：** DIV-1。

- [ ] **Arch 澄清 — 历史回滚为客户端重存语义** *(label: `TODO-doc-clarify-history-rollback-client-side`)* — **Status:** Open（文档修订）。**问题：** APP-AC-11-D2 描述"仅回滚工作流定义、产生新修订"，实现为前端 `history-drawer` 解码旧定义后 `saveWorkflow()` 重存，无服务端 rollback 端点。**规格：** arch 文档注明回滚实现位置在前端、通过重存产生新 Envers 修订。**追溯：** DIV-2。

## C. 复核依赖（环境）

- [ ] **测试环境 — 放行 UAT egress 以实跑 `tests/`** *(label: `TODO-tests-egress-allowlist-uat-hosts`)* — **Status:** Open（环境配置）。**问题：** 当前沙箱 egress 拦截 `workflow-ui-gamma.vercel.app`、`workflow-operation-api-n9sbp.ondigitalocean.app`、`workflow-online-api-nr3e4.ondigitalocean.app`（`Host not in allowlist`），`tests/` 套件无法实跑，审计中需实跑的项标记 BLOCKED(env)。**规格：** 将三 host 加入环境 egress 允许列表后运行 `tests/`，回填审计报告 §3–§5。**追溯：** 审计报告 §0/§8。
