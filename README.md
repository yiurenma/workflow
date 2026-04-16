# Workflow Studio

低代码 API 编排与自动化平台 — 通过可视化拖拽界面设计数据处理流水线，将 API 请求经过多步骤的数据增强、条件判断、转换后，分发到下游系统。

## 架构概览

```
                          ┌─────────────────────┐
                          │    workflow-ui       │  ← 可视化工作流编辑器
                          │  (React + React Flow)│
                          └────────┬────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    ▼                              ▼
         ┌──────────────────┐           ┌──────────────────┐
         │  operation-api   │           │   online-api     │
         │   (控制面)        │           │    (数据面)       │
         │ 定义、配置、审计   │           │ 接收请求、执行管道 │
         └────────┬─────────┘           └────────┬─────────┘
                  │                              │
                  └──────────┬───────────────────┘
                             ▼
                     ┌──────────────┐
                     │  PostgreSQL  │  ← 共享数据库
                     └──────────────┘
```

## 子项目

| 子项目 | 说明 | 技术栈 |
|--------|------|--------|
| [workflow-operation-api](./workflow-operation-api) | 控制面 — 工作流定义 CRUD、实体配置、版本历史、执行记录查询 | Spring Boot 4, Java 21, JPA + Envers, QueryDSL |
| [workflow-online-api](./workflow-online-api) | 数据面 — 接收 API 请求，执行 enrichment → dispatch 管道 | Spring Boot 4, Java 21, OpenFeign, AES 加密 |
| [workflow-ui](./workflow-ui) | 前端 — 可视化拖拽式工作流编辑器 | React 19, TypeScript, React Flow, Ant Design 5, Vite 6 |
| [workflow-agent-teams](./workflow-agent-teams) | 文档与资源 | — |

## 核心概念

### 工作流（Workflow）

每个工作流绑定一个 `applicationName`（应用名），由多个有序步骤组成。步骤按 `logicOrder` 排序，同一 `logicOrder` 的步骤可并行执行。每个步骤包含：

- **规则（Rules）** — JsonPath 表达式，用于匹配负载数据，全部匹配才执行该步骤
- **类型（Type）** — 步骤的具体动作（HTTP 调用、条件判断、函数转换、消息分发等）
- **linkingId** — 将规则和类型关联到同一个步骤

### 执行管道（Pipeline）

当 `online-api` 收到请求时，按以下流程执行：

```
请求进入 → 去重检查 → 加载工作流定义
    │
    ▼
Enrichment 阶段（按 logicOrder 顺序）
    ├── HTTP Fetch (CONSUMER)        — 调用外部 API 获取数据
    ├── Safe Fetch (CONSUMER_W/O_ERR) — 同上但忽略错误
    ├── Condition (IF_ELSE)          — 条件分支判断
    └── Transform (FUNCTION)         — 自定义数据转换
    │
    ▼
Dispatch 阶段
    └── Dispatch (MESSAGE)           — 分发到下游服务
    │
    ▼
记录执行结果 → 加密存储
```

### 画布节点类型

| 节点 | 显示名称 | 后端类型 | 用途 |
|------|----------|----------|------|
| HTTP Fetch | Consumer | `CONSUMER` | 调用外部 API，将响应合并到负载 |
| Safe Fetch | ConsumerWithoutError | `CONSUMER_WITHOUT_ERROR` | 同上，但失败时不中断流程 |
| Condition | IfElse | `IF_ELSE` | 根据条件走不同处理分支 |
| Transform | Function | `FUNCTION_V2` | 通过自定义函数转换数据 |
| Transform+ | FunctionV3 | `FUNCTION_V3` | 增强版数据转换 |
| Dispatch | Message | `DISPATCH` | 将处理后的数据分发到下游系统 |

## 产品特性

- **可视化流水线设计** — 拖拽式画布编辑器，支持桌面和移动端
- **多步骤编排** — 数据获取、条件判断、函数转换、消息分发的完整管道
- **JsonPath 规则引擎** — 通过 JsonPath 表达式灵活匹配数据
- **版本控制与回滚** — 基于 Hibernate Envers 的完整审计历史，支持一键回滚
- **执行追踪** — 完整的执行记录链，包含父子关系和状态追踪
- **同步/异步模式** — 可按应用配置执行模式
- **安全性** — AES/CBC 加密执行数据，Trust Token 服务间认证
- **API 优先** — 支持 JSON/XML 输入，完整的 OpenAPI/Swagger 文档
- **可观测性** — Micrometer 链路追踪、Logbook HTTP 日志、Actuator 健康检查

## 典型使用场景

- **企业系统集成** — 收到订单 → 调用 CRM 获取客户信息 → 条件判断 → 发送通知到不同渠道
- **数据管道编排** — 接收 Webhook → 多源数据增强 → 格式转换 → 分发到下游系统
- **自动化工作流** — 基于规则的多步骤 API 调用链，支持重试和错误处理

## 快速开始

### 克隆仓库

```bash
git clone --recurse-submodules https://github.com/yiurenma/workflow.git
cd workflow
```

如果已克隆但子模块为空：

```bash
git submodule update --init --recursive
```

### 启动后端服务

```bash
# Operation API (控制面，默认端口 8081)
cd workflow-operation-api
./mvnw spring-boot:run

# Online API (数据面，默认端口 8080)
cd workflow-online-api
./mvnw spring-boot:run
```

两个后端共享同一个 PostgreSQL 数据库，`operation-api` 负责 DDL 更新（`ddl-auto: update`），`online-api` 不执行 DDL（`ddl-auto: none`）。

### 启动前端

```bash
cd workflow-ui
npm install
npm run dev
```

前端开发服务器会将 `/api/proxy/operation` 和 `/api/proxy/online` 代理到对应的后端服务。

### API 文档

- Operation API Swagger UI: http://localhost:8081/swagger-ui.html
- Online API Swagger UI: http://localhost:8080/swagger-ui.html

### 产品演示视频（FFmpeg）

使用仓库内 UAT 截图与 Logo 一键生成 **1920×1080** 演示片（输出在 `artifacts/`，已忽略版本控制）：

```bash
./scripts/generate-product-demo-video.sh
```

说明与分镜、画质参数见 [docs/product-demo-video.md](./docs/product-demo-video.md)。

## 项目结构

```
workflow/                        ← 聚合仓库
├── workflow-operation-api/      ← 控制面 API (submodule)
│   ├── src/main/java/com/workflow/
│   │   ├── controller/          ← REST 控制器
│   │   ├── dao/repository/      ← JPA 实体与仓库
│   │   └── common/              ← 配置、异常处理、工具类
│   └── pom.xml
├── workflow-online-api/         ← 数据面 API (submodule)
│   ├── src/main/java/com/workflow/
│   │   ├── controller/          ← 入口控制器
│   │   ├── service/             ← 管道执行逻辑
│   │   ├── dao/repository/      ← JPA 实体与仓库
│   │   └── common/              ← 加密、认证、配置
│   └── pom.xml
├── workflow-ui/                 ← 前端 (submodule)
│   ├── src/
│   │   ├── routes/              ← 页面路由
│   │   ├── components/          ← React 组件
│   │   ├── api/                 ← API 调用层
│   │   └── hooks/               ← 自定义 Hooks
│   └── package.json
└── workflow-agent-teams/        ← 文档与资源
```
