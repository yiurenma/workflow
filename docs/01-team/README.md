# 团队协作文档

## 🤖 这是什么？
记录团队工作流程、角色职责、待办清单和角色分配。

## 📋 文档列表

| 文档 | 作用 |
|------|------|
| [TODO.md](TODO.md) | 统一待办清单（所有 TODO 项） |
| [assignments.md](assignments.md) | 角色分配（谁负责什么） |
| [agent-system.md](agent-system.md) | 自动化角色体系说明 |

## 🔄 标准工作流程

```
1. 从 TODO.md 领取任务
       ↓
2. PM 编写需求文档（pm-doc-vX.0.md + 更新 baseline.md）
       ↓
3. Architect 编写技术方案（arch-doc-vX.0.md）
       ↓
4. Test Manager 编写测试用例（test-doc-vX.0.md）
       ↓
5. 🛑 人类审批（Gate 1）
       ↓
6. Delivery Manager 协调实施
       ↓
7. Frontend/Backend/Database 并行开发
       ↓
8. QA 手动测试 → E2E 自动化测试 → UAT 验收
       ↓
9. 🛑 人类确认（Gate 2）
       ↓
10. 标记 TODO 为 Done
```

## 👥 团队角色

| 角色 | 职责 | 工作区 |
|------|------|--------|
| **PM** | 定义用户故事 + 验收标准 | [../02-PM/](../02-PM/) |
| **Architect** | 设计技术方案 + API 契约 | [../03-Architect/](../03-Architect/) |
| **Delivery Manager** | 协调实施 + 进度跟踪 | [../04-Delivery-Manager/](../04-Delivery-Manager/) |
| **Frontend Dev** | 实现前端（workflow-ui） | [../05-Development/01-Frontend/](../05-Development/01-Frontend/) |
| **Backend Dev** | 实现后端（operation-api + online-api） | [../05-Development/02-Backend/](../05-Development/02-Backend/) |
| **Database Dev** | Schema 变更 + 迁移脚本 | [../05-Development/03-Database/](../05-Development/03-Database/) |
| **Test Manager** | 测试用例设计 | [../06-Testing/01-Test-Manager/](../06-Testing/01-Test-Manager/) |
| **QA** | 手动测试 | [../06-Testing/02-QA/](../06-Testing/02-QA/) |
| **E2E Tester** | 自动化测试 + UAT | [../06-Testing/03-E2E/](../06-Testing/03-E2E/) |

## 🚪 质量门（Quality Gates）

### Gate 1 - 文档审批
**条件：** PM、Architect、Test Manager 三份文档都完成
**审批人：** 人类
**通过后：** Delivery Manager 开始协调实施

### Gate 2 - UAT 验收
**条件：** E2E Tester 在 UAT 环境跑完所有测试并报告 PASS
**审批人：** 人类
**通过后：** 标记 TODO 为 Done
