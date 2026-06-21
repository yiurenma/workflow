# Delivery Manager 工作索引

## 我是谁？
交付经理（Delivery Manager），负责**协调实施**。在 PM、Architect、Test Manager 三份文档都通过人类审批后，我开始：

1. **分配任务** - 把工作拆给 Frontend 和 Backend 开发者
2. **追踪进度** - 确保开发按时完成
3. **管理风险** - 识别阻塞项，协调解决
4. **最终交付** - Commit + Merge + Push 所有改动的 repos

## 📊 当前状态

**最新交付：**
| 版本 | 标题 | 涉及 Repo | 状态 | 日期 |
|------|------|----------|------|------|
| v43.0 | 导入校验放宽 | workflow-ui | ✅ 已交付 | 2026-04-19 |
| v42.0 | CORS 代理 | workflow-ui + workflow-operation-api | ✅ 已交付 | 2026-04-19 |
| v41.0 | UI 按钮对齐 | workflow-ui | ✅ 已交付 | 2026-04-19 |

## 📋 待协调 TODO

**当前等待审批通过** - 无待协调实施

查看完整待办 → [../01-team/TODO.md](../01-team/TODO.md)

## 🗂️ 工作日志说明

`work-logs/` 目录将存放每个版本的交付协调日志。

**目前暂无日志** - 交付日志将在后续版本中补充

## 📖 交付日志格式

每个交付日志（`work-logs/delivery-vX.0.md`）应包含：

1. **任务分配**
   - Frontend 做什么
   - Backend 做什么
   - Database 做什么

2. **依赖关系**
   - 谁等谁（例如：Frontend 等 Backend API 先完成）

3. **时间估算**
   - 预计多久完成

4. **风险识别**
   - 可能的阻塞项

5. **实际进度**
   - 每天更新状态

6. **最终交付**
   - 所有 repos 的 commit hash
   - Push 确认

## 🔄 我的工作流程

```
1. 等待 Gate 1（三份文档通过审批）
       ↓
2. 分配任务给开发团队
       ↓
3. 追踪进度（每日 standup）
       ↓
4. 协调阻塞项
       ↓
5. 开发完成后，协调 commit + merge + push
       ↓
6. 移交给 QA 和 E2E Tester
       ↓
7. 等待 Gate 2（UAT 通过）
       ↓
8. 标记 TODO 为 Done
```

## 📦 Repo 管理规则

| Repo | 分支策略 | 责任开发者 |
|------|---------|-----------|
| `workflow-ui` | commit on `main`, push to `origin/main` | Frontend |
| `workflow-operation-api` | commit on `main`, push to `origin/main` | Backend |
| `workflow-online-api` | commit on `develop`, push to `origin/develop` | Backend |

**重要：** 每个 TODO 完成后，**必须**所有涉及的 repos 都 commit + merge + push

## 🔗 相关文档

- 团队协作 → [../01-team/README.md](../01-team/README.md)
- 前端开发 → [../05-Development/01-Frontend/README.md](../05-Development/01-Frontend/README.md)
- 后端开发 → [../05-Development/02-Backend/README.md](../05-Development/02-Backend/README.md)
- 数据库开发 → [../05-Development/03-Database/README.md](../05-Development/03-Database/README.md)
