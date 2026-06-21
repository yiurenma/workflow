# 覆盖矩阵 — AC × ISO/IEC 25010 × 测试

真值源：`../docs/baseline/pm-doc-master.md`（US/AC）+ `../docs/baseline/TEST_CASES_MASTER.md`（238 TC）。
状态：✅ 已覆盖（本套件实跑）· 🟡 部分 · ⬜ 待补 · 🔵 由 submodule 单元覆盖（根仓库范围外）。

> 这是**活文档**：每新增 AC 必须在此登记测试（与 CLAUDE.md「新 AC 必带测试」规则一致）。缺口转 `../docs/TODO-doc-gaps.md`。

## 功能正确性（ISO25010 Functional suitability）

| AC / 能力 | 测试 | 状态 |
|---|---|---|
| APP-AC-01 列表/搜索/分页 | `api/app.spec.ts`、`e2e/applications.spec.ts` | ✅ |
| APP-AC-02/03 创建/删除 + 确认 | `api/app.spec.ts`(round-trip)、`e2e/` | ✅ |
| APP-AC-10 autoCopy（同源/源不存在） | `api/app.spec.ts` | ✅ |
| APP-AC-11 history | `api/app.spec.ts` | ✅ |
| APP-AC-18 entity-setting PATCH | `api/app.spec.ts` | ✅ |
| APP-AC-52 Deploy 代理端点存在 | `api/app.spec.ts` | 🟡（端点存在；全链路需密钥） |
| REC-AC-12 调用契约/未知应用/缺头 | `api/online.spec.ts`、`contract/online-api` | ✅ |
| REC-AC-15 幂等（M0002） | `api/online.spec.ts` | 🟡（需 keystore 密钥实跑） |
| REC-AC-13/14/16 丰富化/下发/重试 | — | 🔵（online-api 单元 + 需可 stub 环境） |
| CV-AC-04/05/53 画布/导入 | `e2e/canvas.spec.ts` | ✅ |
| CV-AC-40/50 规则 JSONPath 校验 | — | ⬜（待补 ux/e2e） |
| 错误码 WF-* / M00xx | `contract/*`、`api/*` | ✅（契约层）|

## OpenAPI 契约（ISO25010 Compatibility / Functional）

| 项 | 测试 | 状态 |
|---|---|---|
| operation-api 端点声明 + 状态码 | `contract/operation-api.contract.spec.ts` | ✅ @gate |
| online-api 端点声明 + 状态码 | `contract/online-api.contract.spec.ts` | ✅ @gate |
| 响应体 schema 校验（ajv） | `lib/contract.ts`（能力就绪） | 🟡（按需逐端点接入） |

## 可用性 ISO25010 子特性（"合理性全要"）

| 子特性 | 断言 | 测试 | 状态 |
|---|---|---|---|
| Operability 易操作 | 主操作首屏可见 / 触控目标 / N 步可达 | `ux/applications.ux.spec.ts` | ✅ @gate |
| User error protection 防错 | 非法时提交禁用 / 危险操作确认 | `lib/ux.ts`（就绪） | ⬜ 待接入样例 |
| User control & freedom 退路 | 弹窗显式关闭控件 | `ux/*` | ✅ @gate |
| ↳ WAI-ARIA Esc 关闭 | `assertEscapeCloses` | `ux/*`(@advisory fixme) | 🟡 已知差距→TODO |
| Visibility of status 反馈 | 操作 X ms 内有反馈 | `lib/ux.ts`（就绪） | ⬜ 待接入样例 |
| Appropriateness recognizability | 空状态有引导 | `lib/ux.ts`（就绪） | ⬜ 待接入样例 |
| UI aesthetics 一致 | Carbon token 计算样式 + 视觉基线 | `e2e/`(token)、`visual/` | ✅/🟡 |
| Accessibility 无障碍 | axe WCAG 2.2 AA | `a11y/*` | ✅ @gate（含基线豁免） |

## 性能体验（ISO25010 Performance efficiency）

| 项 | 测试 | 状态 |
|---|---|---|
| Core Web Vitals（LCP/CLS/INP） | `perf/web-vitals.spec.ts` | ✅ @gate |

## 已知差距（基线/TODO）

| 发现 | 来源 | TODO |
|---|---|---|
| 模态未支持 Esc 关闭（WAI-ARIA） | `ux/*` @advisory | `TODO-ui-modal-esc-close-wai-aria` |
| 画布 color-contrast ×6（serious） | `a11y/canvas` 基线 | `TODO-ui-a11y-canvas-color-contrast` |
| /records 表单缺 label/select-name ×3（critical） | `a11y/records` 基线 | `TODO-ui-a11y-records-form-labels` |

## 待补（迭代）
- CV 规则校验、防错/反馈/空状态可用性样例、records/canvas 更细的 E2E、响应体 schema 逐端点校验、移动端 a11y/ux 扩展、CI 接线。
