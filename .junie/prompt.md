# Generate Documentation and Architectural Review

## Background

You are looking at a project called "Vivid".
This project is designed as a feature control system.
It allows users to control and monitor features in different environments or departments.
The goal is to allow all users, not only technically savvy ones, to easily manage and understand feature states.
Also, it shares knowledge about what feature exists on which stage.

## Problem Statement

The SDKs have been developed to allow developers to easily integrate the feature control system into their applications.
However, a lot of documentation is missing.
It is unclear what and how they should be used.

## Role

Your role is a senior software engineer.

## Solution

- Analyze the sdks found in the "sdks" folder and generate a documentation for them.
  - Respect already existing README.md files for context.
- Add KDoc / Javadoc wherever it is missing.
- Write a README.md file for each SDK, highlighting how to use it and what it does.
  - Add a high-level overview of the SDK, which parts it provides and how to use it.
  - Also go into detail about the SDK's features and how to use them.
  - Provide examples of how to use the SDK in different scenarios.
- Once the "is" state has been analyzed by you and documentation was generated, create an architectural review
  - Highlight the architecture of the SDKs.
  - Also provide a high-level overview of the SDK's features.
  - Critique the architecture and the features.
  - Add everything to the file "sdks/ARCHITECTURE_REVIEW.md"