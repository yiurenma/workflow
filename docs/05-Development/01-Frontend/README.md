# Frontend 开发索引

## 我是谁？
前端开发者（Frontend Developer），负责实现 `workflow-ui` 的功能。

## 📊 当前状态

**最新实现：**
| 版本 | 标题 | 改动范围 | 状态 | 日期 |
|------|------|----------|------|------|
| v43.0 | 移除导入边引用校验 | `ImportWorkflowModal.tsx` | ✅ 已完成 | 2026-04-19 |
| v42.0 | CORS 代理前端逻辑 | Deploy 组件 | ✅ 已完成 | 2026-04-19 |
| v41.0 | 输入框高度调整 | 全局样式 | ✅ 已完成 | 2026-04-19 |

## 📋 待开发 TODO

**当前活跃前端任务：**
- 🔴 [E2E 测试选择器更新](../../01-team/TODO.md#TODO-e2e-carbon-rewrite-selector-updates) - HIGH

查看完整待办 → [../../01-team/TODO.md](../../01-team/TODO.md)

## 🗂️ 工作日志说明

`work-logs/` 目录将存放每个版本的前端实现日志。

**目前暂无日志** - 开发日志将在后续版本中补充

## 📖 前端实现日志格式

每个前端实现日志（`work-logs/frontend-vX.0.md`）应包含：

1. **实现范围** - 这个版本改了哪些文件
2. **技术决策** - 为什么选择这个实现方式
3. **关键代码片段** - 核心逻辑的代码示例
4. **测试验证** - 本地如何验证功能正常
5. **已知问题** - 发现的 bug 或待优化项

## 💻 技术栈

- **框架：** React 18 + TypeScript
- **构建工具：** Vite
- **状态管理：** TanStack Query
- **画布：** React Flow
- **UI 组件库：** IBM Carbon Design System
- **样式：** CSS Modules

## 🔗 相关文档

- 产品需求 → [../../02-PM/README.md](../../02-PM/README.md)
- 架构设计 → [../../03-Architect/README.md](../../03-Architect/README.md)
- 后端实现 → [../02-Backend/README.md](../02-Backend/README.md)
- QA 测试 → [../../06-Testing/02-QA/README.md](../../06-Testing/02-QA/README.md)

## 📦 代码仓库

- **Repo:** `workflow-ui`
- **分支策略：** commit on `main`, push to `origin/main`
