# Low-Code Workflow

## 项目描述

这是一个基于 **Spring Boot 4.0.3** 和 **JDK 21** 的低代码工作流平台，提供两类能力：

- **Workflow Management**：供低代码 UI 创建、更新、删除工作流
- **Online Execution**：统一在线 API 接收请求并执行对应工作流

## 本地需要的工具

- **JDK 21+**
- **Maven 3.9+**

## 本地启动

```bash
mvn clean install
mvn spring-boot:run
```

启动后访问：`http://localhost:8080`

常用地址：

- Health: `GET /api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`

## Architecture

### Workflow Management APIs (for UI)

| API                 | Description                                                |
| ------------------- | ---------------------------------------------------------- |
| **Create Workflow** | Define a new workflow in the low-code platform             |
| **Delete Workflow** | Remove a workflow by ID                                    |
| **Update Workflow** | Replace an existing workflow (internally: delete + create) |

### Online API (Request Execution)

A single **online API** serves as the entry point for all incoming requests:

1. **Request Ingestion** – Accepts any request regardless of path, headers, or body
2. **Runtime JSON** – Collects all request data (path, headers, query params, body) into a unified runtime JSON
3. **Workflow Selection** – Resolves and loads the workflow defined in the low-code platform
4. **Workflow Execution** – Runs the workflow to:
  - Call backend systems (API-based) and gather required data
  - Send the assembled payload to the fulfillment system for processing

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Request   │────▶│   Online API     │────▶│   Workflow      │
│ (any path/  │     │ (build runtime   │     │   Engine        │
│  header/    │     │  JSON)           │     │                 │
│  body)      │     └────────┬─────────┘     └────────┬────────┘
└─────────────┘              │                        │
                             │                        ▼
                             │               ┌─────────────────┐
                             │               │ Backend APIs    │
                             │               │ (data gathering)│
                             │               └────────┬────────┘
                             │                        │
                             │                        ▼
                             │               ┌─────────────────┐
                             │               │ Fulfillment     │
                             │               │ System          │
                             └──────────────▶│ (processing)    │
                                             └─────────────────┘
```

