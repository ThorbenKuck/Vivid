# Features & Feature Details

## Overview
The **Features** section is the heart of Vivid. It provides a centralized view of all feature flags managed by the system. Users can create, search, and configure features across different environments from here.

## Feature Overview
The main Features page displays a table with all registered features.

### Key Features
*   **Running Number:** A unique sequential number for each feature.
*   **Feature Name & Tags:** The display name and descriptive tags for categorization.
*   **Environment Status Indicators:** A matrix showing the resolved status (Enabled/Disabled) for each environment.
*   **Search:** Quickly filter features by name or tags.
*   **Add Feature:** Create a new feature with a name and optional description.

### Permission Mapping

| Action | Visibility/Access | Required Role |
| :--- | :--- | :--- |
| View Feature List | Visible | `vivid:features:read` |
| Create New Feature | "Add" FAB visible | `vivid:features:write` |
| View Environment Status | Status icons visible | `vivid:env:[envId]:read` or `vivid:env:all:read` |

---

## Feature Details
Clicking **Open** on a feature brings you to the **Feature Details** view. This view is divided into several sections for granular configuration.

### A. General Configuration
*   **Master Switch:** Enables or disables the feature globally. If disabled here, it is disabled in all environments unless explicitly overridden (depending on the strategy).
*   **Name & Description:** Basic identification details.
*   **Tags:** Tags for grouping features.
*   **Notes:** A collaborative section where users can leave comments and history about the feature.
*   **Related Features:** Links to other features to show dependencies or relationships.

### B. Global Overrides & Metadata
*   **Global Flags:** Key-value pairs (toggles) that are passed to the SDKs.
*   **Global Metadata:** Additional custom data associated with the feature.
*   **Usage:** A tracking table showing which client applications are requesting this feature in which environments.

### C. Environment-Specific Overrides
The **Environment Overrides** section allows you to define behavior for specific environments.

*   **Strategy:**
    *   **EXTEND:** Inherits global settings and adds/modifies only specific flags/metadata.
    *   **OVERRIDE:** Completely ignores global settings for this environment and uses the specific configuration defined here.
*   **Environment Status:** Manually enable or disable the feature for a specific environment. This can be "Inherited" from global or "Overridden".
*   **Environment Flags & Metadata:** Define flags and metadata that only apply to this environment.

:::info
You can use the **Copy From** feature to quickly replicate the configuration of one environment to another.
:::

### Permission Mapping

| Action | Required Role |
| :--- | :--- |
| Edit Global Settings (Name, Master Switch, etc.) | `vivid:features:write` |
| Add/Delete Global Flags | `vivid:features:write` |
| Add/View Notes | `vivid:features:read` (View) / `vivid:features:write` (Add) |
| View Environment Overrides | `vivid:env:[envId]:read` (or `vivid:env:all:read`) |
| Edit Environment Overrides | `vivid:env:[envId]:write` (or `vivid:env:all:write`) |

For more details on roles, see [Security & Permissions](../security/permissions.md).
