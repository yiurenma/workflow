# Workflow monorepo — repository index

Use this table when wiring **Agent Teams** or onboarding: each row links the canonical README for that service.

| Repository | Role | README |
|------------|------|--------|
| **workflow-operation-api** | Control plane — workflow CRUD, entity settings, OpenAPI for UI | [workflow-operation-api/README.md](../workflow-operation-api/README.md) |
| **workflow-online-api** | Online ingress — `POST /api/workflow`, shared DB execution pipeline | [workflow-online-api/README.md](../workflow-online-api/README.md) |
| **workflow-ui** | Management UI — Vite/React, proxies or env bases to both APIs | [workflow-ui/README.md](../workflow-ui/README.md) |

Process and role definitions for this product line live in [agent-system.md](agent-system.md) (Workflow 三仓已映射) and [agents/](agents/).
