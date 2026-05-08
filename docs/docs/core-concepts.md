# Core Concepts

Understanding Vivid's architecture and logic is key to mastering feature management. This section covers the core building blocks of the platform.

## Environment Management

Environments are the foundational context for your feature flags. They represent the stages of your software development lifecycle (e.g., `Development`, `Staging`, `Production`).

- **Isolation:** Feature flag states are completely isolated between environments.
- **Controlled Rollout:** Enable a feature in `Development` for testing before promoting it to `Staging` and finally `Production`.
- **Environment-Specific Config:** Metadata and flags can vary between environments for the same feature.

## Feature Flags

A **Feature** in Vivid is the main container for a specific capability you want to control. Each feature can have multiple flags and associated states.

### Primary Flag vs. Sub-flags

- **Primary Flag:** Every feature has a primary toggle that defines whether it's globally enabled or disabled within an environment.
- **Sub-flags:** Use additional flags for more granular control. For example, a `premium-ui` feature might have sub-flags like `dark-mode-available` or `advanced-analytics`.

### State Management

Vivid tracks the state of each flag per environment:
- **Enabled:** The flag is active.
- **Disabled:** The flag is inactive.
- **Locked:** Prevent changes to the flag state in critical environments (e.g., production) until a condition is met.

## Flag Metadata

Metadata allows you to attach custom properties to a feature flag. This is powerful for controlling application behavior dynamically without code changes.

- **Dynamic Config:** Store values like `retry_count`, `max_timeout`, or `api_endpoint_version`.
- **State Metadata:** Control *how* a feature behaves. For instance, a `maintenance_mode` feature could use metadata to specify the maintenance window or custom message.
- **Targeting Metadata:** Provide context for SDK-level decisions, such as which user IDs are part of a beta test group.

:::tip
Think of metadata as a dynamic configuration layer that sits on top of your feature toggles.
:::

![Core Concepts Visual](/img/concepts_visual.png)
*Placeholder for core concepts diagram*
