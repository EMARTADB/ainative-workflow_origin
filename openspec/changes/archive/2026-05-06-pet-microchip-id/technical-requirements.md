## Technical Requirements
**Session name:** 2026-05-06 15:17:21 - docs(cbn-2-technical-requirements): generate artifact technical-requirements.md for pet-microchip-id
**Session id:** ses_202907762ffe9oT9E9ERZSUDvv
**Kind of change:** small-change

---

### TR-001: Microchip ID input validation at both layers

The `microchipId` value must be validated against the pattern `^\d{15}$` (exactly 15 decimal digits) at two independent layers:

1. **MVC / controller layer** — via Spring's `@Pattern` (or equivalent JSR-380 Bean Validation annotation) on the `Pet` form-backing object, so that invalid input is rejected before reaching the service layer and a field-level error is returned to the form.
2. **Service / persistence layer** — any value that bypasses the UI (e.g., direct API call, future integration) must also be rejected before the database write is attempted.

Null values must pass validation at both layers (field is optional — FR-003).

**Category:** Security

**Constraint / Target:**
- Pattern: `^\d{15}$`
- Null allowed; empty string must be coerced to null before persistence
- Validation must not be bypassable by omitting the MVC layer

**Verification:**
- Unit tests on the `Pet` model / form object confirming `@Pattern` rejects non-15-digit and non-numeric strings
- Integration test confirming a POST with an invalid microchipId returns HTTP 200 with form errors (standard Spring MVC validation flow)

---

### TR-002: Database unique constraint and index on microchip_id

The `microchip_id` column must have a **unique constraint** and a **B-tree index** defined at the database level. This enforces FR-004 (system-wide uniqueness) independently of application logic and enables efficient exact-match lookups required by FR-006.

The constraint must allow multiple `NULL` values (pets without a chip) — this is the default SQL behaviour for `NULL` in unique constraints on all supported databases (H2, HSQLDB, MySQL, PostgreSQL).

**Category:** Scalability

**Constraint / Target:**
- `UNIQUE` constraint on `microchip_id` in the `pets` table
- `NULL` values exempt from uniqueness enforcement (standard SQL semantics)
- Index type: B-tree (default for all supported engines)

**Verification:**
- SQL init scripts include `UNIQUE` keyword on the column definition or a separate `CREATE UNIQUE INDEX` statement
- Integration test inserts two pets with the same non-null microchipId and asserts a `DataIntegrityViolationException` (or equivalent) is thrown

---

### TR-003: Microchip search response time

The exact-match search by microchipId on the Find Owners page (FR-006) must complete within **500 ms at p95** under normal operating conditions (single application instance, local or embedded database, dataset size typical of a small clinic — up to ~10 000 pet records).

This target is met by the unique index defined in TR-002, which reduces the lookup to a constant-time index scan.

**Category:** Performance

**Constraint / Target:**
- p95 ≤ 500 ms end-to-end (HTTP request → rendered response)
- Applies to the H2 (default), HSQLDB, MySQL, and PostgreSQL profiles

**Verification:**
- Manual smoke test during development: observe response time in browser dev tools or server logs
- No automated load-test harness is required for this small-change scope

---

### TR-004: Schema migration via plain SQL init scripts

The addition of the `microchip_id` column must be delivered by updating the existing plain SQL schema initialisation scripts (consistent with the project's current approach — no Flyway, no Liquibase). Each supported database has its own DDL script under `src/main/resources/db/`.

Scripts must be **idempotent-safe** within the same initialisation lifecycle (i.e., dropping and recreating the schema on startup is the existing behaviour; no `ALTER TABLE` migration is required).

**Category:** Maintainability

**Constraint / Target:**
- Column definition added to the `CREATE TABLE pets` statement in each DB-specific DDL file (H2, HSQLDB, MySQL, PostgreSQL)
- No new tooling dependency introduced
- Backward compatibility: existing seed data scripts (`populateDB.sql`) must either set `microchip_id = NULL` explicitly or rely on the column default

**Verification:**
- All four DB profiles start successfully with a clean schema (`./mvnw jetty:run-war` and the MySQL/PostgreSQL profiles)
- Seed data loads without error

---

### TR-005: Cross-backend consistency — JPA, JDBC, Spring Data JPA

The microchipId field and the microchip-search feature must function identically across all three persistence backend implementations: `jpa`, `jdbc`, and `spring-data-jpa`. Each backend's repository implementation must be updated to:

1. Persist and retrieve `microchipId`
2. Enforce or propagate uniqueness errors to the service layer
3. Support the exact-match search query by `microchipId`

**Category:** Portability / Extensibility

**Constraint / Target:**
- All three backends activated via Spring profile pass the same functional acceptance criteria (FR-001 through FR-006)
- No feature divergence between backends

**Verification:**
- The three service integration test classes (`ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, `ClinicServiceSpringDataJpaTests`) — which all extend `AbstractClinicServiceTests` — must include shared test cases for microchipId CRUD and uniqueness enforcement
- CI runs all three profiles (`./mvnw verify`)

---

### TR-006: Test coverage — controller, service, and repository layers

The change must include automated tests at three layers to meet the testability standard for a small-change:

| Layer | Required test type |
|---|---|
| MVC controller | `@WebMvcTest` (or `MockMvc`) — form submission, validation errors, search routing |
| Service | Integration test via `AbstractClinicServiceTests` — save, uniqueness violation, search |
| Repository | Covered by service integration tests that exercise each backend directly |

All new test classes must follow the project convention: file names ending in `Tests.java` (not `Test.java`) to be picked up by Surefire.

**Category:** Testability

**Constraint / Target:**
- At minimum: validation rejection (invalid format), null acceptance, duplicate rejection, and successful search covered by automated tests
- Test count: no fixed minimum, but all acceptance criteria in FR-001–FR-006 must have at least one corresponding test

**Verification:**
- `./mvnw test` passes with zero failures
- Test output confirms new test methods were executed

---

### TR-007: Graceful uniqueness-violation error handling

When the database unique constraint (TR-002) is violated — either because the application-layer check was bypassed or due to a race condition — the service layer must catch the resulting `DataIntegrityViolationException` and translate it into a meaningful validation error surfaced to the user (FR-004 acceptance criteria: "This microchip ID is already registered to another pet").

The raw database exception must not propagate to the HTTP response or be visible to the end user.

**Category:** Resilience / Recoverability

**Constraint / Target:**
- `DataIntegrityViolationException` on microchip_id uniqueness must be caught at service or controller level
- User sees a form-level error message, not a 500 error page
- No unhandled exception logged at ERROR level for this expected condition

**Verification:**
- Integration test that directly triggers a duplicate insert and verifies the form error (not a 5xx response) is returned

---

### TR-008: No logging of microchip IDs required (not sensitive)

Microchip IDs are not personal human data and do not require masking or special handling in logs. Standard application logging (INFO/DEBUG level) may include microchipId values when useful for diagnostics (e.g., duplicate-submission warnings).

**Category:** Compliance / Privacy

**Constraint / Target:**
- No masking obligation
- Follow existing logging conventions (SLF4J via the application's configured logger)

**Verification:**
- No specific verification test; this is a permissive constraint confirming the absence of a restriction
