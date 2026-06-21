# QA 工作索引

## 我是谁？
QA（Quality Assurance），负责**手动测试**，验证功能在真实环境的表现。

## 📊 当前状态

**最新测试报告：**
| 版本 | 状态 | 通过率 | 测试设备 | 日期 |
|------|------|--------|----------|------|
| [v43.0](test-reports/ui-test-report-v43.0.md) | ✅ PASS | 6/6 | Desktop + Mobile | 2026-04-19 |
| [v42.0](test-reports/ui-test-report-v42.0.md) | ✅ PASS | 4/4 | Desktop + Mobile | 2026-04-19 |
| [v41.0](test-reports/ui-test-report-v41.0.md) | ✅ PASS | - | Desktop + Mobile | 2026-04-19 |

## 📋 待测试 TODO

**当前无待测试** - 最新开发完成后会分配到 QA

查看完整待办 → [../../01-team/TODO.md](../../01-team/TODO.md)

## 🗂️ 全部测试报告

`test-reports/` 目录包含历史测试报告。

**查找技巧：**
- 按版本号查找：`test-reports/ui-test-report-v[X].0.md`
- 查看最新 5 个报告了解近期质量趋势

## 📖 我的测试报告格式

每个测试报告（`test-reports/ui-test-report-vX.0.md`）包含：

1. **Test Execution Summary** - 测试执行摘要
   - Total Test Cases
   - Passed / Failed
   - Pass Rate
2. **Test Results** - 逐个测试用例结果
   - Status（✅ PASS / ❌ FAIL）
   - Steps Executed
   - Actual Result
   - Evidence（截图链接）
3. **Known Issues** - 发现的问题（如有）
4. **Environment** - 测试环境信息

## 🖥️ 我的测试设备

- **Desktop**: Chrome 1280px × 1024px
- **Mobile**: Chrome 390px × 844px (Pixel 5)

## 🔄 我的工作流程

```
1. 从 Test Manager 获取测试用例（test-doc-vX.0.md）
2. 在 UAT 环境手动执行测试
   - Desktop: https://workflow-ui-gamma.vercel.app
   - Mobile: 同上
3. 记录每个测试用例的执行结果
4. 截图保存证据（如需要）
5. 编写测试报告（ui-test-report-vX.0.md）
6. 提交给 Test Manager 审核
```

## 🔗 相关文档

- 测试用例 → [../01-Test-Manager/work-logs/](../01-Test-Manager/work-logs/)
- E2E 测试报告 → [../03-E2E/README.md](../03-E2E/README.md)
