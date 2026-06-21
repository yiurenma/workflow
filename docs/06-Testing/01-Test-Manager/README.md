# Test Manager 工作索引

## 我是谁？
测试经理（Test Manager），负责编写**测试用例**，协调 QA 和 E2E Tester。

## 📊 当前状态

**测试用例注册表：** [test-cases.md](test-cases.md) - **238 项**（TC-* 编号）

**最新工作：**
| 版本 | 标题 | TODO 标签 | 状态 | 日期 |
|------|------|-----------|------|------|
| [v43.0](work-logs/test-doc-v43.0.md) | 导入校验放宽测试 | `TODO-import-validation-remove-edge-reference-check` | ✅ Done | 2026-04-19 |
| [v42.0](work-logs/test-doc-v42.0.md) | CORS 代理测试 | `TODO-deploy-step1-cors-proxy-for-target-base-url` | ✅ Done | 2026-04-19 |
| [v41.0](work-logs/test-doc-v41.0.md) | UI 按钮高度测试 | `TODO-ui-form-control-heights-and-button-label-contrast` | ✅ Done | 2026-04-19 |

## 📋 待处理 TODO（等待测试用例）

**当前无待处理** - 最新 TODO 已分配给其他角色

查看完整待办 → [../../01-team/TODO.md](../../01-team/TODO.md)

## 🗂️ 全部历史版本

`work-logs/` 目录包含 v1.0 → v43.0 共 **47 个**版本的测试用例设计。

## 📖 我的交付物

每个测试用例日志（`work-logs/test-doc-vX.0.md`）包含：

1. **Test Scope** - 测试范围
2. **Test Cases** - 测试用例列表（TC-* 编号）
   - **Objective** - 测试目标
   - **Preconditions** - 前置条件
   - **Test Data** - 测试数据
   - **Steps** - 测试步骤
   - **Expected Result** - 期望结果
3. **Test Levels** - 测试层级（E2E、Integration、Unit）

## 🔄 我的工作流程

```
1. 阅读 PM 文档（pm-doc-vX.0.md）
2. 阅读 Arch 文档（arch-doc-vX.0.md）
3. 编写测试用例（test-doc-vX.0.md）
4. 协调 QA 执行手动测试
5. 协调 E2E Tester 编写自动化测试
6. 审核 UAT 报告
   - PASS → 进入交付
   - FAIL → 开新 TODO
```
