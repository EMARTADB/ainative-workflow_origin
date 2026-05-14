# Project Context — cbn-2.5-requirements-human-review

## Domain Vocabulary (for review conversations)

- **Owner** — pet owner with name, address, city, telephone
- **Pet** — belongs to an Owner; has name, birth date, type
- **Visit** — vet appointment for a Pet; has date and description
- **Vet** — veterinarian with specialties
- **Specialty** — a vet's area of expertise

## Key Constraints to Verify During Review

When reviewing functional requirements, flag if they assume:
- A REST API (the app is form-based MVC only — no REST layer)
- Thymeleaf or React views (views are JSP only)
- User authentication (no auth system exists)
- Spring Boot features (this is plain Spring Framework 7.x with XML config)

When reviewing technical requirements, flag if they assume:
- `com.fasterxml.jackson` groupId (must be `tools.jackson` for Jackson 3.x)
- Test files named `*Test.java` (Surefire only picks up `*Tests.java`)
- `application.properties` (no such file — config is in Spring XML files)
- Auto-configuration (does not exist in this project)

## Prior Decisions (Architectural Invariants)

- Three persistence backends (`jpa`, `jdbc`, `spring-data-jpa`) must remain interchangeable — functional behavior must be identical across all three
- WAR packaging is required — no embedded server in the artifact
- Java 17 is the minimum — no Java 8/11 syntax constraints apply
