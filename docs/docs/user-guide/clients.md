# Clients & Client Details (The Registry)

## Overview
The **Clients** section acts as a global registry for all applications and services that consume feature flags from Vivid. It provides visibility into which applications are active, their SDK versions, and their recent feature requests.

## Global Client Registry
The main Clients page lists all registered clients and their connectivity status across your environments.

### Key Features
*   **Client Identification:** Shows the application name and its unique **Client Token**.
*   **Environment Presence:** A visual summary of which environments the client is currently "Online" or "Offline" in.
*   **Search:** Filter clients by name or token.

### Permission Mapping

| Action | Required Role |
| :--- | :--- |
| View Client List | `vivid:clients:read` |
| Create New Client | `vivid:clients:write` |

---

## Client Details
The details view provides deep insights into a specific client's interaction with Vivid.

### Key Features
*   **Environment Presence Details:** Detailed connectivity stats, including:
    *   **Last Seen:** When the client last sent a heartbeat or request.
    *   **SDK Version:** The version of the Vivid SDK being used by the application.
*   **Feature Usage:** A history of which features this specific client has requested and in which environment.
*   **Management & Danger Zone:**
    *   **Rename Application:** Update the display name of the client. Requires typing the application name as confirmation.
    *   **Client Token Generation:** View or update the secret token used by the client for authentication. Requires typing the application name as confirmation.

:::warning
Changing a **Client Token** will immediately disconnect any application using the old token. The application will be unable to fetch features until its configuration is updated with the new token.
:::

:::danger
**Delete Client:** Deleting a client removes all its presence and usage history. This action requires explicit confirmation.
:::

### Permission Mapping

| Action | Required Role |
| :--- | :--- |
| View Details & Usage | `vivid:clients:read` |
| Rename Client / Update Token | `vivid:clients:write` |
| Delete Client | `vivid:clients:write` |

For more details on roles, see [Security & Permissions](../security/permissions.md).
