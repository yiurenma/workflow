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

## 全量补充（2026-06-21 第二轮）

**新增套件与最终结果：全套件 103 passed / 12 skipped(均为已知 fixme/视口跳过) / 0 failed；硬门禁 @gate 62 passed / 0 failed。**

| 套件 | 覆盖 | 状态 |
|---|---|---|
| `api/operation-full.spec.ts` | TC-01/02/03/10/11/18/19 + 错误码 WF-400-101/102/301/302/401/402、WF-404-000/101（自带数据可重复） | ✅ 19/19 @gate |
| `api/online-full.spec.ts` | M0001、440000、缺参数、非法 body；M0002/M0004 需 keystore（fixme） | ✅ 5 + 2 fixme |
| `a11y/home.a11y.spec.ts` | 主页 / + 关于页 /about（桌面+移动） | ✅ @gate |
| `e2e/home.spec.ts` | CV-US-45 文案 + CV-US-46 favicon + CTA 导航 | ✅ |
| `e2e/carbon-tokens.spec.ts` | CV-AC-36：导航 #161616、标准按钮 0px、主色 #0f62fe | ✅ @gate |
| `perf/web-vitals-pages.spec.ts` | 主页/画布 CWV | ✅ @gate |
| `visual/pages.visual.spec.ts` | 主页/记录/画布 基线 | ✅ @advisory |
| `ux/records.ux.spec.ts` | 记录页 + 导航触控目标 | ✅ @gate |

**对比度基线按视口生效**：桌面严格、移动端豁免共享 nav 对比度债（→ `TODO-ui-a11y-mobile-nav-contrast`）。

## 已知差距（基线/TODO，套件抓到的真实缺陷）
| 发现 | TODO |
|---|---|
| 模态未支持 Esc 关闭（WAI-ARIA） | `TODO-ui-modal-esc-close-wai-aria` |
| 画布 color-contrast ×6（serious） | `TODO-ui-a11y-canvas-color-contrast` |
| /records 表单缺 label/select-name ×3（critical） | `TODO-ui-a11y-records-form-labels` |
| 移动端 nav 激活态对比度不足 | `TODO-ui-a11y-mobile-nav-contrast` |
| online 执行/幂等需 keystore 密钥 | `TODO-tests-online-api-keystore-secret-for-execution` |
| CI 阻断接线（各 repo） | `TODO-tests-ci-wiring-gate` |

## 画布全功能 E2E（2026-06-21 第三轮，`e2e/canvas-full.spec.ts`）

多角度覆盖；用 Import 注入多节点工作流作为种子（客户端，不依赖后端数据）。桌面 22 passed / 3 skip(1 advisory + 2 @uat)；移动端 4 passed（结构层）+ 富交互待溢出菜单。

| 角度 | 用例 | 状态 |
|---|---|---|
| CV-US-04 打开/浏览/平移缩放 | A1-A3 | ✅ 桌面+移动 |
| CV-US-05/42 调色板+描述 | B | ✅ 桌面 |
| CV-US-53/54/55/57 导入校验（合法/非法类型/重复ID/缺字段/IFELSE分支/markdown围栏/Apply） | C1-C7 | ✅ 桌面 |
| CV-US-07/37/41/30 节点抽屉（打开/只读默认+Edit/关闭按钮/点空白关闭/可改宽） | D1-D5 | ✅ 桌面 |
| CV-US-50 规则键 JSONPath 内联校验 | E1 | ✅ 桌面（Done禁用=E2 fixme→TODO） |
| CV-US-08/43 删除节点+清边 | F1 | ✅ 桌面 |
| CV-US-17 Test 运行模态 | G1（G2 真执行=@uat） | ✅/🔵 |
| CV-US-20/21/44 AI Explain/Generate 模态 | H1/H2（真返回=@uat） | ✅/🔵 |
| CV-US-09 保存 | I1（I2 持久化=@uat） | ✅/🔵 |

> `@uat` 项需 UAT egress 放行后 `RUN_UAT=1` 实跑（Run 真执行、Save 持久化、AI 真返回）。

## 待补（后续迭代）
- CV 规则键 JSONPath 校验的 E2E（需打开节点抽屉）、防错/反馈/空状态可用性样例接入、响应体 schema 逐端点 ajv 校验、settings/copy/history/deploy 的 UI E2E（mock 静态，需更全 mock 或 UAT）、移动端更多 ux 扩展。
