# Vivid

Welcome to the Vivid repository!

Vivid is a feature management platform with the aim to give your whole team control over features.
The official documentation is available at [GitHub Pages](https://thorbenkuck.github.io/Vivid/).

## Repository Structure

This repository is structured as a mono‑repo.
Its split into four main sections:

- backend/
  The backend application of Vivid, written in Kotlin with Spring Boot.
- frontend/
  The frontend application of Vivid, written in Angular.
- sdks/
  A collection of SDKs for Vivid clients.
- test/
  A test Applikation for locally testing Vivid.

---

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Node.js 18+ and npm
- Docker (optional but recommended for local PostgreSQL)

Optional:
- A recent Angular CLI (the project includes `@angular/cli` as a dev dependency so `npm start` works without a global install).

---

## Starting the Backend

Before you get started, I highly recommend that you setup your IDE project.
This will make it easier to run the backend application.
Vivid uses maven for its backend.

To set up the backend in your IDE, you need to import the project as a maven project.
How to do that depends on your IDE.

> [!NOTE]
> For IntelliJ, right-click on the pom.xml and select the last context menu action `+ Add as Maven Project`.
> Then wait for the project to be imported.

Once done with that, you'll have imported the backend, test application and the SDKs into your IDE.
This makes it easier to run the backend application.

> [!WARN]
> If you don't integrate the IDE, your backend will complain about missing dependencies on first start.
> Specifically the client-api module, which contains the contract between the backend and the clients.
> 
> To fix this, you'd need to manually `mvn clean install` in the `sdks/client-api` module.
> This will compile the contract and install it into your local maven repository.
> 
> Additionally, the test application will complain about missing dependencies as well.
> 
> So if you don't want to integrate all modules at once, you'll need to manually `mvn clean install` all the required `sdks/*` modules.

After you set up your project and compiled the dependencies, you can start the backend application.
Vivid's backend requires two things when running, a PostgreSQL database and a KeyCloak server.
Both can be run either locally or in a container.
For ease of use, you can start up a docker container using docker-compose.

For that, you need to have Docker installed.
With docker installed, you can run:

```
cd backend
docker compose up -d
```

This will start up a PostgreSQL database and a KeyCloak server.
- The database is setup at `localhost:5333` with username `postgres`, password `postgres` and db name `vivid`.
  - You can connect to the database using the tool of your choice (e.g. pgAdmin, IntelliJ, DBeaver, etc.).
- The KeyCloak server is available at [localhost:8989](http://localhost:8989). The root user has username `admin` and password `admin`.
  - The DB of the KeyCloak is isolated and not accessible from outside.

With that setup, you can start the backend application.
Either by navigating to the `backend/` directory and run `mvn spring-boot:run` or finding the class `com.vivid.backend.VividApplication` and starting it with your IDE of choice.

Relevant URLs of the backend are:
- Application starts on [http://localhost:8080](http://localhost:8080).
- OpenAPI/Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs).

To stop and remove the containers later, run:
```
cd backend
docker compose down
```

---

## Starting the Frontend (Angular)

Vivids frontend is a single page application (SPA) written in Angular.
It is built with Angular CLI and uses as little dependencies as possible.
To start the frontend, run:

```
cd frontend
npm install
npm start
```

The application will then be available under [http://localhost:4200](http://localhost:4200).

> [!NOTE]
> By default, the frontend will try to reach the backend at `http://localhost:8080`.
> If you changed the port of the backend, you need to change it in `frontend/src/environments/environment.ts` as well.
