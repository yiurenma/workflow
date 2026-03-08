# Low-Code Workflow

A low-code workflow platform built with **Spring Boot 4.0.3** and **JDK 21**. It provides APIs for a UI to define and manage workflows, plus a single **online API** that executes workflows by gathering data from backend systems and forwarding to fulfillment systems.

## Overview

This platform enables:

- **Workflow Management** – Create, update, and delete workflows via API (consumed by a low-code UI)
- **Online Execution** – One unified API that accepts any incoming request, builds a runtime context, and runs the configured workflow end-to-end

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

## Prerequisites

- **JDK 21** or higher
- **Maven 3.9+**

## Quick Start

with Maven installed globally:

```bash
mvn clean install
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

## API Endpoints

### Workflow Management (for UI)


| Method   | Endpoint              | Description                                    |
| -------- | --------------------- | ---------------------------------------------- |
| `POST`   | `/api/workflows`      | Create a new workflow                          |
| `DELETE` | `/api/workflows/{id}` | Delete a workflow by ID                        |
| `PUT`    | `/api/workflows/{id}` | Update a workflow (delete + create internally) |


### Online API (Request Execution)


| Method | Endpoint     | Description                                                                                                                                                    |
| ------ | ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `*`    | `/online/**` | Single entry point – accepts any path, headers, and body; builds runtime JSON, loads workflow, gathers data from backend APIs, and sends to fulfillment system |


### System


| Endpoint                                | Description                                              |
| --------------------------------------- | -------------------------------------------------------- |
| `GET /api/health`                       | Health check                                             |
| `GET /actuator/health`                  | Spring Boot Actuator health                              |
| `GET /actuator/info`                    | Application info                                         |
| `http://localhost:8080/swagger-ui.html` | Swagger UI (API documentation)                           |
| `http://localhost:8080/v3/api-docs`     | OpenAPI JSON                                             |
| `http://localhost:8080/h2-console`      | H2 Database Console (JDBC URL: `jdbc:h2:mem:workflowdb`) |


## Project Structure

```
src/
├── main/
│   ├── java/com/workflow/
│   │   ├── LowcodeWorkflowApplication.java   # Main entry point
│   │   ├── controller/
│   │   │   └── HealthController.java         # Sample REST controller
│   │   └── dao/repository/
│   │       ├── WorkflowType.java            # Entity
│   │       ├── WorkflowRule.java
│   │       ├── WorkflowRuleAndType.java
│   │       ├── WorkflowRecord.java
│   │       ├── WorkflowReport.java
│   │       ├── WorkflowEntitySetting.java
│   │       ├── WorkflowEntityAndLinkingIdMapping.java
│   │       ├── *Repository.java             # JPA repositories
│   │       ├── Auditable.java
│   │       └── Shedlock.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/                    # Schema migration scripts
└── test/
    └── java/com/workflow/
        └── LowcodeWorkflowApplicationTests.java
```

## Online API Execution Flow

1. **Ingest** – Capture path, headers, query params, and body from the incoming request
2. **Build Runtime JSON** – Merge all request data into a single runtime context
3. **Resolve Workflow** – Select the workflow to run (based on low-code platform configuration)
4. **Execute Workflow** – For each step:
  - Call backend APIs to gather data
  - Transform and merge results into the runtime context
5. **Fulfill** – Send the final payload to the fulfillment system for processing

