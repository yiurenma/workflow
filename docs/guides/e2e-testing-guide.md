# E2E全量测试经验总结

**日期：** 2026-04-07  
**测试人员：** Claude (AI Agent)  
**目标读者：** 测试经理及未来执行全量测试的团队成员

## 执行概要

本次全量E2E测试从失败到成功，经历了多次迭代和问题修复。最终结果：
- **通过：** 28个测试（42%）
- **失败：** 5个测试（8%，均为真实应用bug）
- **跳过：** 33个测试（50%，正确跳过）

## 关键经验教训

### 1. 测试配置问题必须先解决

**问题：** 初次运行时所有66个测试全部失败，因为playwright.config.ts配置了云环境的浏览器路径。

**教训：**
- ✅ **本地测试不要硬编码云环境路径** - 移除 `executablePath: '/opt/pw-browsers/...'`
- ✅ **让Playwright使用本地安装的浏览器** - 不指定executablePath
- ✅ **Mobile项目必须明确指定browserName** - `browserName: 'chromium'` 覆盖设备默认的WebKit

**修复代码：**
```typescript
// playwright.config.ts
export default defineConfig({
  use: {
    baseURL: 'https://workflow-ui-gamma.vercel.app',
    trace: 'on-first-retry',
    // 不要指定 executablePath
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
        browserName: 'chromium', // 明确指定使用Chromium
      },
    },
  ],
});
```

### 2. 测试数据必须自动创建

**问题：** 很多测试因为UAT环境没有测试数据而跳过。

**教训：**
- ✅ **不要依赖手动创建测试数据** - 测试应该自给自足
- ✅ **使用fixtures自动创建测试数据** - 在测试前调用API创建
- ✅ **复用生产数据作为模板** - 拷贝UK_DRFI_1的workflow结构

**实现方案：**
```typescript
// e2e/fixtures.ts
import { test as base } from '@playwright/test';

const OPERATION_API = 'https://workflow-operation-api-n9sbp.ondigitalocean.app/api';

export const test = base.extend({
  page: async ({ page }, use) => {
    // 测试前自动创建测试数据
    await ensureTestApplications();
    await use(page);
  },
});

async function ensureTestApplications(): Promise<void> {
  // 创建空workflow的应用（用于基础测试）
  await createTestApplication('E2E_TEST_APP_1', false);
  
  // 创建带真实workflow的应用（用于canvas测试）
  await createTestApplication('E2E_TEST_CANVAS', true);
}
```

### 3. 视口检查避免误报

**问题：** Desktop测试在Mobile项目运行导致33个误报失败。

**教训：**
- ✅ **Desktop测试应该在移动视口跳过** - 添加viewport宽度检查
- ✅ **Mobile测试应该在桌面视口跳过** - 避免在错误环境运行

**实现方案：**
```typescript
// Desktop测试套件
test.beforeEach(async ({ page }) => {
  await page.goto('/workflows/');
  await page.waitForLoadState('networkidle');
  
  // 在移动视口跳过Desktop测试
  const width = page.viewportSize()?.width ?? 1280;
  if (width < 768) {
    test.skip();
  }
});

// Mobile测试套件
test.beforeEach(async ({ page }) => {
  await page.goto('/workflows/');
  await page.waitForLoadState('networkidle');
  
  // 在桌面视口跳过Mobile测试
  const width = page.viewportSize()?.width ?? 1280;
  if (width >= 768) {
    test.skip();
  }
});
```

### 4. 区分测试问题和应用bug

**关键原则：** 测试失败不一定是应用bug，可能是测试配置问题。

**判断标准：**
- ❌ **测试配置问题：** 所有测试都失败，错误信息相同（如浏览器不存在）
- ❌ **测试数据问题：** 测试被跳过，因为找不到元素
- ✅ **真实应用bug：** 少数特定测试失败，错误信息指向功能问题

**本次识别的真实bug：**
1. 移动端溢出菜单导航错误（3个测试失败）
2. Canvas workflow数据undefined（1个测试失败）
3. 移动端分页不可见（1个测试失败）

## 快速全量测试检查清单

### 执行前检查（5分钟）

- [ ] **检查playwright.config.ts**
  - [ ] 没有硬编码的executablePath
  - [ ] Mobile项目指定了browserName: 'chromium'
  - [ ] baseURL指向正确的UAT环境

- [ ] **检查测试数据设置**
  - [ ] fixtures.ts存在并正确配置
  - [ ] 测试应用会自动创建
  - [ ] 有带workflow的测试应用（用于canvas测试）

- [ ] **检查视口逻辑**
  - [ ] Desktop测试有viewport < 768的跳过逻辑
  - [ ] Mobile测试有viewport >= 768的跳过逻辑

### 执行测试（10分钟）

```bash
cd workflow-ui
npm run test:e2e
```

### 结果分析（5分钟）

1. **查看总结**
   - 通过率应该 > 40%
   - 失败率应该 < 10%
   - 跳过率约 50%（视口不匹配）

2. **分析失败**
   - 如果所有测试失败 → 检查配置
   - 如果大量测试跳过 → 检查测试数据
   - 如果少数测试失败 → 真实bug，添加到TODO.md

3. **生成报告**
   - 打开 `playwright-report/index.html` 查看详细结果
   - 创建测试报告到 `workflow-agent-teams/docs/`
   - 把确认的bug添加到 `workflow-agent-teams/TODO.md`

## 常见问题排查

### Q: 所有测试失败，错误"Executable doesn't exist"
**A:** playwright.config.ts配置了错误的浏览器路径。移除executablePath配置。

### Q: Mobile Chrome测试失败，错误"webkit not found"
**A:** Mobile项目没有指定browserName。添加 `browserName: 'chromium'`。

### Q: 很多测试跳过，原因"no applications exist"
**A:** 测试数据没有自动创建。检查fixtures.ts是否正确配置。

### Q: Desktop测试在Mobile项目失败
**A:** 缺少viewport检查。在beforeEach中添加viewport宽度检查。

### Q: Canvas测试跳过
**A:** 没有带workflow的测试应用。创建E2E_TEST_CANVAS并复制UK_DRFI_1的workflow。

## 文件清单

执行全量测试需要以下文件正确配置：

```
workflow-ui/
├── playwright.config.ts          # 测试配置（无硬编码路径）
├── e2e/
│   ├── fixtures.ts               # 自动创建测试数据
│   ├── applications-desktop.spec.ts  # Desktop测试（有viewport检查）
│   ├── applications-mobile.spec.ts   # Mobile测试（有viewport检查）
│   ├── canvas.spec.ts
│   ├── navigation.spec.ts
│   ├── records.spec.ts
│   └── ...
└── package.json                  # 包含 test:e2e 脚本
```

## 时间估算

- **首次配置：** 30分钟（修复配置、添加fixtures、添加viewport检查）
- **后续执行：** 20分钟（5分钟检查 + 10分钟运行 + 5分钟分析）

## 成功标准

全量测试成功的标志：
- ✅ 通过率 > 40%
- ✅ 失败的都是真实应用bug（不是测试配置问题）
- ✅ 跳过的都是正确跳过（视口不匹配或缺少特定数据）
- ✅ 测试报告已生成并提交到workflow-agent-teams/docs/
- ✅ 确认的bug已添加到workflow-agent-teams/TODO.md

---

**最后更新：** 2026-04-07  
**维护者：** 测试团队  
**相关文档：** 
- `workflow-agent-teams/docs/e2e-test-report-v1.1.md`
- `workflow-agent-teams/TODO.md`
