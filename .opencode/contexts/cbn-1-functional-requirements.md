# Project Context — cbn-1-functional-requirements

## Project Overview

Spring PetClinic implemented with **plain Spring Framework 7.x** (NOT Spring Boot). WAR packaging deployed on Jetty. This is a veterinary clinic management application.

## Domain Vocabulary

- **Owner** — pet owner with name, address, city, telephone
- **Pet** — belongs to an Owner; has name, birth date, type (cat, dog, etc.)
- **Visit** — a vet appointment for a Pet; has date and description
- **Vet** — veterinarian with specialties
- **Specialty** — a vet's area of expertise (e.g., radiology, surgery)

## Key Functional Areas

- Owner management (CRUD: find, add, edit owners)
- Pet management (add, edit pets per owner)
- Visit management (add visits per pet)
- Vet listing (read-only directory with specialties)

## Architecture Constraints Relevant to Functional Requirements

- Views are **JSP** (not Thymeleaf, not React) — UI changes must be JSP-based
- No REST API layer — all interactions are form-based MVC
- Spring profiles select the persistence backend (`jpa`, `jdbc`, `spring-data-jpa`) — functional behavior must be identical across all three
- No user authentication/authorization system exists currently

## Change Naming Convention

Use kebab-case derived from the functional intent, e.g.:
- `add-visit-notes` — adding a notes field to visits
- `filter-vets-by-specialty` — filtering the vet list
- `owner-phone-validation` — validating phone number format
