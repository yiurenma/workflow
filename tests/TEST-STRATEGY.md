# 测试策略 — 全量回归 + 可用性（业界规范驱动）

目标：**任何改动不破坏已有功能**，且**主动验证用户操作的便捷性/合理性**。全部在根仓库 `tests/`、**全黑盒**、不动任何 submodule。

## 遵循的业界规范

| 维度 | 规范 | 落地 | 门禁 |
|---|---|---|---|
| 质量总纲 | **ISO/IEC 25010** | 覆盖矩阵按其 Usability 6 子特性组织 | 框架 |
| 无障碍 | **WCAG 2.2 AA** + WAI-ARIA | `@axe-core/playwright`（`lib/a11y.ts`） | **硬** |
| 性能体验 | **Core Web Vitals**（LCP/INP/CLS） | `web-vitals` 注入（`lib/webvitals.ts`） | **硬** |
| API 契约 | **OpenAPI** + RFC 9110 | `/v3/api-docs` + ajv（`lib/contract.ts`） | **硬** |
| 可用性 | **Nielsen 10** + ISO 9241-110 | 断言库 `lib/ux.ts` + 启发式清单 | 部分硬/软 |
| 测试设计 | **ISTQB**（等价类/边界/判定表/状态转换） | 系统化导出用例 | 方法 |
| 行为规范 | **BDD / Given-When-Then** | 测试标题用同一语言 | 方法 |
| 视觉回归 | Playwright `toHaveScreenshot` | `visual/` 基线 | 软 |

## 门禁分级（关键）

- **硬门禁 `@gate`**：功能正确性、OpenAPI 契约、WCAG AA（基线外）、Core Web Vitals。**CI 用 `--grep @gate` 阻断合并。**
- **软提示 `@advisory`**：视觉回归 diff、WAI-ARIA 理想项（如 Esc 关闭）、Nielsen 主观项、AI UX 审计。出报告/转 TODO，**不阻断**。
- **基线豁免（baseline allowlist）**：存量已知违规（如某页 color-contrast）按规则**豁免并挂 TODO**，门禁仍抓**新**回归；修复后移除豁免。这是处理"遗留 a11y 债"的业界标准做法。

## 分层（黑盒约束下偏集成/E2E）

| 层 | 在哪 | 说明 |
|---|---|---|
| 单元 | **各 service 子模块**（JaCoCo ~98%） | 不在本套件范围（不动 submodule） |
| API / 契约 | `tests/api/` `tests/contract/` | 端点、错误码、幂等、OpenAPI 一致性 |
| E2E 功能 | `tests/e2e/` | 关键用户流程（桌面+移动，5 层 UX） |
| 无障碍 | `tests/a11y/` | 每界面 axe WCAG AA |
| 可用性 | `tests/ux/` | ISO25010 子特性断言 |
| 性能 | `tests/perf/` | Core Web Vitals |
| 视觉 | `tests/visual/` | 截图基线 |

## 选择器原则
仅用**可访问语义**（role / label / text）定位，不依赖内部 class/testid。这既满足"不动 submodule"，又**倒逼无障碍**——app 越无障碍，越可测。

## 确定性
- 默认对**本地栈**跑（operation 8080 / online 8081 / UI mock 5173），env 可切 UAT。
- 数据用 `lib/seed.ts` 对本地 H2 播种；外部 HTTP 依赖的执行型用例需可 stub 的环境（见 `../docs/local-verification-report.md`）。

## 如何跑

```bash
cd tests && npm install
export OPERATION_API_BASE=http://localhost:8080 ONLINE_API_BASE=http://localhost:8081 \
       UI_BASE=http://localhost:5173 PLAYWRIGHT_BROWSERS_PATH=/opt/pw-browsers

npx playwright test                              # 全部
npx playwright test --grep @gate                 # 仅硬门禁（CI 阻断用）
npx playwright test --project=contract           # 契约
npx playwright test --project=a11y               # 无障碍
npx playwright test --project=ux --project=perf  # 可用性 + 性能
npx playwright test --project=visual             # 视觉（首跑写基线）
```

## 未覆盖 / 后续（诚实声明）
- 单元层在 submodule，未触碰；本套件是集成/契约/E2E/UX/a11y/性能层。
- 纯主观"措辞/流程是否别扭"由**周期性 AI UX 审计 + 人工启发式清单**兜底（非自动门禁）。
- **CI 接线**（真正阻断合并）需配置各 repo 的 CI（动 submodule）——本次只产出套件 + `@gate` 约定；接线见 `../docs/TODO-doc-gaps.md`。
- 238 全量逐条填充是迭代工作，进度见 `coverage-matrix.md`。
