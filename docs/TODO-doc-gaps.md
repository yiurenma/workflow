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

## E. UI 体现文档更新（用户文档 → 产品内可见，2026-06-21）

> 来源：用户要求"文档更新如何在 UI 体现"。这些是 **workflow-ui 功能**，需走完整 PM→Arch→Test→批准→实现流程（会动 workflow-ui 子模块）。按价值排序。

- [ ] **UI — What's New / 更新提示（CHANGELOG 驱动）** *(label: `TODO-ui-surface-docs-whatsnew-help`)* — **Status:** Open（P0，最直接的"文档→UI"闭环）。**目标:** 读 `docs/promo/CHANGELOG.md`，导航上加 What's New 入口 + 未读红点（localStorage 记已读版本，看过即清）；点开为更新抽屉。**架构决策（需 Arch 定）:** UI 如何取根仓库 markdown —— 构建时把 CHANGELOG 拷进 workflow-ui / 起文档站让 UI 链过去 / 提供 docs 接口。最小可行 = 构建时打入 CHANGELOG。**追溯:** CLAUDE.md「User-facing docs maintenance」。

- [ ] **UI — Docs/Help 页（升级现有 about）** *(label: `TODO-ui-docs-help-page`)* — **Status:** Open（P1）。**目标:** 把现有空壳 `workflow-ui/src/routes/about.tsx` 升级为文档/帮助中心，渲染 `docs/guide/` 或链到文档站；导航 NAV 数组（`src/routes/__root.tsx`）加 "Docs" 项（桌面 + 移动 tab bar）。**追溯:** docs/guide。

- [ ] **UI — 上下文帮助（节点/规则/空状态）** *(label: `TODO-ui-contextual-help-links`)* — **Status:** Open（P1）。**目标:** 节点编辑器每种节点的 help 文案取自 `reference/nodes.md`；规则字段旁 "?" 链到 `reference/rules-jsonpath.md`；应用/记录空状态链到入门指南。**追溯:** docs/guide/reference。

- [ ] **UI — 每个 API 的"如何调用"面板** *(label: `TODO-ui-call-this-api-panel`)* — **Status:** Open（P1，贴 API maker 定位）。**目标:** 在应用/画布放 "Call this API" 面板，自动用 `applicationName` 拼出调用契约（路径/必需头 `X-Request-Correlation-Id`/参数）+ 可复制 curl。**依赖/呼应:** `TODO-per-workflow-api-contract-openapi`（每 API 契约/OpenAPI）。**追溯:** docs/guide/reference/api-call.md。

## F. 测试体系发现的真实缺陷（2026-06-21，黑盒套件 `tests/` 实跑抓到）

> 由新建的可用性/无障碍套件实跑发现，均为 workflow-ui 缺陷（走完整流程修复）。已在 `tests/` 用基线豁免/`@advisory` fixme 标记，修复后移除标记即转回归。

- [ ] **UI 无障碍 — /records 表单控件缺标签** *(label: `TODO-ui-a11y-records-form-labels`)* — **Status:** Open（critical）。**证据:** axe WCAG 2.2 AA 扫 `/records`：`label` ×2、`select-name` ×1（表单输入/下拉无可访问名）。**修复:** 给筛选输入/下拉补 `<label>`/`aria-label`。**追溯:** `tests/a11y/records.a11y.spec.ts` 基线豁免。

- [ ] **UI 无障碍 — 画布文字对比度不足** *(label: `TODO-ui-a11y-canvas-color-contrast`)* — **Status:** Open（serious）。**证据:** axe 扫画布：`color-contrast` ×6，文字/背景对比度 < 4.5:1。**修复:** 调整相关文字颜色达 WCAG AA。**追溯:** `tests/a11y/canvas.a11y.spec.ts` 基线豁免。

- [ ] **UI 无障碍 — 移动端导航激活态文字对比度不足** *(label: `TODO-ui-a11y-mobile-nav-contrast`)* — **Status:** Open（serious）。**证据:** axe 扫移动视口（390px）多页：移动 tab bar 激活态链接文字 `color-contrast` < 4.5:1（共享 nav，跨 /workflows、/、/records）。**修复:** 调整移动激活态文字/背景色达 WCAG AA。**追溯:** `tests/a11y/*` 移动端基线豁免。

- [ ] **UI 可用性 — 模态支持 Esc 关闭（WAI-ARIA 对话框模式）** *(label: `TODO-ui-modal-esc-close-wai-aria`)* — **Status:** Open（usability）。**证据:** 新建/导入等模态有 ×/Cancel 但**无 Esc 关闭**（违反 WAI-ARIA Authoring Practices 对话框模式）。**修复:** 模态统一加 Esc 关闭处理。**追溯:** `tests/ux/*` `@advisory` fixme。

- [ ] **UI 画布 — 节点抽屉 Done 未随规则错误禁用（CV-AC-50-4）** *(label: `TODO-ui-drawer-done-disable-on-rule-error`)* — **Status:** Open（轻微）。**证据:** 画布全功能 E2E：节点抽屉编辑态规则键填非法 JSONPath（如 `a, b`）会显示内联红错（"single JSONPath expression"），但提交按钮 **Done 未被禁用**；CV-AC-50-4 要求"验证错误时禁用保存按钮"。**修复:** 任一规则键有错时禁用 Done。**追溯:** `tests/e2e/canvas-full.spec.ts` E2（@advisory fixme）。

- [ ] **测试 — 画布富交互的移动端覆盖 + UAT 真后端流程** *(label: `TODO-tests-canvas-mobile-and-uat-flows`)* — **Status:** Open（2026-06-21 实跑确认，证据更具体）。**目标:** (1) 移动端画布工具栏在溢出菜单内，需补"打开溢出菜单"以解锁移动端的 import/配置/删除等富交互 E2E；(2) `@uat` 标记的真后端流程（Run 真执行返回摘要、Save 持久化重载、AI Explain/Generate 真返回）需 UAT egress 放行后用 `RUN_UAT=1` 实跑。**实跑发现（egress 已放行后）:** `@uat G2`（Run 真执行）与 `@uat I2`（Save 持久化）当前是**空壳/无断言**——`I2` body 为空（仅 `test.skip`），`G2` 点 Run 后无任何断言（验证仅为注释）。因此桌面端 `RUN_UAT=1` 下二者**平凡通过**（不真正验证后端）；移动端 `@uat G2` 反而**超时失败**，因为 Run 按钮在移动溢出菜单内、用例未先打开溢出菜单。**修复:** (a) 给 `@uat` 用例补真断言（Run 后断响应摘要可见/含状态；Save 后重载断节点持久化）；(b) `@uat` 富交互用例加视口守卫或先开溢出菜单，避免移动端误失败。**追溯:** `tests/e2e/canvas-full.spec.ts`；详见 `docs/reports/uat/uat-report-v45.0.md`。

- [ ] **测试 — CI 接线（各 repo 配置阻断合并）** *(label: `TODO-tests-ci-wiring-gate`)* — **Status:** Open。**目标:** 在各 repo CI 上跑 `tests/` 的 `--grep @gate` 作为合并门禁（需动 submodule CI 配置）。`@advisory` 出报告不阻断。**追溯:** `tests/TEST-STRATEGY.md`。

## C. 复核依赖（环境）

- [x] **测试环境 — 放行 UAT egress 以实跑 `tests/`** *(label: `TODO-tests-egress-allowlist-uat-hosts`)* — **Status:** Verified（2026-06-21，已放行并实跑）。**问题（历史）：** 沙箱 egress 曾拦截 `workflow-ui-gamma.vercel.app`、`workflow-operation-api-n9sbp.ondigitalocean.app`、`workflow-online-api-nr3e4.ondigitalocean.app`（`Host not in allowlist`），`tests/` 无法实跑。**结果：** 三 host 已加入自定义 allowlist；curl 确认前端 200、operation-api 200、online-api 500（应用级 `No static resource for '/'`，非 allowlist 拦截）；画布全功能 E2E 已对真实 UAT 实跑（见 `uat-report-v45.0.md`）。**遗留环境注意（给任何沙箱 runner）：** egress 网关做 **TLS 拦截**，私有 CA 在系统 bundle 受信（curl/node OK）但 Playwright 自带 Chromium 不认（`ERR_CERT_AUTHORITY_INVALID`）——需 `IGNORE_HTTPS_ERRORS=1`（`playwright.config.ts` 已加 env 开关，默认严格）。画布路由 `/workflows/:app` 需真实存在的应用方能渲染，跑前用 operation-api 建临时应用、`CANVAS_APP` 指向它、跑完删除。**追溯：** 审计报告 §0/§8；`docs/reports/uat/uat-report-v45.0.md`。
