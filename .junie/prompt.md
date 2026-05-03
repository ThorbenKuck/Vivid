# Task: Refactor Vivid to "Global Defaults with Environment Overrides"

## Context & Goal
Currently, **Vivid** manages feature flags and metadata strictly per environment. We want to pivot to a model where the **Feature** entity holds the "Global Truth" (Default values), and **Environments** can either **Override** or **Extend** those defaults.

This refactoring affects both the Backend (Spring Boot/JPA) and the Frontend (Angular). Since we are in the prototype phase, database migrations can discard old data.

---

## 1. Backend Refactoring (Domain & Logic)

### A. Feature Entity Updates
*   **New Fields:** Add `enabled` (boolean), `flags` (Map<String, Boolean>), and `metadata` (List<MetadataValue>). These represent the global default state.
*   **Relation:** Add a collection of `EnvironmentOverride` entities.
*   **Cascade:** Ensure `CascadeType.ALL` and `orphanRemoval = true` are set.

### B. EnvironmentOverride Entity (New)
Create a new entity/Value Object to handle specialization:
*   **Link:** Direct many-to-one relationship to `Feature` and `Environment`.
*   **Fields:** `enabled` (Boolean, nullable), `flags` (Map), `metadata` (List).
*   **Strategy:** Add an Enum `OverrideStrategy` with two values:
  1.  `OVERRIDE`: The environment values completely replace the global defaults.
  2.  `EXTEND`: The environment values are merged with global defaults (Maps are merged, Lists are appended).
*   **Lifecycle:** If a Feature or Environment is deleted, the corresponding `EnvironmentOverride` must be deleted (Cascade).

### C. Business Logic & Resolution
Implement a resolver (preferably within the `Feature` entity or a Domain Service):
*   Logic: `resolve(feature, environmentId?)`
*   If `environmentId` is null: Return global defaults.
*   If `environmentId` is provided:
  *   Check for an `EnvironmentOverride`.
  *   If found: Apply `OverrideStrategy` logic.
  *   If not found: Fall back to global defaults.
*   **Implementation Note:** Access overrides directly via the Feature entity (`feature.getEnvironmentOverrides().find(...)`) rather than creating a separate `EnvironmentOverrideRepository`. Use `JOIN FETCH` where necessary to avoid N+1 issues.

### D. Cleanup

We will no longer need to maintain the `FeatureEnvironment` entity. This will be replaced by the `EnvironmentOverride` entity, which will encapsulate all environment-specific overrides.
*   Cleanup the entities and remove the `FeatureEnvironment` entity.
*   In the `ClientFeatureController`, map the `ClientFeatureDto` based on the optional environment overrides using the same logic as in our Business Logic.

---

## 2. Frontend Refactoring (UI/UX)

### A. Layout Restructuring
*   **Global Configuration:** Move the creation and editing of Flags and Metadata definitions to a "Global" header section above the Environment tabs.
*   **Environment Tabs:** The tabs should now only display the specialized state for that specific environment.
*   **Override UI:** In the Environment tab, provide a way to "Add Override" for specific flags or metadata.

### B. Visual Feedback (Crucial)
*   **Indicator:** Users must see at a glance if a value is "Inherited" from Global or "Overridden/Extended" locally.
*   Use subtle UI hints (e.g., a dot, a color shift, or a "Modified" label) to highlight overridden fields.
*   Make sure to also use a consistent visual hierarchy and spacing for readability.
*   In the Environment tab, provide a clear visual indicator if the feature is active here and if anything has been overwritten.

### C. Styling Constraints
*   **Global CSS:** You **MUST** use the existing global CSS variables defined in `styles.scss` for colors, spacing, and sizing.
*   **Consistency:** Maintain unified component sizing and the existing design language of Vivid.

---

## 3. Technical Requirements
*   **DB Migration:** Update the schema. You are allowed to drop/recreate tables as we are not live yet.
*   **Clean Code:** Use "Tell, Don't Ask" principles. Keep the resolution logic encapsulated.
*   **Frontend Logic:** Handle the "Extend" vs "Override" logic in the UI to show the user a "Preview" of the resulting state.

---

**Please start by analyzing the current `Feature` and `Environment` entities.**
**Respect the ".junie/guidelines.md" file**