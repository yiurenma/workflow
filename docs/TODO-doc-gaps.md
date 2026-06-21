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

## B'. 本地实跑新增（2026-06-21）

- [ ] **Arch 澄清 — 在线执行必需关联头名为 `X-Request-Correlation-Id`** *(label: `TODO-doc-clarify-online-correlation-header-name`)* — **Status:** Open（文档修订）。**证据（本地实跑）：** 缺该头 online-api 返回 400 `440000`「Required request header 'X-Request-Correlation-Id' ... not present」。早期源码审计误记为 `x-request-id`。**规格：** arch/test 文档统一为 `X-Request-Correlation-Id`。**追溯：** DIV-3 / REC-US-12。

- [ ] **测试/环境 — online-api 完整执行需真实 JKS keystore 密钥** *(label: `TODO-tests-online-api-keystore-secret-for-execution`)* — **Status:** Open（环境/密钥）。**证据：** 本地 H2 起 online-api 后，执行写 runtime 触发 `SecureData` 加密；仓库默认 `jks.storepass=changeit` 非真实密码（`keytool` 报 password incorrect），首次执行返回 500「Exception while encrypting value」。**影响：** REC-US-15 幂等（重复→`M0002`）等完整执行路径本地无法验证（仅路由/校验/未知应用已 PASS）。**规格：** 提供 `JKS_STOREPASS`/`JKS_KEYPASS`（或本地测试用 keystore + 对应密码）后即可实跑幂等。**追溯：** 审计 §4 / local-verification-report §online-api。

## D. 产品功能差距 —— "Serverless Easy API Maker" 定位（roadmap，2026-06-21）

> 来源：把平台当 API maker 评估的结论（见 plan 文件 roadmap）。用户已确认这些是"细节、后续丰富"，先登记。

- [ ] **同步响应塑造 —— 调用即拿到结果** *(label: `TODO-online-api-shape-synchronous-response`)* — **Status:** Open（P0）。**问题：** `postWorkflow` 返回 `ResponseEntity<Void>`，成功仅 200 空体；API maker 的命门是"调它→拿到结果"。**规格：** 让工作流计算并塑造响应 body/状态码/头（与 SSE 逐步返回 `TODO-online-api-post-optional-sse-runtime-per-step` 对齐）。**追溯：** 产品说明书"能力边界"。

- [ ] **每个 API 的契约 + 自动 OpenAPI** *(label: `TODO-per-workflow-api-contract-openapi`)* — **Status:** Open（P0）。**问题：** 用户做出来的每个 API 无输入/输出 schema、无自动文档，复用卡在"别人不知道怎么调"。**规格：** 为每个已发布 workflow 生成入参/出参契约 + OpenAPI + curl/示例。

- [ ] **发布 API 的入站鉴权 / 限流 / CORS** *(label: `TODO-published-api-auth-ratelimit-cors`)* — **Status:** Open（P1）。**问题：** 入站端点对用户 API 无访问控制（现有 auth 仅出站 trust token）。**规格：** API key/token、按 API 限流/配额、CORS 配置。

- [ ] **每个 API 自定义路径 / 方法** *(label: `TODO-published-api-custom-routing`)* — **Status:** Open（P1）。**问题：** 全部走 `POST /api/workflow?applicationName=`，API 无独立身份。**规格：** 自定义路径/方法/路径参数，URL 自解释。

- [ ] **一等公民"调用另一个工作流"节点 + API 目录** *(label: `TODO-call-workflow-node-and-catalog`)* — **Status:** Open（P2）。**规格：** 从注册表挑已发布 workflow（带契约）的节点；加 API 目录用于发现。

## C. 复核依赖（环境）

- [ ] **测试环境 — 放行 UAT egress 以实跑 `tests/`** *(label: `TODO-tests-egress-allowlist-uat-hosts`)* — **Status:** Open（环境配置）。**问题：** 当前沙箱 egress 拦截 `workflow-ui-gamma.vercel.app`、`workflow-operation-api-n9sbp.ondigitalocean.app`、`workflow-online-api-nr3e4.ondigitalocean.app`（`Host not in allowlist`），`tests/` 套件无法实跑，审计中需实跑的项标记 BLOCKED(env)。**规格：** 将三 host 加入环境 egress 允许列表后运行 `tests/`，回填审计报告 §3–§5。**追溯：** 审计报告 §0/§8。
