# System Settings

## Overview
The **Settings** section allows administrators to configure global behavior and security policies for the entire Vivid instance.

## Global Configuration
These toggles control how Vivid interacts with client applications.

### Key Features
*   **Require Client Tokens:** When enabled, every SDK request must include a valid `Client Token`. If disabled, requests without tokens will be accepted (but may have limited functionality if dynamic registration is also disabled).
*   **Allow Dynamic Client Registration:** When enabled, Vivid will automatically create a new client entry in the registry if it receives a request from an unknown `App Name`.
*   **Online Threshold:** A duration picker to define how long a client is considered "Online" after its last heartbeat. For example, if set to `5 minutes`, a client that hasn't communicated for over 5 minutes will appear as "Offline" in the UI.
*   **Distribution Providers:** A list of external feature flag providers that are currently integrated with Vivid.

### Permission Mapping

| Action | Required Role |
| :--- | :--- |
| View System Settings | `vivid:settings:read` |
| Modify & Save Settings | `vivid:settings:write` |

For more details on roles, see [Security & Permissions](../security/permissions.md).
