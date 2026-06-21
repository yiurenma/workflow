# 文档索引 INDEX

整理自 `workflow-agent-teams/docs/`（238 篇）+ 仓库根 `TODO.md`/`agent-system.md`，按类型归入子文件夹。**本目录为只读整理副本**；submodule 内原档保持不变，既有 agent 流程仍指向原档。

**最新版本一览（截至整理时）：** PM v43.0 · Arch v43.0 · Test v43.0 · UI 报告 v43.0 · E2E 报告 v44.0 · UAT 报告 v43.0 · 产品基线 `pm-doc-master` v2.29

## 0. 入口与审计（新增）

| 文件 | 用途 |
|---|---|
| `../DOCS.md` | 仓库根总导航（跨所有 repo） |
| `doc-implementation-audit-v1.0.md` | **文档↔实现审计报告**（逐 US/AC 核对结果） |
| `TODO-doc-gaps.md` | 审计发现的缺口待办（独立于 submodule TODO）——**顶部有「🗂️ 待办登记表」索引：先扫状态/优先级，再跳小节；UI bug 配截图** |
| `reports/uat/uat-report-v45.0.md` | 最近一次画布全功能 UAT 实跑（真实 UAT）结论 + bug 截图 |
| `reports/uat/screenshots/` | UI bug 截图证据（按报告版本分目录，如 `v45/`） |
| `../tests/` | 测试驱动审计套件（Playwright API + E2E）；bug 截图复现脚本 `../tests/scripts/capture-bug-screenshots.mjs` |

## 1. 产品基线 baseline/

| 文件 | 用途 | 状态 |
|---|---|---|
| `baseline/pm-doc-master.md` | **单一产品基线**（中文，US/AC，固定文件名）v2.29 | 当前 |
| `baseline/TEST_CASES_MASTER.md` | 主测试用例注册表（TC-*，238 项） | 当前 |
| `baseline/agent-assignments-v1.0.md` | 角色分配快照 | 快照 |

## 2. PM 切片 pm/

`pm-doc-v1.0 → v43.0`（含 v1.2，缺 v17/v26；v32.0 拆 6 part）。**最新：`pm-doc-v43.0.md`**。
后验：`pm/postmortem/`（cors-oauth、gho-token）。旧版为历史切片，已并入 `baseline/pm-doc-master.md`。

## 3. 架构 arch/

`arch-doc-v1.0 → v43.0`（缺 v17；v32.0 拆 6 part）。**最新：`arch-doc-v43.0.md`**（接口契约/错误码权威来源）。
后验：`arch/postmortem/`（cors-oauth、gho-token）。

## 4. 测试用例 test/

`test-doc-v1.0 → v43.0`（缺 v17；v32.0 拆 6 part）。**最新：`test-doc-v43.0.md`**。
后验：`test/postmortem/`（cors-oauth、gho-token）。

## 5. 报告 reports/

| 子目录 | 内容 | 最新 |
|---|---|---|
| `reports/ui/` | QA 手测报告 `ui-test-report-v*.md` + `qa-report-v19-v23.md` | v43.0 |
| `reports/e2e/` | Playwright E2E 报告 `e2e-test-report-v*.md` | **v44.0**（81/90 失败，选择器待更新，见审计 GAP-4） |
| `reports/uat/` | UAT 报告 `uat-report-v*.md` + `uat-script-v25/26.0.md` | v43.0 |

> 报告为**历史记录**（某时点快照），非规范。审计仅核对其覆盖功能今天是否仍成立。

## 6. 指南 guides/

| 文件 | 用途 |
|---|---|
| `guides/e2e-testing-guide.md` | E2E 测试方法论 |
| `guides/e2e-full-test-guide.md` | E2E 全量测试编写/执行参考（五层 UX 框架） |

## 7. 项目程序 programs/v32.0/

v32.0 统一程序全套快照（index/progress/approval/completion 共 10 篇）+ `delivery-plan-v1.0.md` + `unified-program-status-v36.0.md`。多为历史进度/完成标记。

## 8. 根级语料（拷贝自 workflow-agent-teams 根）

| 文件 | 用途 |
|---|---|
| `TODO.md` | 原始 backlog 副本（权威原档仍在 submodule） |
| `agent-system.md` | 自动化角色体系（中文） |
