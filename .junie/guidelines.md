# Project Guidelines

## Repository Structure
This repository is a mono-repo and must follow this structure:
- backend/
- frontend/
- sdks/

- Backend code must only exist inside /backend.
- Frontend code must only exist inside /frontend.
- SDK implementations must be placed inside /sdks/{language}.
- Do not mix layers or cross these boundaries.

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

- API
  - Controllers must not access repositories directly.
  - Controllers may only communicate with services.
  - Controllers only expose and accept DTOs, which are mapped to/from domain entities.
  - DTOs must be used for all external communication.
- Business
  - Services contain business logic.
  - Services should accept Domain entities or "primitve" parameters.
  - If required, a Service may expose an internal DTO (for example: `class MyService { data class Input(/**/); data class Output(/**/); fun myFunction(input: Input): Output { /** } }`)
- Domain
  - Repositories are only used inside services.
  - Repositories are located inside the domain layer.
  - Domain entities must never be exposed directly in API responses.
  - Entity Namings
    - All entity classes MUST end with the name suffix "Entitiy".
    - All entities must have an `@jakarta.persistence.Entity` and `@jakarta.persistence.Table` annotation.
    - The `@jakarta.persistence.Entity` annotation must contain the name without the "Entity" suffix
    - The `@jakarta.persistence.Table` annotation must contain the name in lower snake case in plural
      - For example: An entity for a User must look like this: `@jakarta.persistence.Entity("User"); @jakarta.persistence.Table("users"); class UserEntity {}`


- Always separate classes by files! Do not put multiple classes in a single file!

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

##### Dto Mapping

DTOs are part of the API layer.
Because of that, mapping should happen "DTO centric" (i.e. controlled from the DTO, not the domain entity).

The DTO mapping should happen using extension functions on the entity.
For example, if a `FooEntity` entity is mapped to a `FooDto`, the mapping should be done using an extension function like `val fooEntity = FooEntity(); fooEntity.toDto()`.
If possible, the dto should have a `toEntity` function that maps the dto to an entity. If there are any required parameters that cannot be resolved with "toEntity" functions of other DTOs, they should be passed as parameters to the `toEntity` function.
These mapping functions must be in the same file as the `FooDto`, to it might look like this:
```kotlin
class FooDto(/* Parameter */) {
  fun toEntity(/* Dependend Parameters */): FooEntity {
      return FooEntity(/* Parameter and Dependend Parameters */)
  }
}

fun FooEntity.toDto() {
    return FooDto(/* Parameters */)
}
```

#### Testing

- Use JUnit 5.
- Service layer must have unit tests.
- Every service must have at least one unit test testing every business logic happy and unhappy path.
- Controllers must have integration tests.
- Every controller must have at least one integration test testing every endpoint happy path.