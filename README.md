### Vivid Monorepo

Vivid is a feature management platform with a cleanly separated backend (Spring Boot + Kotlin) and frontend (Angular). This repository is structured as a mono‑repo:

- backend/
- frontend/
- sdks/

This guide explains how to start the backend and the frontend locally.

---

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 18+ and npm
- Docker (optional but recommended for local PostgreSQL)

Optional:
- A recent Angular CLI (the project includes `@angular/cli` as a dev dependency so `npm start` works without a global install).

---

### 1) Start the Database (PostgreSQL)

You can run PostgreSQL using the provided Docker Compose file.

```
# from repository root
docker compose -f backend/docker-compose.yaml up -d
```

Defaults (configurable via env vars):
- Host: `localhost`
- Port: `5332` (container 5432 mapped to 5332)
- Database: `vivid`
- Username: `postgres`
- Password: `postgres`

The backend reads these via Spring properties (see `backend/backend/src/main/resources/application.yml`) and supports overrides with these environment variables:
- `DATABASE_URL` (default `jdbc:postgresql://localhost:5332/vivid`)
- `DATABASE_USERNAME` (default `postgres`)
- `DATABASE_PASSWORD` (default `postgres`)

To stop and remove the database container:
```
docker compose -f backend/docker-compose.yaml down
```

---

### 2) Start the Backend (Spring Boot + Kotlin)

```
cd backend/backend
mvn spring-boot:run
```

- Application starts on `http://localhost:8080`.
- Flyway will run database migrations on startup.
- OpenAPI/Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

APIs:
- Web API (management): `http://localhost:8080/api/web/**`
- Client API (runtime): `http://localhost:8080/api/client/**`

Run tests:
```
cd backend/backend
mvn clean test
```

---

### 3) Start the Frontend (Angular)

```
cd frontend
npm install
npm start
```

- App runs at `http://localhost:4200`.
- Default backend base URL is `http://localhost:8080` and can be changed via environment files:
  - `frontend/src/environments/environment.ts`
  - `frontend/src/environments/environment.prod.ts`

Frontend highlights:
- Mobile‑first layout with a collapsible sidebar. In mobile view the sidebar is fully hidden; on desktop it can collapse to icons‑only. The collapse/expand button is in the header (left side).
- Dark theme is default. Theme can be changed from the sidebar dropdown (light/dark). Theme variables are applied via the `data-theme` attribute on `<html>`.
- Environments: On initial load the app fetches all environments from the backend. If none exist, the header shows a “+ Add Environment” button (navigates to the Environments page). If one exists, it is auto‑selected; if multiple exist, the header shows a “Select environment …” dropdown. Selecting an environment affects the Features pages.

Backend Environment support (summary)
- The backend now models `Environment` and a `FeatureEnvironment` relation that stores `enabled`, `flags`, and `metadata` per environment.
- Web API endpoints support search and pagination for features and environments, and allow upserting environment‑specific state: `PUT /api/web/features/{id}/environments/{environmentId}`.
- Client API now requires `environmentId` when fetching enabled features: `GET /api/client/features?environmentId=...`.

---

### Troubleshooting

- PostgreSQL port already in use: adjust the host port in `backend/docker-compose.yaml` or stop the conflicting service.
- Database connection issues: verify `DATABASE_URL`, credentials, and that the container is healthy (`docker ps`, `docker logs`).
- Clean rebuild backend: `cd backend/backend && mvn clean install`.
- If the frontend cannot reach the backend, confirm CORS settings and that `environment.apiBaseUrl` matches the backend URL.

---

### Repository Structure (quick view)

```
backend/
  ├─ backend/                # Spring Boot Kotlin app (Maven)
  │  ├─ pom.xml
  │  └─ src/
  ├─ docker-compose.yaml     # Local PostgreSQL
frontend/
  ├─ angular.json
  ├─ package.json
  └─ src/
```
