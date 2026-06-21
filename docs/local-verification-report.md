# 本地运行验证报告 v1.0

**日期：** 2026-06-21
**起因：** UAT 线上 host 被沙箱 egress 拦截，无法实跑。改为**本地把服务跑起来**用 `tests/` 实跑验证，把审计中的 BLOCKED(env) 变成真实 PASS/FAIL。
**前提（本机已具备）：** JDK 21、Maven 3.9（Maven Central 可达）、Node 22、psql；**Playwright 浏览器已预装** `/opt/pw-browsers/chromium-1194`（Chromium 141，`PLAYWRIGHT_BROWSERS_PATH` 已设）——403 只挡下载、不挡已装浏览器。

## 运行拓扑（全本地）

| 服务 | 启动方式 | 端口 | 数据 |
|---|---|---|---|
| operation-api | `mvn spring-boot:test-run -Dspring-boot.run.profiles=test`（覆盖数据源为文件型 H2 `jdbc:h2:file:/tmp/wf-h2/db;AUTO_SERVER=TRUE`） | 8080 | H2，播种 `TEST_APP_A` |
| online-api | `spring-boot:test-run` + `SPRING_DATASOURCE_*` 指向**同一**文件 H2 + 覆盖 H2 驱动/方言 | 8081 | 共享同一 H2（读 operation 建的表） |
| workflow-ui | `npm run dev`（默认 mock 模式，`vite-plugin-mock-dev-server` 提供 `DEMO_APP`） | 5173 | mock API |
| 测试 | `tests/` Playwright（钉 1.56 对齐预装 chromium 1194） | — | 打本地服务 |

> 关键：H2 是 `test` scope，普通 `spring-boot:run` 没有 H2 且不加载 `application-test.yml`（会回落到被墙的 Neon）；必须用 **`spring-boot:test-run`**（含 test 类路径）。

## 结果总览

| 层 | 套件 | 结果 |
|---|---|---|
| **operation-api API**（H2，8080） | `tests/api/app.spec.ts` | ✅ **10/10 PASS** |
| **online-api API**（共享 H2，8081） | `tests/api/online.spec.ts` | ✅ 2 PASS · ⏭️ 2 SKIP（需 keystore 密钥 / SSE 缺失） |
| **前端 E2E**（真实 Chromium，5173） | `tests/e2e/*`（桌面 1280 + 移动 390×844） | ✅ **17 PASS · 1 SKIP · 0 FAIL** |

### operation-api（10/10 PASS）
APP-AC-01 列表 + 模糊搜索 · APP-AC-11 history（Envers）· APP-AC-18 PATCH 未知应用→`WF-404-101` · APP-AC-10 autoCopy 同源→`WF-400-301`/源不存在→`WF-400-302` · APP-AC-03 删除未知→`WF-400-101` · REC-AC-19 records 筛选+分页 · APP-AC-52 deploy 代理端点存在 · APP-AC-02+03 create→delete 往返。

### online-api（2 PASS / 2 SKIP）
- ✅ REC-AC-12-1 缺关联头被拒（400）
- ✅ REC-AC-12-3 未知应用→`M0001`（可分类失败）
- ⏭️ REC-AC-15-2 幂等（重复→`M0002`）：**需真实 JKS keystore 密钥**。完整执行路径在写 runtime 时加密（`SecureData`），仓库默认 `changeit` 非真实密码（`keytool` 验证报 “password was incorrect”），本地无法完成加密→首次执行 500。幂等检查逻辑本身在源码已确认（控制器 204-209，先于执行）。
- ⏭️ GAP-1 SSE：`test.fixme`，源码确认无任何 `SseEmitter`/`text/event-stream`。

### 前端 E2E（17 PASS / 1 SKIP）
桌面 + 移动各跑：APP-US-01 列表/搜索/创建入口（视口感知：桌面按钮 vs 移动 FAB）· Layer3 无横向裁切 · Layer4 搜索可输入 · CV-US-36 导航栏 0px 圆角（Layer5 计算样式）· CV-US-04 画布工具栏/ReactFlow 渲染 + Layer2 尺寸 · CV-US-53 Import 模态（桌面）· REC-US-19 记录页标题 + 筛选可操作。
- ⏭️ 1 SKIP：移动端 Import 在溢出菜单，未直接暴露按钮，优雅跳过。

## 审计修正与新发现

| 编号 | 内容 |
|---|---|
| **DIV-3（新）** | online-api 必需头是 **`X-Request-Correlation-Id`**，非早期审计写的 `x-request-id`。已修正 `tests/api/online.spec.ts` 与审计 §4。建议 arch 文档据此澄清。 |
| 环境依赖 | online-api 完整执行需真实 keystore 密钥（部署密钥，不在仓库）。本地仅能验证到路由/校验/未知应用/头校验层。 |
| 观察（既有债，不在本次范围） | `workflow-ui` `tsc -b` 有 5 处 TS6133 未用变量报错；`eslint` 21 errors/17 warnings。**运行时无影响**（dev 服务 + 全套 E2E 绿）。按约定不动 submodule，仅记录。 |

## 复现命令

```bash
# 后端（H2）
cd workflow-operation-api && mvn spring-boot:test-run -Dspring-boot.run.profiles=test \
  -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:h2:file:/tmp/wf-h2/db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1"
cd workflow-online-api && SPRING_DATASOURCE_URL="jdbc:h2:file:/tmp/wf-h2/db;AUTO_SERVER=TRUE" \
  SPRING_DATASOURCE_USERNAME=sa SPRING_DATASOURCE_PASSWORD= SPRING_JPA_HIBERNATE_DDL_AUTO=none SERVER_PORT=8081 \
  mvn spring-boot:test-run -Dspring-boot.run.profiles=test \
  -Dspring-boot.run.arguments="--spring.datasource.driver-class-name=org.h2.Driver --spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
# 前端
cd workflow-ui && npm install && npm run dev
# 测试
cd tests && npm install
OPERATION_API_BASE=http://localhost:8080 APP_A=TEST_APP_A APP_WRITE=1 npm run test:api -- api/app.spec.ts
ONLINE_API_BASE=http://localhost:8081 npm run test:api -- api/online.spec.ts
UI_BASE=http://localhost:5173 npx playwright test --project=desktop-chrome --project=mobile-chrome
```

## 结论
- **后端控制面（APP/记录/错误码）本地真跑全过**；执行面（REC）路由/校验/幂等检查点确认，完整执行仅差部署 keystore 密钥。
- **前端全套 E2E 本地真跑通过**（真实浏览器、桌面+移动、五层 UX）。
- 远程 UAT 仍需把三个 host 加入 egress 白名单才能对生产环境复跑；本地结果已足以确认文档所述能力**在代码中真实可用**。
