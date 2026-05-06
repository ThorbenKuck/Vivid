# Security & Permissions

## Overview
Vivid uses OIDC (OpenID Connect) for authentication and a role-based access control (RBAC) system for permissions. Roles are typically managed in your OIDC provider (like Keycloak) and extracted from the JWT token.

### Role Extraction
Vivid looks for roles in the `realm_access.roles` claim of the incoming JWT. 
All Vivid-specific roles are expected to have a prefix (default: `vivid:`). This prefix is configurable in the backend properties.

## Global Roles & Resource Access
The following table describes how roles map to permissions within the Vivid UI and API.

| Role | Resource | Action | Impact |
| :--- | :--- | :--- | :--- |
| `vivid:admin` | All | Full | Global administrator access. Overrides all other permissions. |
| `vivid:all:write` | All | Write | Grant write access to all resources. |
| `vivid:all:read` | All | Read | Grant read access to all resources. |
| `vivid:features:read` | Features | Read | View features, tags, and global settings. |
| `vivid:features:write` | Features | Write | Create, edit, and delete features. |
| `vivid:clients:read` | Clients | Read | View registered clients and their status. |
| `vivid:clients:write` | Clients | Write | Register, update, or delete clients. |
| `vivid:settings:read` | Settings | Read | View global system settings. |
| `vivid:settings:write` | Settings | Write | Modify global system settings. |
| `vivid:environments:read` | Environments | Read | View the list of environments. |
| `vivid:environments:write` | Environments | Write | Create, delete, or reorder environments (Global Management). |

## Environment-Specific Permissions
Vivid allows fine-grained control over specific environments. This is particularly useful for protecting Production environments while allowing changes in Development or Staging.

*   **Environment Admin:** `vivid:env:admin` grants full read/write access to all environments.
*   **All Environments:**
    *   `vivid:env:all:read`: Read access to all environments.
    *   `vivid:env:all:write`: Write access to all environments.
*   **Specific Environments:**
    *   `vivid:env:[envId]:read`: Read access to a specific environment (e.g., `vivid:env:production:read`).
    *   `vivid:env:[envId]:write`: Write access to a specific environment (e.g., `vivid:env:production:write`).

:::tip
The `[envId]` corresponds to the technical name of the environment as configured in Vivid. If an environment is named "Production", the role would be `vivid:env:production:write`.
:::

## UI Visibility vs. API Security

### UI Impact
The frontend automatically adapts based on the user's permissions. Upon login, the frontend fetches the effective permissions from the `/api/auth/permissions` endpoint. This endpoint returns a `PermissionSetDto` containing the resolved permission levels for all resources and environments.

- **Sidebar Links:** Navigation items for Features, Clients, Settings, or Environments are only visible if the user has at least `read` access to that resource.
- **Buttons & Toggles:** "Create" buttons, "Delete" actions, and feature toggle switches are disabled or hidden if the user lacks `write` access.
- **Read-only Mode:** Forms and detail pages enter a read-only state when `write` access is missing.

### API Enforcement
Every API request is strictly validated on the backend using Spring Security's `@PreAuthorize` annotation.
- **Global Resources:** Secured via `@permissionService.hasPermission('resource', 'action')`.
- **Environment Overrides:** Secured via `@permissionService.hasEnvPermission(#envId, 'write')`.
- **403 Forbidden:** If a user attempts to access an endpoint without the required roles, the API returns a `403 Forbidden` response.

## Configuration Example
Here is an example of a JWT payload (claims) for a user with "Feature Manager" permissions allowed to modify the "development" environment and only view "production":

```json
{
  "sub": "12345678-1234-1234-1234-1234567890ab",
  "preferred_username": "jdoe",
  "realm_access": {
    "roles": [
      "vivid:features:read",
      "vivid:features:write",
      "vivid:env:development:write",
      "vivid:env:production:read"
    ]
  }
}
```

:::danger
Renaming an environment in Vivid will change the required role for specific access (e.g., from `vivid:env:old-name:write` to `vivid:env:new-name:write`). Ensure your OIDC provider roles are updated accordingly to prevent lockouts.
:::
