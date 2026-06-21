# tests/ — 测试驱动文档审计套件

每个测试**编码一条文档里的 US/AC**（来自 `docs/baseline/pm-doc-master.md` v2.29），对 UAT 环境实跑；PASS/FAIL 即为该条的审计结论（TDD：失败=未实现=一条 `docs/TODO-doc-gaps.md` 缺口）。

## 如何跑

```bash
cd tests
npm install
npx playwright install chromium
npx playwright test                 # 全部（api + desktop + mobile）
npm run test:api                    # 仅 operation/online API
npm run test:desktop                # 仅桌面 1280px UI
npm run test:mobile                 # 仅移动 390×844 UI
```

环境变量（默认指向 UAT，见 `playwright.config.ts`）：
`UI_BASE` · `OPERATION_API_BASE` · `ONLINE_API_BASE` · `APP_A`（已知存在的应用名）。
写操作默认跳过，需 `APP_WRITE=1`（operation 增删）/`ONLINE_WRITE=1`（执行）显式开启。

## ⚠️ 当前不可实跑

沙箱 egress 未放行三个 UAT host（`Host not in allowlist`）。本套件已写好但无法在此实跑——把三 host 加入环境 egress 允许列表后即可。详见 `../docs/doc-implementation-audit-v1.0.md` §0。

## 测试 ↔ 文档映射

| 文件 | 覆盖 | 域 |
|---|---|---|
| `api/app.spec.ts` | APP-US-01/02/03/10/18/52、REC-US-19；错误码 WF-* | operation-api |
| `api/online.spec.ts` | REC-US-12/15；**GAP-1 SSE**（`test.fixme`，待实现） | online-api |
| `e2e/applications.spec.ts` | APP-US-01；CV-US-36 Carbon（五层 L1/L3/L4/L5） | workflow-ui |
| `e2e/records.spec.ts` | REC-US-19（五层 L1/L4） | workflow-ui |
| `e2e/canvas.spec.ts` | CV-US-04/05/53（五层 L1/L2/L4） | workflow-ui |
| `fixtures/sample-workflow.json` | 6 种插件类型回归数据（CV-AC-54-4） | — |

## 五层 UX 框架（CV-US-38）

L1 存在 · L2 尺寸 · L3 视口（不裁切）· L4 交互 · L5 效果（计算样式/API 响应）。E2E 测试标题标注其验证的层。

## 已知缺口（套件内显式标记）

- `GAP-1` online-api SSE：`api/online.spec.ts` 用 `test.fixme` 标记，编码目标契约，实现后移除标记即转为回归测试。
- `GAP-4` E2E 选择器：本套件用 Carbon `.cds-*`/role 选择器（非 `.ant-*`），可作为 `workflow-ui/e2e` 选择器更新的参考。
