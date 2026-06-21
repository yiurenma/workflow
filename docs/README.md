# Workflow 平台文档总入口

## 📊 当前状态概览

| 模块 | 当前版本 | 状态 | 最后更新 |
|------|---------|------|----------|
| 产品需求 | v43.0 | ✅ 已完成 | 2026-04-19 |
| 架构设计 | v43.0 | ✅ 已完成 | 2026-04-19 |
| 前端开发 | v43.0 | ✅ 已完成 | 2026-04-19 |
| 后端开发 | v43.0 | ✅ 已完成 | 2026-04-19 |
| 数据库 | - | ⏸️ 无变更 | - |
| QA 测试 | v43.0 | ✅ 6/6 PASS | 2026-04-19 |
| E2E 测试 | v44.0 | ⚠️ 81 failures | 2026-04-19 |
| UAT 验收 | v45.0 | ✅ 29/30 PASS | 2026-06-21 |

## 🗂️ 待办事项

**当前活跃 TODO：** 
- 🔴 [E2E 测试选择器更新](01-team/TODO.md#TODO-e2e-carbon-rewrite-selector-updates) - HIGH
- 🟡 [部署重写](01-team/TODO.md#TODO-deploy-rewrite-online-api-workflow-json) - OPEN
- 🟡 [SSE 流式返回](01-team/TODO.md#TODO-online-api-post-optional-sse-runtime-per-step) - OPEN

详见 → [01-team/TODO.md](01-team/TODO.md)

## 📍 快速导航

| 我要... | 去哪里 |
|---------|--------|
| 📋 看产品要做什么 | [02-PM/baseline.md](02-PM/baseline.md) (v2.29) |
| 🏗️ 看最新技术方案 | [03-Architect/work-logs/arch-doc-v43.0.md](03-Architect/work-logs/arch-doc-v43.0.md) |
| 💻 看前端最新实现 | [05-Development/01-Frontend/README.md](05-Development/01-Frontend/README.md) |
| 🧪 看测试体系 | [06-Testing/README.md](06-Testing/README.md) |
| ✅ 看最新 UAT 报告 | [06-Testing/03-E2E/uat-reports/uat-report-v45.0.md](06-Testing/03-E2E/uat-reports/uat-report-v45.0.md) |
| 📖 学产品怎么用 | [98-product-docs/02-getting-started.md](98-product-docs/02-getting-started.md) |

## 📂 目录结构

```
01-team/            团队协作（TODO、角色分配、工作流程）
02-PM/              产品需求（baseline + 工作日志）
03-Architect/       架构设计（技术方案 + API 契约）
04-Delivery-Manager/ 交付协调（实施计划 + 进度跟踪）
05-Development/     开发实现
  ├── 01-Frontend/    → 前端（workflow-ui）
  ├── 02-Backend/     → 后端（operation-api + online-api）
  └── 03-Database/    → 数据库（schema + migrations）
06-Testing/         测试验证
  ├── 01-Test-Manager/ → 测试用例设计
  ├── 02-QA/          → 手动测试
  └── 03-E2E/         → 自动化测试 + UAT
97-postmortem/      事后分析（重大事件复盘）
98-product-docs/    产品文档（面向外部用户）
```

## 🔄 工作流程

```
TODO 领取 → PM 写需求 → Architect 写方案 → Test Manager 写用例 
  → 人类审批（Gate 1）
  → Delivery Manager 协调 → 开发实施（Frontend + Backend + Database）
  → QA 手测 → E2E 自动化测试 → UAT 验收
  → 人类确认（Gate 2）
  → 标记 Done
```

## 🏢 团队角色

| 角色 | 职责 | 工作区 |
|------|------|--------|
| PM | 定义产品功能 | `02-PM/` |
| Architect | 设计技术方案 | `03-Architect/` |
| Delivery Manager | 协调实施 | `04-Delivery-Manager/` |
| Frontend Dev | 前端开发 | `05-Development/01-Frontend/` |
| Backend Dev | 后端开发 | `05-Development/02-Backend/` |
| Database Dev | 数据库开发 | `05-Development/03-Database/` |
| Test Manager | 测试用例设计 | `06-Testing/01-Test-Manager/` |
| QA | 手动测试 | `06-Testing/02-QA/` |
| E2E Tester | 自动化测试 + UAT | `06-Testing/03-E2E/` |
