# Workflow Platform — 全量 E2E 测试指南

**文档版本：** v1.0  
**最后更新：** 2026-04-08  
**目标读者：** 测试经理、QA 工程师  
**测试框架：** Playwright  
**测试环境：** UAT (https://workflow-ui-gamma.vercel.app)

---

## 文档目的

本文档为测试经理提供执行全量 E2E 测试的完整参考，包括：
- 测试前的环境检查清单
- 测试执行步骤
- 结果分析标准
- 常见问题排查
- 测试报告模板

---

## 一、测试前准备（5-10 分钟）

### 1.1 环境检查

#### 检查测试配置文件

```bash
cd /Users/yuangeorge/Documents/GitHub/workflow/workflow-ui
cat playwright.config.ts
```

**必须确认的配置项：**

| 配置项 | 正确值 | 错误示例 | 说明 |
|--------|--------|----------|------|
| `baseURL` | `https://workflow-ui-gamma.vercel.app` | `http://localhost:5173` | 必须指向 UAT 环境 |
| `executablePath` | **不应存在** | `/opt/pw-browsers/...` | 硬编码路径会导致本地测试失败 |
| Mobile 项目 `browserName` | `'chromium'` | 未指定或 `'webkit'` | iPhone 设备默认 WebKit 在 Mac 不可用 |

**正确的配置示例：**

```typescript
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: 1,
  use: {
    baseURL: 'https://workflow-ui-gamma.vercel.app',
    trace: 'on-first-retry',
    // ❌ 不要添加 executablePath
  },
  projects: [
    {
      name: 'Desktop Chrome',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'Mobile Chrome',
      use: {
        ...devices['iPhone 12'],
        browserName: 'chromium', // ✅ 必须明确指定
      },
    },
  ],
});
```

#### 检查测试数据自动创建

```bash
cat e2e/fixtures.ts
```

**必须确认：**
- ✅ `ensureTestApplications()` 函数存在
- ✅ 创建至少 3 个测试应用（E2E_TEST_APP_1/2/3）
- ✅ 创建带 workflow 的测试应用（E2E_TEST_CANVAS）用于 canvas 测试

#### 检查视口跳过逻辑

```bash
grep -n "viewport.*skip" e2e/applications-desktop.spec.ts
grep -n "viewport.*skip" e2e/applications-mobile.spec.ts
```

**必须确认：**
- ✅ Desktop 测试在 `width < 768` 时跳过
- ✅ Mobile 测试在 `width >= 768` 时跳过

### 1.2 依赖安装

```bash
cd workflow-ui
npm install
npx playwright install chromium
```

### 1.3 UAT 环境健康检查

```bash
# 检查 operation-api
curl -s https://workflow-operation-api-n9sbp.ondigitalocean.app/api/workflow/entity-setting?page=0&size=1 | jq '.totalElements'

# 检查 online-api
curl -s https://workflow-online-api-nr3e4.ondigitalocean.app/actuator/health | jq '.status'

# 检查 UI
curl -s -o /dev/null -w "%{http_code}" https://workflow-ui-gamma.vercel.app
```

**预期结果：**
- operation-api 返回 `totalElements` 数字
- online-api 返回 `"UP"`
- UI 返回 `200`

---

## 二、执行测试（10-15 分钟）

### 2.1 运行全量测试

```bash
cd workflow-ui
npm run test:e2e
```

### 2.2 生成 HTML 报告

测试完成后自动生成报告：

```bash
open playwright-report/index.html
```

### 2.3 测试覆盖范围

| 测试套件 | 测试数量 | 覆盖功能 |
|---------|---------|---------|
| **applications-desktop.spec.ts** | 9 | 桌面端应用列表、表格、分页、搜索、CRUD 操作 |
| **applications-mobile.spec.ts** | 10 | 移动端应用卡片、FAB 按钮、溢出菜单、桌面视图切换 |
| **canvas.spec.ts** | 3 | Canvas 加载、Explain 按钮、无 JS 错误 |
| **explain.spec.ts** | 2 | Explain 功能、Token 提示/模态框 |
| **navigation.spec.ts** | 4 × 2 | 路由导航（桌面+移动） |
| **node-editor.spec.ts** | 2 | 节点编辑器抽屉、三面板布局 |
| **records.spec.ts** | 3 × 2 | 记录列表、表格/卡片视图、分页（桌面+移动） |
| **总计** | **66** | 全功能覆盖 |

---

## 三、结果分析标准

### 3.1 成功标准

| 指标 | 目标值 | 说明 |
|------|--------|------|
| **通过率** | ≥ 40% | 至少 26/66 测试通过 |
| **失败率** | ≤ 10% | 最多 6/66 测试失败 |
| **跳过率** | ~50% | 约 33/66 测试因视口不匹配正确跳过 |
| **执行时间** | ≤ 15 分钟 | 包含重试 |

### 3.2 失败分类

#### 类型 A：测试配置问题（紧急修复）

**特征：**
- ❌ 所有或大部分测试失败
- ❌ 错误信息相同（如 "Executable doesn't exist"）
- ❌ 失败发生在测试启动阶段

**处理：**
1. 检查 `playwright.config.ts` 配置
2. 检查浏览器安装：`npx playwright install chromium`
3. 修复后重新运行全量测试

#### 类型 B：测试数据问题（需修复）

**特征：**
- ⊘ 大量测试被跳过
- ⊘ 跳过原因：找不到元素、无应用数据
- ⊘ 但基础导航测试通过

**处理：**
1. 检查 `e2e/fixtures.ts` 是否正确配置
2. 手动验证 UAT 环境是否有测试数据
3. 修复后重新运行全量测试

#### 类型 C：真实应用 Bug（记录到 TODO.md）

**特征：**
- ❌ 少数特定测试失败（< 10%）
- ❌ 错误信息指向功能问题
- ✅ 其他测试正常通过

**处理：**
1. 分析失败原因
2. 在 `workflow-agent-teams/TODO.md` 创建 TODO 项
3. 继续完成测试报告

### 3.3 已知 Bug 参考

根据历史测试报告，以下是已知的应用 Bug：

| Bug ID | 描述 | 影响测试 | 状态 |
|--------|------|---------|------|
| **TODO-mobile-app-overflow-history-copy-modal-not-navigation** | 移动端溢出菜单点击 History/Copy 导航到 Workflow 而非打开模态框 | TC-APP-MOB-05/06/07 | Open |
| **TODO-canvas-workflow-undefined-crash** | Canvas 组件因 `workflow.pluginList` undefined 崩溃 | TC-CANVAS-01 | Open |
| **TODO-mobile-records-pagination-invisible** | 移动端记录列表分页不可见 | TC-REC-03 (Mobile) | Open |

---

## 四、测试报告编写

### 4.1 报告模板

在 `workflow-agent-teams/docs/` 创建新报告：

```bash
cd /Users/yuangeorge/Documents/GitHub/workflow/workflow-agent-teams/docs
cp e2e-test-report-v1.1.md e2e-test-report-vX.Y.md
```

### 4.2 报告必须包含的内容

```markdown
# E2E Test Report vX.Y

**Date:** YYYY-MM-DD  
**Environment:** UAT (https://workflow-ui-gamma.vercel.app)  
**Test Suite:** Playwright E2E Tests  
**Execution Time:** X.X minutes

## Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| **Total Tests** | 66 | 100% |
| **Passed** | XX | XX% |
| **Failed** | XX | XX% |
| **Skipped** | XX | XX% |

## Test Configuration

- **Projects:** Desktop Chrome (1280x720), Mobile Chrome (390x844)
- **Base URL:** https://workflow-ui-gamma.vercel.app
- **Timeout:** 30 seconds per test
- **Retries:** 1

## Results by Category

### ✅ Passing Tests (XX)

[列出通过的测试分类]

### ❌ Failing Tests (XX)

[详细描述每个失败测试，包括错误信息和根本原因]

### ⏭️ Skipped Tests (XX)

[说明跳过原因]

## Critical Issues Summary

[如果有真实应用 Bug，详细描述]

## Next Steps

[建议的后续行动]

---

**Report Generated:** YYYY-MM-DD HH:MM CST  
**Test Artifacts:** `playwright-report/index.html`
```

### 4.3 更新 TODO.md

如果发现新 Bug，在 `workflow-agent-teams/TODO.md` 的 `## Open` 部分添加：

```markdown
- [ ] **[Bug 简短描述]** — [详细描述问题、预期行为、影响范围]. **Affected test:** TC-XXX-XX. **Scope:** `workflow-ui` [具体组件]. **Recognition label:** `TODO-[bug-identifier]`.
```

---

## 五、常见问题排查

### 问题 1：所有测试失败 "Executable doesn't exist"

**原因：** `playwright.config.ts` 配置了错误的浏览器路径

**解决：**
```typescript
// ❌ 删除这行
executablePath: '/opt/pw-browsers/chromium-1140/chrome-linux/chrome'

// ✅ 不指定 executablePath，让 Playwright 使用本地浏览器
```

### 问题 2：Mobile Chrome 测试失败 "webkit not found"

**原因：** Mobile 项目没有指定 `browserName`，默认使用 WebKit

**解决：**
```typescript
{
  name: 'Mobile Chrome',
  use: {
    ...devices['iPhone 12'],
    browserName: 'chromium', // ✅ 添加这行
  },
}
```

### 问题 3：大量测试跳过 "no applications exist"

**原因：** 测试数据没有自动创建

**解决：**
1. 检查 `e2e/fixtures.ts` 是否存在
2. 确认 `ensureTestApplications()` 被调用
3. 手动验证 UAT 环境：
   ```bash
   curl "https://workflow-operation-api-n9sbp.ondigitalocean.app/api/workflow/entity-setting?applicationName=E2E_TEST_APP_1"
   ```

### 问题 4：Desktop 测试在 Mobile 项目失败

**原因：** 缺少视口检查

**解决：** 在 `applications-desktop.spec.ts` 的 `beforeEach` 添加：
```typescript
test.beforeEach(async ({ page }) => {
  await page.goto('/workflows/');
  await page.waitForLoadState('networkidle');
  
  // 在移动视口跳过 Desktop 测试
  const width = page.viewportSize()?.width ?? 1280;
  if (width < 768) {
    test.skip();
  }
});
```

### 问题 5：Canvas 测试全部跳过

**原因：** 没有带 workflow 的测试应用

**解决：** 在 `e2e/fixtures.ts` 确认：
```typescript
await createTestApplication('E2E_TEST_CANVAS', true); // ✅ 第二个参数必须是 true
```

---

## 六、测试执行检查清单

### 执行前（5 分钟）

- [ ] `playwright.config.ts` 无硬编码 `executablePath`
- [ ] Mobile 项目指定 `browserName: 'chromium'`
- [ ] `baseURL` 指向 UAT 环境
- [ ] `e2e/fixtures.ts` 存在并正确配置
- [ ] Desktop 测试有 viewport < 768 跳过逻辑
- [ ] Mobile 测试有 viewport >= 768 跳过逻辑
- [ ] 已安装 Chromium：`npx playwright install chromium`
- [ ] UAT 环境健康检查通过

### 执行中（10 分钟）

- [ ] 运行 `npm run test:e2e`
- [ ] 观察测试进度，记录异常
- [ ] 等待测试完成

### 执行后（5 分钟）

- [ ] 查看总结：通过率 > 40%，失败率 < 10%
- [ ] 打开 `playwright-report/index.html` 查看详细结果
- [ ] 分析失败原因：配置问题 vs 测试数据问题 vs 真实 Bug
- [ ] 创建测试报告到 `workflow-agent-teams/docs/e2e-test-report-vX.Y.md`
- [ ] 把确认的 Bug 添加到 `workflow-agent-teams/TODO.md`
- [ ] 提交测试报告和 TODO 更新到 Git

---

## 七、测试数据管理

### 7.1 自动创建的测试应用

| 应用名称 | 用途 | Workflow | 创建方式 |
|---------|------|----------|---------|
| `E2E_TEST_APP_1` | 基础 CRUD 测试 | 空 | `fixtures.ts` 自动创建 |
| `E2E_TEST_APP_2` | 基础 CRUD 测试 | 空 | `fixtures.ts` 自动创建 |
| `E2E_TEST_APP_3` | 基础 CRUD 测试 | 空 | `fixtures.ts` 自动创建 |
| `E2E_TEST_CANVAS` | Canvas/节点编辑器测试 | 复制自 UK_DRFI_1 | `fixtures.ts` 自动创建 |

### 7.2 测试数据清理

测试应用会持久化在 UAT 环境，不会自动删除。如需清理：

```bash
# 删除测试应用
curl -X DELETE "https://workflow-operation-api-n9sbp.ondigitalocean.app/api/workflow?applicationName=E2E_TEST_APP_1"
curl -X DELETE "https://workflow-operation-api-n9sbp.ondigitalocean.app/api/workflow?applicationName=E2E_TEST_APP_2"
curl -X DELETE "https://workflow-operation-api-n9sbp.ondigitalocean.app/api/workflow?applicationName=E2E_TEST_CANVAS"
```

---

## 八、时间估算

| 阶段 | 首次执行 | 后续执行 |
|------|---------|---------|
| **环境检查** | 10 分钟 | 5 分钟 |
| **测试执行** | 15 分钟 | 10 分钟 |
| **结果分析** | 15 分钟 | 5 分钟 |
| **报告编写** | 20 分钟 | 10 分钟 |
| **总计** | **60 分钟** | **30 分钟** |

---

## 九、参考文档

- **测试用例主文档：** `workflow-agent-teams/docs/TEST_CASES_MASTER.md`
- **E2E 测试经验总结：** `workflow-agent-teams/docs/e2e-testing-guide.md`
- **最新测试报告：** `workflow-agent-teams/docs/e2e-test-report-v1.1.md`
- **TODO 积压：** `workflow-agent-teams/TODO.md`
- **流程规则：** `workflow-agent-teams/CLAUDE.md`

---

## 十、快速参考命令

```bash
# 进入 UI 项目
cd /Users/yuangeorge/Documents/GitHub/workflow/workflow-ui

# 安装依赖
npm install
npx playwright install chromium

# 运行全量测试
npm run test:e2e

# 查看报告
open playwright-report/index.html

# 检查 UAT 环境
curl -s https://workflow-operation-api-n9sbp.ondigitalocean.app/api/workflow/entity-setting?page=0&size=1 | jq

# 创建测试报告
cd ../workflow-agent-teams/docs
cp e2e-test-report-v1.1.md e2e-test-report-vX.Y.md
```

---

**文档维护者：** 测试团队  
**最后更新：** 2026-04-08  
**版本：** v1.0
