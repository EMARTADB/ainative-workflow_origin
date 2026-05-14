# Project Context — cbn-4-construction

## Build Commands

```bash
# Build + run all tests (CI equivalent)
./mvnw verify

# Build without tests
./mvnw verify -DskipTests

# Run locally (H2 in-memory, jpa profile by default)
./mvnw jetty:run-war

# Run with a different DB
./mvnw jetty:run-war -P MySQL

# Run with a different persistence impl
./mvnw jetty:run-war -Dspring.profiles.active=jdbc

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=OwnerControllerTests

# Run a single test method
./mvnw test -Dtest=OwnerControllerTests#testMethodName

# Compile SCSS → CSS (only when changing styles)
./mvnw generate-resources -P css
```

## Architecture

```
src/main/java/.../petclinic/
  model/           ← domain entities
  service/         ← ClinicServiceImpl (transactional facade)
  repository/
    jdbc/          ← plain JDBC impl
    jpa/           ← Hibernate/JPA impl
    springdatajpa/ ← Spring Data JPA impl
  web/             ← Spring MVC controllers
src/main/webapp/WEB-INF/jsp/  ← JSP views (NOT Thymeleaf)
src/main/resources/spring/    ← XML config files
```

## Critical Implementation Rules

- **NOT Spring Boot** — no `@SpringBootApplication`, no `application.properties`, no auto-configuration
- **Three repository backends** — any data access change must be implemented in all three: `jdbc/`, `jpa/`, `springdatajpa/`
- **XML config** — new beans registered in `src/main/resources/spring/business-config.xml` (service/repo) or `mvc-core-config.xml` (MVC)
- **Views are JSP** — use JSTL and Spring form tags, not Thymeleaf
- **Jackson 3.x** — use groupId `tools.jackson` (NOT `com.fasterxml.jackson`)
- **Java 17** — use modern Java syntax freely (records, text blocks, pattern matching, etc.)

## Test Quirks

- Surefire picks up only `**/*Tests.java` (with the `s`) — NEVER name a test file `*Test.java`
- Service integration tests extend `AbstractClinicServiceTests` — add new service test methods there
- Spring XML configs for tests are loaded from `src/test/java/` (also a `testResource` directory)

## Code Style

- `.editorconfig`: UTF-8, LF line endings, 4-space indent (Java + XML), trim trailing whitespace, final newline
- No linter (no Checkstyle, Spotless, PMD), no pre-commit hooks

## Config File Locations

- `src/main/resources/spring/business-config.xml` — service layer, repos, transactions, caching
- `src/main/resources/spring/datasource-config.xml` — DataSource (filtered by Maven profile)
- `src/main/resources/spring/mvc-core-config.xml` — MVC setup, component scan
- `src/main/resources/spring/mvc-view-config.xml` — view resolvers, content negotiation

## Knowledge Graph

This project has a graphify knowledge graph at `graphify-out/`. Before answering architecture questions, read `graphify-out/GRAPH_REPORT.md`. After modifying code files, run `graphify update .` to keep the graph current.
