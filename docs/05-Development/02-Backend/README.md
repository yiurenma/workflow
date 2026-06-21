# Backend 开发索引

## 我是谁？
后端开发者（Backend Developer），负责实现：
- `workflow-operation-api`（控制面 - 应用 CRUD）
- `workflow-online-api`（执行面 - 工作流运行）

## 📊 当前状态

**最新实现：**
| 版本 | 标题 | 改动范围 | 状态 | 日期 |
|------|------|----------|------|------|
| v43.0 | （该版本无后端改动） | - | ⏸️ 跳过 | 2026-04-19 |
| v42.0 | CORS 代理 API 实现 | `DeployProxyController` | ✅ 已完成 | 2026-04-19 |
| v41.0 | （该版本无后端改动） | - | ⏸️ 跳过 | 2026-04-19 |

## 📋 待开发 TODO

**当前活跃后端任务：**
- 🟡 [SSE 流式返回](../../01-team/TODO.md#TODO-online-api-post-optional-sse-runtime-per-step) - OPEN

查看完整待办 → [../../01-team/TODO.md](../../01-team/TODO.md)

## 🗂️ 工作日志说明

`work-logs/` 目录将存放每个版本的后端实现日志。

**目前暂无日志** - 开发日志将在后续版本中补充

## 📖 后端实现日志格式

每个后端实现日志（`work-logs/backend-vX.0.md`）应包含：

1. **实现范围** - 这个版本改了哪些 API
2. **API 契约** - 新增/修改的接口定义（与 Arch 文档对齐）
3. **数据库变更** - 是否有新表/字段/索引
4. **技术决策** - 为什么选择这个实现方式
5. **测试验证** - 如何用 curl/Postman 验证

## 💻 技术栈

- **框架：** Spring Boot 4.0.3
- **Java 版本：** JDK 21
- **数据库：** Neon PostgreSQL（共享）
- **迁移工具：** Flyway
- **连接池：** HikariCP
- **构建工具：** Maven

## 📦 代码仓库

- **operation-api:** commit on `main`, push to `origin/main`
- **online-api:** commit on `develop`, push to `origin/develop`
