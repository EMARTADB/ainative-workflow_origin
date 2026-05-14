# Project Context — cbn-2-technical-requirements

## Technology Stack

- **Framework**: Spring Framework 7.x (plain XML config — NOT Spring Boot)
- **Java**: 17 minimum (enforced by `maven-enforcer-plugin`)
- **Build**: Maven via `./mvnw` wrapper
- **Packaging**: WAR deployed on Jetty (local dev) or any servlet container
- **Persistence**: Three interchangeable backends selected by Spring profile:
  - `jpa` (default) — Hibernate/JPA
  - `jdbc` — plain JDBC
  - `spring-data-jpa` — Spring Data JPA
- **Databases supported**: H2 (default, in-memory), HSQLDB, MySQL, PostgreSQL
- **Views**: JSP (not Thymeleaf, not any JS framework)
- **Jackson**: version 3.x using groupId `tools.jackson` (NOT `com.fasterxml.jackson`)

## Existing NFR Baselines

- **No authentication/authorization** — the application has no security layer currently
- **No REST API** — all interactions are synchronous form-based MVC
- **No pre-commit hooks, no linter** — code style enforced only by `.editorconfig`
- **CI**: `./mvnw -B verify` on Java 17 and 21 for PRs; SonarCloud on main branch

## Infrastructure Constraints

- Local dev runs on `http://localhost:8080` via `./mvnw jetty:run-war`
- No Docker, no Kubernetes, no cloud deployment defined in the project
- Database is H2 in-memory by default — data does not persist across restarts unless a file-based DB is configured

## Compliance / Privacy

- No PII handling beyond what the clinic domain requires (owner names, addresses, phone numbers)
- No GDPR or HIPAA controls currently implemented

## Testability Constraints

- Surefire picks up only `**/*Tests.java` (with the `s`) — test files named `*Test.java` are silently skipped
- Service integration tests extend `AbstractClinicServiceTests` — new service tests should follow this pattern
- Spring XML configs for tests are loaded from `src/test/java/` (also a `testResource` directory)
- Run all tests: `./mvnw test`
- Run single test class: `./mvnw test -Dtest=OwnerControllerTests`
