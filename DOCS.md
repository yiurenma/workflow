# Workflow 平台 — 文档总导航 DOCS

仓库根的单一文档入口。整理目标：把分散的文档收拢到 `docs/`，并对每篇文档做"文档↔实现"审计。

> **整理原则：** 本目录下 `docs/` 是 submodule `workflow-agent-teams/docs/`（238 篇）+ 根级 `TODO.md`/`agent-system.md` 的**只读整理副本**。**未改动任何 submodule 文件**；原档与既有 agent 流程（`CLAUDE.md` 指向的路径）保持不变。Git 历史仍在各 submodule。

## 快速入口

| 我要… | 去 |
|---|---|
| 看产品要做什么（基线） | `docs/baseline/pm-doc-master.md`（v2.29，中文 US/AC） |
| 看文档分类与最新版本 | `docs/INDEX.md` |
| 看"文档是否真的实现了"的审计结论 | `docs/doc-implementation-audit-v1.0.md` |
| 看审计发现的缺口/待办 | `docs/TODO-doc-gaps.md` |
| 跑测试驱动审计套件 | `tests/`（见下方"如何跑测试"） |
| 看接口契约/错误码 | `docs/arch/arch-doc-v43.0.md`（最新） |
| 看主测试用例 | `docs/baseline/TEST_CASES_MASTER.md` |

## 各 repo 文档

| Repo | 文档 |
|---|---|
| 根 `workflow` | 本文件 `DOCS.md`、流程规则 `CLAUDE.md`、整理副本 `docs/`、测试套件 `tests/` |
| `workflow-ui`（前端） | `workflow-ui/README.md`、`AGENTS.md`、`REBUILD_PROGRESS.md` |
| `workflow-operation-api`（控制面） | `README.md`、`AGENTS.md`、`docs/agent-system.md`、`docs/agents/*.md` |
| `workflow-online-api`（执行面） | `README.md`、`AGENTS.md` |
| `workflow-agent-teams`（流程中枢，**文档原档**） | `docs/`（238 篇原档）、`TODO.md`、`agent-system.md` |
| `.claude`（Claude 配置） | `agents/*.md`（14 角色定义）、`plugins/ibm-carbon-design/`（设计系统 skill） |

## 服务拓扑

- **workflow-ui** — React 18 / Vite / TS / TanStack / React Flow / Carbon。前端经 env `VITE_OPERATION_API_BASE`、`VITE_ONLINE_API_BASE` 调两个后端。
- **workflow-operation-api** — 控制面（Spring Boot 4.0.3 / JDK 21）：应用 CRUD、entity-setting、autoCopy、history、records、deploy 代理。
- **workflow-online-api** — 执行面（Spring Boot 4.0.3 / JDK 21）：`POST /api/workflow` 单入口、丰富化→下发、幂等、重试。
- 两后端**共享同一 Neon PostgreSQL**。

## UAT 环境

| 服务 | URL |
|---|---|
| 前端 | https://workflow-ui-gamma.vercel.app |
| operation-api | https://workflow-operation-api-n9sbp.ondigitalocean.app |
| online-api | https://workflow-online-api-nr3e4.ondigitalocean.app |

> ⚠️ 当前沙箱 egress 未放行以上 host（`Host not in allowlist`），`tests/` 无法在此实跑——见审计报告 §0。

## 如何跑测试（egress 放行后）

```bash
cd tests
npm install
npx playwright install chromium
npx playwright test            # API + E2E，对 UAT 环境
```

每个测试标题标注其编码的文档/AC；结果 PASS/FAIL 回填 `docs/doc-implementation-audit-v1.0.md`。
