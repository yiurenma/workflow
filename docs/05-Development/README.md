# 开发体系总览

## 这是什么？
开发实施阶段，分为**前端、后端、数据库**三个团队并行开发。

## 📊 当前开发状态

| 团队 | 当前版本 | 状态 | Repo | 最后更新 |
|------|---------|------|------|----------|
| [前端](01-Frontend/README.md) | v43.0 | ✅ 已完成 | `workflow-ui` | 2026-04-19 |
| [后端](02-Backend/README.md) | v43.0 | ✅ 已完成 | `workflow-operation-api` + `workflow-online-api` | 2026-04-19 |
| [数据库](03-Database/README.md) | - | ⏸️ 无变更 | 共享 Neon PostgreSQL | - |

## 🗂️ 待开发 TODO

**当前活跃开发任务：**
- 🔴 [E2E 测试选择器更新](../01-team/TODO.md#TODO-e2e-carbon-rewrite-selector-updates) - **Frontend** - HIGH
- 🟡 [部署重写](../01-team/TODO.md#TODO-deploy-rewrite-online-api-workflow-json) - **Frontend** - OPEN
- 🟡 [SSE 流式返回](../01-team/TODO.md#TODO-online-api-post-optional-sse-runtime-per-step) - **Backend** - OPEN

## 📍 快速导航

| 我要... | 去哪里 |
|---------|--------|
| 💻 看前端实现 | [01-Frontend/README.md](01-Frontend/README.md) |
| 💻 看后端实现 | [02-Backend/README.md](02-Backend/README.md) |
| 🗄️ 看数据库变更 | [03-Database/README.md](03-Database/README.md) |

## 🔄 开发流程

```
Delivery Manager 分配任务
       ↓
Frontend + Backend + Database 并行开发
       ↓
Database 先完成（如有 schema 变更）
Backend 依赖新 schema 开发
Frontend 调用新 API
       ↓
Delivery Manager 协调 commit + merge + push
       ↓
进入测试阶段（QA + E2E）
```

## 📂 子目录说明

- `01-Frontend/` - React 18 + Vite + Carbon + React Flow
- `02-Backend/` - Spring Boot 4.0.3 + JDK 21
- `03-Database/` - Neon PostgreSQL + Flyway migrations
