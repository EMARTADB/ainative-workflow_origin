# Project Context — cbn-3-definition

## Architecture Patterns

```
src/main/java/.../petclinic/
  model/           ← domain entities (extend BaseEntity / NamedEntity)
  service/         ← ClinicServiceImpl (transactional facade — single service class)
  repository/
    jdbc/          ← plain JDBC impl
    jpa/           ← Hibernate/JPA impl
    springdatajpa/ ← Spring Data JPA impl
  web/             ← Spring MVC controllers
src/main/webapp/WEB-INF/jsp/  ← JSP views
src/main/resources/spring/    ← XML config files
```

## Design Conventions

- **Service layer**: One facade `ClinicService` / `ClinicServiceImpl`. New operations go here.
- **Repository pattern**: Every new data access method must be implemented in all three backends (`jdbc/`, `jpa/`, `springdatajpa/`) to maintain profile interchangeability.
- **Controllers**: Spring MVC `@Controller` classes in `web/`. Use `@GetMapping`/`@PostMapping`. Return view names as strings.
- **Views**: JSP files in `src/main/webapp/WEB-INF/jsp/`. Use JSTL and Spring form tags.
- **Spring XML config**: New beans go in the appropriate XML file:
  - Service/repo beans → `business-config.xml`
  - MVC beans → `mvc-core-config.xml`
  - DataSource → `datasource-config.xml`

## Naming Rules

- Test classes: must end in `Tests` (plural) — e.g., `OwnerControllerTests`, NOT `OwnerControllerTest`
- Repository interfaces: `<Entity>Repository` (e.g., `OwnerRepository`)
- Controllers: `<Entity>Controller` (e.g., `OwnerController`)
- JSP views: kebab-case in subdirectory matching entity (e.g., `owners/ownerDetails.jsp`)

## Jackson Dependency Coordinates

If Jackson is needed: use groupId `tools.jackson` (NOT `com.fasterxml.jackson`) — Jackson 3.x changed coordinates.

## Spec Design Guidance

- Tasks must be implementable independently and in order
- Each task should touch one layer at a time (model → repository → service → controller → view)
- Include a task for each of the three repository backends if data access changes
- Include a test task for each new controller method or service method
