# 文档 ↔ 实现 审计报告 v1.0

**日期：** 2026-06-21
**审计范围：** 产品基线 `pm-doc-master.md` v2.29（APP / REC / CV 三域全部 US/AC）+ 最新 arch/test 文档 + 全部 238 篇文档的归类核对
**方法：** 对照源码逐项核验（read-only）。计划原定用 Playwright 实跑 UAT，但**当前沙箱 egress 策略拦截了三个 UAT 线上地址**（`Host not in allowlist`），无法实跑。故本轮验证以**源码核对**为证据；测试套件已写入 `tests/`，待 host 加入 egress 允许列表后即可实跑复核。

## 0. 环境与可达性

| 目标 | 结果 |
|---|---|
| `workflow-operation-api-n9sbp.ondigitalocean.app` | ❌ BLOCKED — `Host not in allowlist`（egress 未放行） |
| `workflow-online-api-nr3e4.ondigitalocean.app` | ❌ BLOCKED — 同上 |
| `workflow-ui-gamma.vercel.app` | ❌ BLOCKED — 同上 |

> 影响：所有需要**实跑** UAT 的验收项（Layer 4 交互 / Layer 5 效果 / API 实测）在本轮标记 **BLOCKED(env)**，但均给出**源码层**判定。

## 1. 总体结论

- **文档中已标记 DONE 的能力，源码层面全部落实** —— APP（应用管理）、CV（画布/节点编辑器/导入/AI）、REC（在线执行/丰富化/下发/幂等/重试）核心能力均能在三个服务仓库找到对应实现，证据见下表。
- **真正的实现缺口 = 4 个早已 Open 的 TODO**（SSE、deploy 重写、canvas Test SSE、E2E 选择器）。审计**确认**了它们确实未实现/未完成，并转写为 `TODO-doc-gaps.md` 条目。
- **2 处文档↔实现偏差（DIV）**：deploy 三步的"端点命名"与历史回滚的"服务端 vs 客户端"实现方式，功能可用但文档表述与代码结构不完全一致，建议在 arch 文档澄清。

| 判定 | 数量 | 说明 |
|---|---|---|
| ✅ 已实现 (Implemented) | 大多数 APP/CV/REC 验收项 | 见 §3–§5 |
| ⚠️ 偏差 (Divergent) | 2 | DIV-1 deploy 端点命名；DIV-2 历史回滚实现位置 |
| ❌ 缺失 (Missing) | 4 | 对应 4 个 Open TODO（GAP-1~4） |
| ⛔ BLOCKED(env) | 全部需实跑项 | egress 拦截，源码已判定 |

## 2. 238 篇文档归类核对

| 类别 | 数量 | 审计处理 |
|---|---|---|
| 产品基线 `pm-doc-master.md` (v2.29) | 1 | **逐 US/AC 核对**（§3–§5） |
| `pm-doc-v*.md` 切片 | 48 | 历史切片，最新 v43.0 已并入基线；旧版标记 superseded（被基线取代），不逐条复核 |
| `arch-doc-v*.md` | 49 | 同上；最新版作为接口契约来源核对 |
| `test-doc-v*.md` | 48 | 同上；TC 用例已并入 `TEST_CASES_MASTER.md` |
| `ui/e2e/uat` 报告 | 72 | **历史记录**，非规范。核对其覆盖的功能今天是否仍成立、其中记录的 FAIL 是否已修复（见 §6） |
| guides / programs / postmortem | 18 | 引用核对（URL、环境名、env 变量是否仍准确） |
| baseline 其余 (TEST_CASES_MASTER, agent-assignments) | 2 | 作为测试用例来源 |

## 3. 【APP】应用管理 — 逐项

| US/AC | 能力 | 判定 | 证据（文件:行 / 说明） |
|---|---|---|---|
| APP-US-01 | 列表/搜索/分页(默认5)/桌面表+移动卡切换+偏好持久化/空状态 | ✅ | `workflow-ui/src/routes/workflows/index.tsx`（pageSize=5；搜索；表/卡；空状态）。后端 `WorkflowEntitySettingController:74-78` QueryDSL 模糊查询 |
| APP-US-02 | 创建应用 + 移动 FAB 可拖/位置记忆 | ✅ | `workflows/index.tsx`（FAB pointer 拖拽、localStorage `workflow_fab_pos`） |
| APP-US-03 | 删除 + 确认框；不因历史记录禁止删除 | ✅ | UI `CarbonModal.carbonConfirm()`；后端 `WorkflowDeleteController:64-93`「report/record kept」保留记录；`WF-409-201` guard 已移除 |
| APP-US-18 | 编辑应用设置（全字段） | ✅ | UI `settings-modal/index.tsx`；后端 `WorkflowEntitySettingController:153-225` PATCH(enabled/asyncMode/retry/tracking/eimId/region/...) |
| APP-US-10 | 跨应用复制（含设置+完整工作流定义） | ✅ | UI Copy 弹窗；后端 `WorkflowAutoCopyController:49-101`（同源拒绝 WF-400-301、源不存在 WF-400-302、复制 entity+pluginList） |
| APP-US-11 | 配置历史与回滚 | ⚠️ DIV-2 | 历史：后端 `WorkflowEntitySettingController:102-121`（Envers）。**回滚无独立服务端端点**，由 UI `history-drawer` 解码旧定义后 `saveWorkflow()` 重存实现（功能可用，产生新修订）。建议 arch 文档注明回滚为客户端重存语义 |
| APP-US-52 | Deploy 部署（5字段/三步/进度/CORS代理） | ✅ + ⚠️ DIV-1 | UI `DeployModal.tsx`（5字段、三步顺序、进度、桌面+移动入口、跨域自动走代理）。CORS 代理后端 `DeployProxyController:11-49`。**DIV-1**：文档的三步名 `CreateApplicationName/UpdateApplicationName/SaveWorkflow` 非真实端点名 —— 实际映射到 `POST/PATCH /entity-setting` + `POST /workflow`。功能等价，命名需 arch 澄清。**实测 BLOCKED(env)** |

## 4. 【REC】运行记录与在线执行 — 逐项

| US/AC | 能力 | 判定 | 证据 |
|---|---|---|---|
| REC-US-19 | 记录浏览/筛选/下钻/父子重试链 | ✅ | UI `routes/records/index.tsx`；后端 `WorkflowRecordController:57-120`（筛选 app/status/确认号/跟踪号/客户ID/日期；详情含子记录 originWorkflowRecordId） |
| REC-US-12 | 对接方提交运行请求 | ✅ | `WorkflowOnlineController.postWorkflow():115`，`POST /api/workflow`（query applicationName/confirmationNumber，header x-request-id） |
| REC-US-13 | 丰富化：按连线顺序、JSONPath 全匹配、合并、无隐式默认分支 | ✅ | `WorkflowDispatchService:70-131` 按 logicOrder 排序；`ruleAndTypesFullyMatch():200-223` 全匹配才执行；`DefaultWorkflowRuntimePayloadImpl:64-87` 合并 runtime |
| REC-US-14 | 下发：丰富化后执行、各通道状态写入、总状态组合、通道可扩展 | ✅ | `WorkflowDispatchService:126-183`；`WorkflowRuleAndTypeService:105-146`（SM_SUCCESS/SM_FAIL、tracking）；`DispatchStepStatus`（sms/email/push 列表，可扩展） |
| REC-US-15 | 幂等：同应用同业务键唯一、重复拒绝 | ✅ | `WorkflowOnlineController:204-209`（requestId+applicationName 查重 → M0002）；索引 `idx_workflow_record_corr_app` |
| REC-US-16 | 失败重试：可关联记录、次数追溯、终态明确 | ✅ | `originWorkflowRecordId`+`retryTimes` 字段；`WorkflowRunStatus.RETRY_ALL_FAIL` 终态；自请求例外 M0004 |
| —（运行模式） | 同步/异步与应用配置一致 | ✅ | `WorkflowOnlineController:240-244` 依 `isAsyncMode()` 分支；`@Async dispatchFromPersistedRecord` |
| —（runtime 落库） | 每步执行后 runtime 写入 DB | ✅ | `WorkflowDispatchService:117-118,156-160,177-180` save；AES/CBC 加密 `SecureData:73-88` |

## 5. 【CV】画布 — 逐项

| US/AC | 能力 | 判定 | 证据 |
|---|---|---|---|
| CV-US-04~09 | 打开画布/加步骤/连线/配置/删节点(清边)/保存 | ✅ | `worflow-canvas/`(@xyflow/react)、`workflow-sider/`(拖放调色板)、`workflow-drawer/`、`useWorkflowConnections.ts`(删节点重算边) |
| CV-US-32/33/34/37 | 节点编辑器无障碍/对比度/关闭按钮 | ✅ | `workflow-drawer/index.tsx`（aria-label「Node Configuration」、× 关闭、点空白关闭、只读默认+编辑按钮） |
| CV-US-40/50 | 规则键单 JSONPath 校验（jsonpath-plus、失焦、错误禁用保存） | ✅ | `RuleInput.tsx` + `utils/validateRuleKey.ts`（import jsonpath-plus） |
| CV-US-17/20/21/44 | Test 试跑 / AI Explain / 设备码授权 / AI Generate | ✅ | `workflow-header/`（executeRun→onlineApi）、`useAIExplain`、`useGitHubDeviceFlow.ts`、`WorkflowGeneratorModal.tsx` |
| CV-US-53/54/55/57 | JSON 导入：粘贴/上传、剥围栏、6 种插件类型、IFELSE 宽松、移除边引用必存在检查、预览、确认替换 | ✅ | `ImportWorkflowModal.tsx`（VALID_PLUGIN_TYPES=6 种、`isIFELSEBranchId`、已移除 edge-exists 检查、预览摘要、window.confirm） |
| CV-US-35/36/39/48/49 | 视觉设计系统(IBM Carbon)/残留样式审计/Carbon 合规 | ✅(源码) / ⛔ 实测 | `.cds-*` + 自定义类，`theme.ts`/`index.css`；**Layer5 计算样式与视觉回归需实跑，BLOCKED(env)** |
| CV-US-45/46 | 主页介绍文案 / 蜗牛 favicon | ✅ | `WorkflowStudioIntro.tsx`、`routes/index.tsx` hero；`public/favicon.svg`（蜗牛、#0f62fe） |
| CV-US-51 | 前端重构（设计交付像素还原、零回归） | ✅ | 已重建为 Carbon（`.cds-*`），TODO 标记 DONE（UAT PASS 2026-04-19） |
| CV-US-38/47/48 | Playwright 五层验证 / E2E 套件 | ⚠️ 见 GAP-4 | `workflow-ui/e2e/*.spec.ts` + `playwright.config.ts`(baseURL=gamma)。**但** v44.0 报告 81/90 失败（选择器仍找 `.ant-*`），已 Open |

## 6. 实现缺口（→ 已转写为 TODO-doc-gaps.md）

| 编号 | 缺口 | 来源文档 | 源码确认 | 对应已存在 TODO |
|---|---|---|---|---|
| GAP-1 | online-api POST 可选 SSE：按请求头开关、每步完成后推送 DB 已存 runtime | 基线无（Open TODO）/ 待 arch | ❌ 全仓零 `SseEmitter`/`text/event-stream`，`postWorkflow` 返回 `ResponseEntity<Void>` 单次响应 | `TODO-online-api-post-optional-sse-runtime-per-step` |
| GAP-2 | 画布 Test：SSE 逐步展示每步响应 + 大 JSON 性能 | Open TODO | ❌ 依赖 GAP-1，UI 无 SSE 消费 | `TODO-canvas-test-sse-per-step-response-ui-performance` |
| GAP-3 | Deploy 重写：点击后自动调 online API、执行名 vs 源应用、双块请求体、JSON 工作流 + AI Generate | Open TODO | ⚠️ 现为三步 operation-api 调用，未改为 online-api 单调 + 双块体 | `TODO-deploy-rewrite-online-api-workflow-json` |
| GAP-4 | E2E 选择器 Carbon 化：81/90 失败，测试仍查 `.ant-*` | `e2e-test-report-v44.0.md` | ⚠️ 部分 e2e spec 已用 `.cds-*`/role，但仍有大量 `.ant-*` 残留致失败 | `TODO-e2e-carbon-rewrite-selector-updates` |

## 7. 文档↔实现偏差（建议 arch 澄清，非阻断）

- **DIV-1（Deploy 三步命名）**：`pm-doc-master` APP-AC-52-D4 与多版 arch 用 `CreateApplicationName/UpdateApplicationName/SaveWorkflow` 作为"路径"，实际不存在同名端点，UI 映射到 `POST/PATCH /api/workflow/entity-setting` + `POST /api/workflow`。建议在最新 arch 文档把"逻辑三步 ↔ 真实端点"对应表写清。
- **DIV-2（历史回滚）**：APP-AC-11-D2 描述"仅回滚工作流定义、产生新修订"，实现为**客户端**解码旧定义后重存（无服务端 rollback 端点）。功能满足，但 arch 文档宜注明实现位置在前端。

## 8. 复核方式（egress 放行后）

1. 在环境 egress 允许列表加入三个 UAT host。
2. `cd tests && npm install && npx playwright install chromium && npx playwright test`。
3. 用 `tests/` 输出的 PASS/FAIL 回填本报告 §3–§5 中 BLOCKED(env) 行；任何新 FAIL 追加为新的 `TODO-doc-gaps.md` 条目。
