# Low-Code Workflow

A low-code project built with **Spring Boot 4.0.3** and **JDK 21**.

## Prerequisites

- **JDK 21** or higher
- **Maven 3.9+** (or use the included Maven Wrapper)

## Quick Start

```bash
# Build the project (using Maven Wrapper - no Maven install needed)
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

Or with Maven installed globally:

```bash
mvn clean install
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

## Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/health` | Simple health check |
| `GET /actuator/health` | Spring Boot Actuator health |
| `GET /actuator/info` | Application info |
| `http://localhost:8080/h2-console` | H2 Database Console (JDBC URL: `jdbc:h2:mem:workflowdb`) |

## Project Structure

```
src/
├── main/
│   ├── java/com/workflow/
│   │   ├── LowcodeWorkflowApplication.java   # Main entry point
│   │   └── controller/
│   │       └── HealthController.java         # Sample REST controller
│   └── resources/
│       └── application.yml                    # Configuration
└── test/
    └── java/com/workflow/
        └── LowcodeWorkflowApplicationTests.java
```

## Dependencies (Low-Code Friendly)

- **Spring Boot Web** – REST APIs
- **Spring Data JPA** – Data-driven entities with minimal code
- **H2 Database** – In-memory DB for development (swap for PostgreSQL/MySQL in production)
- **Lombok** – Reduces boilerplate
- **Actuator** – Health checks and metrics
- **DevTools** – Hot reload during development

## Configuration

Edit `src/main/resources/application.yml` to customize:

- Server port
- Database connection (for production, switch from H2 to PostgreSQL/MySQL)
- JPA/Hibernate settings
