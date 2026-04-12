# Implement a GitHub pages documentation for Vivid

## Background

You are looking at a project called "Vivid".
This project is designed as a feature control system.
It allows users to control and monitor features in different environments or departments.
The goal is to allow all users, not only technically savvy ones, to easily manage and understand feature states.
Also, it shares knowledge about what feature exists on which stage.

## Role
Act as a Senior Technical Writer and Developer Experience (DX) Engineer.
Your goal is to create a comprehensive, modern documentation suite for my project, modeled after high-quality industry examples like [Flipt](https://flipt.io/).

## Project Architecture & UI Requirements
The documentation must support a modern MDX-based framework (like Docusaurus, Nextra, or Starlight).
Please generate the structure and content for:

1.  **Landing Page (Home):**
  * A high-impact Hero section with a clear "Value Proposition."
  * Feature sections explaining the core utility of the project.
  * Header Navigation: `Home`, `Docs`, and `GitHub` (placeholder link).

2.  **Documentation (Docs):**
  * A structured **Sidebar Navigation** for developer-centric content.
  * Logical flow from high-level overview to technical implementation.

## Content Sections to Generate
Please provide the Markdown/MDX content for the following files:

### 1. `index.mdx` (The Landing Page)
* Clear headline and sub-headline.
* Call-to-Action (CTA) buttons: "Get Started" and "View on GitHub."
* Three-column feature highlights (e.g., Feature Management, Ease of Use, Metrics).

### 2. `docs/introduction.md`
* What is this project?
* What problem does it solve?
* High-level architecture overview.

### 3. `docs/getting-started.md`
* Installation steps (CLI/Package Manager).
* Quickstart guide to get the first feature setup.

### 4. `docs/core-concepts.md`
* Detailed explanation of the internal logic.
* Include sub-sections for:
  * **Environment Management:** How to create and manage environments, and what they are used for.
  * **Feature Flags:** How flags are stored in a feature. Highlight the primary flag and the sub-flags.
  * **Flag Metadata:** What metadata can be used for (specifying whena and how a feature is enabled, state management, controlling how the application behaves, etc.).

### 5. `docs/sdks.md`
* Explain the existing SDKs and how to use them.

### 6. `_sidebar.json` (or Navigation Config)
* Provide a JSON structure that defines the order of the sidebar links.

## Tone & Formatting
* **Tone:** Professional, developer-friendly, and concise.
* **Components:** Use code blocks (TS/JS), Admonitions/Callouts (:::note, :::info), and bold text for emphasis.
* **Visuals:** Use placeholders for diagrams or screenshots where appropriate.

---
**Note for Junie:** Ensure the documentation feels like a cohesive "Product" rather than just a collection of README files.