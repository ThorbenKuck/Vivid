# Implement a Dynamic Environment Permission System

## Background

You are looking at a project called "Vivid".
This project is designed as a feature control system.
It allows users to control and monitor features in different environments or departments.
The goal is to allow all users, not only technically savvy ones, to easily manage and understand feature states.
Also, it shares knowledge about what feature exists on which stage.

## Problem Statement

Users can do everything in the system.
This is a security risk.
We need to implement a dynamic permission system that allows users to control what they can do in the system.

## Solution

We need to integrate a role-based access control system respecting dynamic "environments" (e.g., 'pr', 'staging', 'dev' etc.).
The logic should be backend-driven, using Keycloak as the Identity Provider.

### General Context & Requirements

A user should be able to access and see only the elements of the UI they have the rights to.

1. **Keycloak Role Schema:**
  - `environments:read` / `environments:write` (read or write access to the environment overview)
  - `teams:read` / `teams:write` (read or write access to the team overview)
  - `departments:read` / `departments:write` (read or write access to the department overview)
  - `admin` (Full access to everything, INCLUDING TO ALL ENVIRONMENTS!!!)
    - IMPORTANT! A user with this role should be able to do everything in the system!
    - EVERYTHING!
2. **Backend-Driven Logic:** An endpoint must validate the JWT and return a calculated permission set to the frontend.
3. **Current State:** Keycloak user synchronization with the local DB during login is already implemented. Check if this can be reused / expanded to include the roles.

### Environment Specific Context & Requirements

In addition to the general permissions, we can also have different permissions for each environment.
These permissions are dynamic because environents can be added or removed at any time.

1. **Keycloak Role Schema:**
- `env:admin` (Full access to all environments)
- `env:all:read` / `env:all:write` (Wildcards for all environments)
- `env:[envId]:read` / `env:[envId]:write` (Environment-specific rights)
2. **Hierarchy:** Admin > All-Wildcard > Specific Right.

Use these roles in the Backend to:
- Filter out environments that the user does not have access to when returning data from the backend.
- Validate if the user has the required rights to update a specific environment.

Use these roles in the Frontend to:
- Render UI elements based on the user's permissions.
  - If the user has not write permissions to a specific environment, add a "lock" icon to the UI, on hover show a tooltip that the user has no write permissions.
  - Disable all inputs for the environment if the user does not have write permissions.

### 1. Backend: Security Service & Controller (Spring Boot)
- Create a `PermissionService` that extracts `realm_access.roles` from the `JwtAuthenticationToken`.
- Implement a method `getEffectivePermissions()` that returns a DTO or Map (e.g., `{ "environments": "read", "teams": "write", "environment": { "all": "read", "dev": "write", "staging": "write" } }`) based on the hierarchy mentioned above.
- Create a REST controller `GET /api/auth/permissions` that serves this object to the frontend.
- Secure API endpoints using `@PreAuthorize` with a SpEL expression that utilizes the new `PermissionService`.

### 2. Frontend: Permission Management (Angular)
- Create a `PermissionService` that fetches data from `/api/auth/permissions` upon app initialization (or post-login) and stores the permissions locally.
- Implement a structural directive named `*hasPermission`.
  - Syntax example: `*hasPermission="['teams', 'write']"`
  - The directive should only render the element if the `PermissionService` confirms the user has the required rights.
- Implement a structural directive named `*hasEnvPermission`.
  - Syntax example: `*hasEnvPermission="['pr', 'write']"`
  - Expands the *hasPermissions directive to check specifically for the environment-specific access rights.

### 3. Integration & Refactoring
- Ensure the roles can be extracted from any OIDC provider.
  - For tests, we will use Keycloak, but we should support other providers as well.
- Use the existing local User entity if additional metadata is required for the permission check.

## Desired Outcome
Provide a consistent solution where UI elements in Angular can be easily protected based on Environment IDs, while the backend remains the "Source of Truth" for all authorization decisions.

## Technical Requirements

- Make sure to use the variables defined in style.css
  - We need to make sure that theme switching is uneffected.
- Follow the existing code style.
- Do not use external libraries in the frontend for the new features!
- Separate classes by files. Do not put multiple classes in a single file.
- Make breaking API changes. We are not live, this is okay. Better than having dead code lines.

## Non Technical Requirements

- Develop the frontend mobile-first.
- Make sure to follow the existing designs.
- Make sure to follow the existing code style.
- Double-check your work.
  - If you are not 99% sure what to do, ask for clarification.
  - Double-check also that the code is following the existing code style and guidelines.