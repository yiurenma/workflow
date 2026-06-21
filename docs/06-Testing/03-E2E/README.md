# E2E & UAT 测试索引

## 我是谁？
E2E Tester，使用 **Playwright** 执行自动化测试和 UAT 验收。

## 📊 当前测试状态

### E2E 测试（开发环境）
| 版本 | 状态 | 通过率 | 主要问题 | 日期 |
|------|------|--------|----------|------|
| [v44.0](e2e-reports/e2e-test-report-v44.0.md) | ⚠️ FAIL | 9/90 | Carbon 重写后选择器失效 | 2026-04-19 |
| [v43.0](e2e-reports/e2e-test-report-v43.0.md) | ✅ PASS | 6/6 | - | 2026-04-19 |

### UAT 验收（真实环境）
| 版本 | 状态 | 通过率 | 主要问题 | 日期 |
|------|------|--------|----------|------|
| [v45.0](uat-reports/uat-report-v45.0.md) | ✅ PASS | 29/30 | 1 个测试实现缺陷（非产品缺陷） | 2026-06-21 |
| [v43.0](uat-reports/uat-report-v43.0.md) | ✅ PASS | 6/6 | - | 2026-04-19 |

## 🚨 待修复问题

**v44.0 E2E 测试 81 个失败：**
- ❌ Canvas import (15 tests)
- ❌ Node editor (8 tests)
- ❌ JsonPath (4 tests)
- ❌ Applications list (7 tests)
- ❌ Modal/Drawer 交互 (6 tests)

**根本原因：** IBM Carbon 重写后，测试仍在查找 Ant Design 类名（`.ant-*`）

**修复计划：** 更新所有选择器为 Carbon 类名（`.cds-*`）

详见 → [e2e-reports/e2e-test-report-v44.0.md](e2e-reports/e2e-test-report-v44.0.md)

## 📍 报告导航

| 类型 | 最新版本 | 状态 |
|------|---------|------|
| E2E 报告 | [v44.0](e2e-reports/e2e-test-report-v44.0.md) | ⚠️ 81 failures |
| UAT 报告 | [v45.0](uat-reports/uat-report-v45.0.md) | ✅ PASS |
| Bug 截图 | [screenshots/v45/](screenshots/v45/) | 14 张截图 |

## 🧪 测试环境

**E2E 测试：** 开发环境（可能是 mock）
**UAT 验收：**
- Frontend: https://workflow-ui-gamma.vercel.app
- operation-api: https://workflow-operation-api-n9sbp.ondigitalocean.app
- online-api: https://workflow-online-api-nr3e4.ondigitalocean.app

## 📖 测试方法论

**5 层 UX 验证框架：** [guides/5-layer-validation.md](guides/5-layer-validation.md)

1. **Layer 1 (Exist)** - 元素是否存在于 DOM
2. **Layer 2 (Size)** - 尺寸是否符合规范
3. **Layer 3 (Viewport)** - 是否在视口内（未被裁剪）
4. **Layer 4 (Interact)** - 交互是否正常（点击、输入）
5. **Layer 5 (Effect)** - 效果是否符合预期（样式、API 响应）

## 🗂️ 全部报告

- **E2E 报告：** `e2e-reports/` (v1.0 → v44.0)
- **UAT 报告：** `uat-reports/` (v4.0 → v45.0)
- **Bug 截图：** `screenshots/` (按版本分组)
