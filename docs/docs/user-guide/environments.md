# Environments & Environment Details

## Overview
Environments in Vivid represent your different deployment stages (e.g., Development, Staging, Production). They allow you to maintain separate feature flag configurations and rules for each stage of your application lifecycle.

## Environment Management
The **Environments** page provides a high-level view of all configured stages.

### Key Features
*   **Environment List:** View all environments, their keys, and descriptions.
*   **Create Environment:** Add a new environment by providing a name and optional description. A unique key is automatically generated.
*   **Reordering:** Click **Reorder** to enable drag-and-drop sorting. The order here determines the display order in the Features overview and other parts of the UI.
*   **Deletion:** Remove an environment. 

:::danger
Deleting an environment is permanent and will remove all associated feature overrides and client presence data for that environment.
:::

### Permission Mapping

| Action | Required Role |
| :--- | :--- |
| View Environment List | `vivid:environments:read` |
| Create/Delete/Reorder Environments | `vivid:environments:write` |

---

## Environment Details
Opening an environment reveals its specific configuration and rules.

### Key Features
*   **Basic Information:** View the name, description, and the unique environment key used by SDKs.
*   **Rules:** Define automated behavior for the environment.
    *   **Match Environment:** Automatically synchronize settings from another environment if certain conditions are met (e.g., promotional flags).

### Permission Mapping

| Action | Required Role |
| :--- | :--- |
| View Details | `vivid:env:[envId]:read` or `vivid:env:all:read` |
| Modify Rules / Save Changes | `vivid:env:[envId]:write` or `vivid:env:admin` |

For more details on roles, see [Security & Permissions](../security/permissions.md).
