# Task: Refactor Vivid Core Architecture & Implement Environment-Aware Metrics

## 1. Objective
Refactor **Vivid** to support global feature defaults with environment-specific overrides and introduce a technology-agnostic distribution system with environment-aware client tracking.

---

## 2. Backend Refactoring (Spring Boot)

### A. Feature & Environment Overrides
*   **Feature Entity:** Add `enabled` (boolean), `flags` (Map<String, Boolean>), and `metadata` (Map<String, MetadataValue>). These are the global defaults.
*   **EnvironmentOverride Entity:** Links `Feature` and `Environment`.
    *   Fields: `enabled` (nullable Boolean), `flags` (Map), `metadata` (Map).
    *   Strategy: `OverrideStrategy` (ENUM: `OVERRIDE`, `EXTEND`).
*   **Resolution Logic:** Implement a resolver that merges defaults with overrides. Use `JOIN FETCH` for performance. Access overrides via the `Feature` entity directly (no separate repository calls for every check).

### B. Technology-Agnostic Distribution
*   **Abstraction:** Create a `FeatureDistributionProvider` interface (e.g., for SSE, Kafka).
*   **Decoupling:** Use Spring `ApplicationEventPublisher`. When a feature is updated, fire a `FeatureChangedEvent`. All active `DistributionProviders` should listen and push updates.
*   **SSE:** Refactor existing `EnvironmentStream` into a provider implementation.

### C. Environment-Aware Client Registry
*   **Entity `VividClient`:** Must be bound to an `Environment`.
    *   Unique Identity: Combination of `clientName/id` and `environmentId`.
    *   Fields: `lastSeen`, `technologies` (List of strings like "SSE", "Kafka", "Polling"), `clientVersion`.
*   **Heartbeat Endpoint:** `POST /api/v1/clients/heartbeat`. SDKs must report their presence. The backend updates the `lastSeen` timestamp for that specific client-environment pair.

---

## 3. Frontend Refactoring (Angular)

### A. Global CSS & Design
*   **Constraint:** You **MUST** use the global CSS variables defined in `styles.scss` for all styling. Maintain the existing design language.

### B. UI Views
*   **Feature Configuration:**
    *   Top: Global Default settings.
    *   Tabs: Environment-specific overrides.
    *   **Visual Indicator:** Use a clear visual hint (e.g., icon or colored label) to show if a value is "Inherited", "Overridden", or "Extended".
*   **Usage Tab (within Feature):** List clients that have requested this feature, grouped by their environment.
*   **Clients Dashboard (New Page):** A comprehensive list of all registered clients.
    *   Group or filter by Environment.
    *   Show "Online/Offline" status based on `lastSeen`.
*   **Settings Page (New Page):** Display status of available `DistributionProviders` (e.g., SSE active, Kafka enabled).

---

## 4. Technical Quality
*   **Migrations:** You may drop and recreate tables (prototype stage).
*   **Efficiency:** Ensure the heartbeat endpoint is high-performance. Avoid N+1 queries by fetching relationships correctly.
*   **Immutability:** Use functional patterns for map/list merging in the "EXTEND" logic.

---

**Please start by providing a technical design document for the `EnvironmentOverride` resolution and the `VividClient` registry before implementing the code.**
**Respect the ".junie/guidelines.md" file**