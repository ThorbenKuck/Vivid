# Getting Started

Ready to take control of your features? This guide will help you set up Vivid locally and get your first feature flag running.

## Prerequisites

Before starting, ensure you have the following installed:
- **Java 21** (JDK)
- **Maven 3.9+**
- **Node.js 18+** and npm
- **Docker** (optional, recommended for PostgreSQL)

## Local Installation

### 1. Database Setup

Vivid uses PostgreSQL for persistent storage. You can start it easily with Docker:

```bash
# From repository root
docker compose -f backend/docker-compose.yaml up -d
```

### 2. Start the Backend

Run the Spring Boot application using Maven:

```bash
cd backend/backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`. You can explore the Swagger UI at `http://localhost:8080/swagger-ui.html`.

### 3. Start the Frontend

Install dependencies and start the Angular development server:

```bash
cd frontend
npm install
npm start
```

The dashboard will be available at `http://localhost:4200`.

## Your First Feature Flag

Once everything is running, follow these steps to create your first flag:

1. **Add an Environment:** Go to the "Environments" page and create a new environment (e.g., `Production`).
2. **Create a Feature:** Navigate to "Features" and click "Add Feature".
3. **Configure the Flag:**
   - Give it a name like `new-search-algorithm`.
   - Enable it in your new environment.
   - Add any metadata (e.g., `algorithm_version: 2.0`).

:::note
Vivid is designed for speed. Use the Client API (`/api/client/features/{env}`) to fetch enabled features for your SDK.
:::

## Next Steps

Now that you have your first feature configured, learn more about:
- [Core Concepts](./core-concepts.md) to understand the internal logic.
- [SDKs](./sdks.md) to integrate with your applications.
