# Project Guidelines

## Repository Structure
This repository is a mono-repo and must follow this structure:
- backend/
- frontend/
- sdks/

All backend-related code must live strictly inside /backend.
No backend code may be generated outside this directory.

Frontend code must only exist inside /frontend.

SDK implementations must be placed inside /sdks/{language}.

Do not mix layers or cross these boundaries.

### Backend (Spring Boot + Kotlin + Maven)

The backend must be written in Kotlin.
The backend must use Spring Boot.
The backend must use Maven for build management.
The backend must follow clean architecture principles.

Technologies:
- Spring Boot 4
- Kotlin 2.20.2
- Spring Data JPA (inherited from spring boot starter)
- PostgreSQL (newest)
- Flyway (inherited from spring boot starter)

#### Layering Rules

The backend application must have only 3 layers:

- api
- service
- domain

*No other layers are allowed on the root level. All other layers must be nested inside one of these.*

Layers must be open, unidirectional and downward-only.
Api may access service and domain.
Service may access domain.
Domain may not access anything else.
Upwards dependencies must be resolved via constructor injections of interface implementations.

#### Rules:

- Controllers must not access repositories directly.
- Controllers may only communicate with services.
- Services contain business logic.
- Repositories are only used inside services.
- Repositories are located inside the domain layer.
- Domain entities must never be exposed directly in API responses.
- DTOs must be used for all external communication.
- Mapping must be done via functions in the DTOs.
  - For converting DTOs to entities, use/create `toEntity()` functions with mandatory parameters.
  - For converting entities to DTOs, use the `toDto()` extension functions (`fun DomainEntity.toDto(): DTO {}`).
- Separate classes by files. Do not put multiple classes in a single file.
- Extension functions are always placed in the same file as the class they extend.

#### Code Quality

- Kotlin idiomatic style.
- Constructor injection only. No field injection.
- No global mutable state.
- Use data classes for DTOs.
- Use clear naming conventions.
- Avoid magic strings.
- No hardcoded configuration values. Use Spring property files.

#### Apis

Dual API Separation.
Two strictly separated APIs must exist.

- Web API
  - Base path: /api/web/**
  - Used by the frontend applications.
- Client API
  - Base path: /api/client/**
  - Used by application SDKs.

Controllers of these APIs must live in separate packages:
- Web API: `api.web`
- Client API: `api.client`

They must not share controller classes.
All apis should be versioned and documented using code-first swagger.

#### Testing

- Use JUnit 5.
- Service layer must have unit tests.
- Every service must have at least one unit test testing every business logic happy and unhappy path.
- Controllers must have integration tests.
- Every controller must have at least one integration test testing every endpoint happy path.