# 测试体系总览

## 这是什么？
三层测试体系：**Test Manager**（设计用例）→ **QA**（手动测试）→ **E2E Tester**（自动化 + UAT）

## 📊 当前测试状态

| 测试类型 | 当前版本 | 状态 | 通过率 | 最后更新 |
|---------|---------|------|--------|----------|
| [测试用例](01-Test-Manager/README.md) | v43.0 | ✅ 已编写 | - | 2026-04-19 |
| [QA 手测](02-QA/README.md) | v43.0 | ✅ PASS | 6/6 | 2026-04-19 |
| [E2E 测试](03-E2E/README.md) | v44.0 | ⚠️ FAIL | 9/90 | 2026-04-19 |
| [UAT 验收](03-E2E/README.md) | v45.0 | ✅ PASS | 29/30 | 2026-06-21 |

## 🚨 当前测试问题

**E2E 测试 81 个失败** - Carbon 重写后选择器需更新
- **优先级：** 🔴 HIGH
- **TODO 标签：** `TODO-e2e-carbon-rewrite-selector-updates`
- **详细报告：** [03-E2E/e2e-reports/e2e-test-report-v44.0.md](03-E2E/e2e-reports/e2e-test-report-v44.0.md)
- **影响范围：** Canvas import、Node editor、Modal、Drawer

## 📍 快速导航

 去哪里 |
|---------|--------|
| 📋 看测试用例 | [01-Test-Manager/test-cases.md](01-Test-Manager/test-cases.md) (238 项) |
| 👁️ 看手测报告 | [02-QA/test-reports/ui-test-report-v43.0.md](02-QA/test-reports/ui-test-report-v43.0.md) |
| 🤖 看 E2E 报告 | [03-E2E/e2e-reports/e2e-test-report-v44.0.md](03-E2E/e2e-reports/e2e-test-report-v44.0.md) |
| ✅ 看 UAT 报告 | [03-E2E/uat-reports/uat-report-v45.0.md](03-E2E/uat-reports/uat-report-v45.0.md) |
| 📸 看 Bug 截图 | [03-E2E/screenshots/v45/](03-E2E/screenshots/v45/) |

## 🔄 测试流程

```
Test Manager 编写测试用例（test-doc-vX.0.md）
       ↓
QA 手动测试（Desktop 1280px + Mobile 390×844）
  → 写 ui-test-report-vX.0.md
       ↓
E2E Tester 编写 Playwright 测试
  → 在开发环境跑 E2E
  → 写 e2e-test-report-vX.0.md
       ↓
E2E Tester 在 UAT 环境跑验收
  → 真实环境：workflow-ui-gamma.vercel.app
  → 5 层 UX 验证（Exist/Size/Viewport/Interact/Effect）
  → 写 uat-report-vX.0.md
       ↓
Test Manager 审核结果
  → PASS → 进入交付
  → FAIL → 开新 TODO，重新走流程
```

## 📂 子目录说明

- `01-Test-Manager/` - 测试用例设计与管理（TC-* 编号）
- `02-QA/` - 手动测试报告（Desktop + Mobile）
- `03-E2E/` - Playwright 自动化测试 + UAT 验收
