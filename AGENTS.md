# AGENTS.md

## Key Facts

- **NOT Spring Boot** — plain Spring Framework 7.x with XML config (`src/main/resources/spring/*.xml`). No auto-configuration, no `application.properties`.
- **WAR packaging** requiring a servlet container. Local dev uses Jetty via Maven plugin.
- **Java 17 minimum** (enforced by `maven-enforcer-plugin`). Use the Maven wrapper (`./mvnw`).
- **Jackson 3.x** uses groupId `tools.jackson` (not `com.fasterxml.jackson`). Adding Jackson deps must use the new coordinates.

## Commands

```bash
# Build + test (CI equivalent)
./mvnw verify

# Run locally (H2 in-memory, jpa profile by default)
./mvnw jetty:run-war

# Run with a different DB (H2 is default; also HSQLDB, MySQL, PostgreSQL)
./mvnw jetty:run-war -P MySQL

# Run with a different persistence impl (jpa is default; also jdbc, spring-data-jpa)
./mvnw jetty:run-war -Dspring.profiles.active=jdbc

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=OwnerControllerTests

# Run a single test method
./mvnw test -Dtest=OwnerControllerTests#testMethodName

# Compile SCSS → CSS (output is committed; only needed when changing styles)
./mvnw generate-resources -P css
```

## Test Quirks

- Surefire only picks up `**/*Tests.java` (with the `s`). Files named `*Test.java` are silently skipped.
- Service integration tests (`ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, `ClinicServiceSpringDataJpaTests`) all extend `AbstractClinicServiceTests` — test logic lives in the parent.
- Spring XML configs for tests are loaded from `src/test/java/` (also a `testResource` directory), not just `src/test/resources/`.

## Architecture

```
src/main/java/.../petclinic/
  PetclinicInitializer.java   ← app entry point (replaces web.xml)
  model/                      ← domain entities
  service/                    ← ClinicServiceImpl (transactional facade)
  repository/
    jdbc/                     ← plain JDBC impl
    jpa/                      ← Hibernate/JPA impl
    springdatajpa/            ← Spring Data JPA impl
  web/                        ← Spring MVC controllers
src/main/webapp/WEB-INF/jsp/  ← JSP views (not Thymeleaf)
```

Spring profiles (`jpa`, `jdbc`, `spring-data-jpa`) select which repository impl is active at runtime.

## Config Files

- `src/main/resources/spring/business-config.xml` — service layer, repos, transactions, caching
- `src/main/resources/spring/datasource-config.xml` — DataSource (filtered by Maven profile)
- `src/main/resources/spring/mvc-core-config.xml` — MVC setup, component scan
- `src/main/resources/spring/mvc-view-config.xml` — view resolvers, content negotiation

## CI

- PRs: `./mvnw -B verify` on Java 17 and 21
- Main branch: `./mvnw -B verify` + SonarCloud scan (requires `SONAR_TOKEN` secret)
- OpenCode agent triggered by `/oc` or `/opencode` in issue/PR comments

## Style

- `.editorconfig`: UTF-8, LF, 4-space indent (Java + XML), trim trailing whitespace, final newline
- No linter (no Checkstyle, Spotless, PMD), no pre-commit hooks
