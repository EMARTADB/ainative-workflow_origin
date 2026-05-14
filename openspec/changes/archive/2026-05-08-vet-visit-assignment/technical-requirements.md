## Technical Requirements
**Session name:** 2026-05-06 18:17:50 - docs(cbn-2-technical-requirements): generate artifact technical-requirements.md for vet-visit-assignment
**Session id:** ses_201eb3905ffe3Z5rzBMQma680o
**Kind of change:** standard-change

---

### TR-001: Vet Dropdown Query Performance

The vet list used to populate the assignment dropdown must be retrieved via a single, unbounded SQL query against the `vets` table on every form load. No caching layer is required given the expected population is fewer than 50 veterinarians.

**Category:** Performance

**Constraint / Target:**
- The vet list query must complete in under 200 ms under normal load conditions.
- No application-level caching (e.g., Spring Cache) is required or introduced for this field.
- The query must use the existing `VetRepository.findAll()` method; no new repository method is needed.

**Verification:**
- Manual testing: measure form load time on a populated H2/PostgreSQL instance with ≤ 50 vet records.
- Confirm no N+1 queries are introduced (e.g., lazy-loading specialties for each vet in the dropdown).

---

### TR-002: Future Role-Based Access Hook

The vet-assignment field must not be guarded by a role check in this iteration, but the design must not prevent a future role-based access control layer from being applied at the controller or service level without structural rework.

**Category:** Security

**Constraint / Target:**
- No `@PreAuthorize` or role filter is added in this change.
- The `VisitController` must not embed hard-coded access decisions regarding the `vet` field.
- The controller method signature must remain compatible with Spring Security annotation injection if added later.

**Verification:**
- Code review confirms no inline access logic is embedded in the controller for the vet field.
- A future security layer can be applied by adding a single annotation or filter without modifying business logic.

---

### TR-003: Schema Migration Integrity

The `vet_id` column added to the `visits` table must be nullable and must not break existing data or trigger cascading errors on existing rows.

**Category:** Reliability

**Constraint / Target:**
- The DDL change is: `ALTER TABLE visits ADD COLUMN vet_id INT NULL REFERENCES vets(id)`.
- Existing rows default to `NULL`; no backfill or data migration is required.
- The migration must be idempotent where the schema init script is used (H2, HSQLDB) and must be a safe `ALTER TABLE` for persistent databases (MySQL, PostgreSQL).
- All three repository implementations (JDBC, JPA, Spring Data JPA) must handle `vet_id = NULL` without `NullPointerException` or mapping error.

**Verification:**
- Run `./mvnw verify` on all profiles (default H2, HSQLDB, MySQL, PostgreSQL) with existing seed data; no errors must occur.
- Verify that visits created before the migration are readable with `vet = null` and display a blank vet field in the UI.

---

### TR-004: Cross-Repository Consistency

The `vet_id` field must be saved and retrieved identically across the JDBC, JPA (Hibernate), and Spring Data JPA repository implementations.

**Category:** Reliability

**Constraint / Target:**
- All three implementations of `VisitRepository.save()` must persist `vet_id` when set and leave it `NULL` when not set.
- All three implementations of `VisitRepository.findByPetId()` (or equivalent) must return the `vet` reference correctly populated.
- No repository implementation may silently drop or ignore the `vet` field.

**Verification:**
- Integration tests in `AbstractClinicServiceTests` extended with a `saveVisit_withVet` and `saveVisit_withoutVet` scenario, executed against all three implementations via `ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, and `ClinicServiceSpringDataJpaTests`.

---

### TR-005: Vet List Failure Handling

If the `VetRepository.findAll()` call throws an exception during form rendering, the system must not silently swallow the error or render a broken form with an empty dropdown and no feedback.

**Category:** Resilience / Recoverability

**Constraint / Target:**
- An unhandled exception from the vet list query must propagate to the Spring MVC exception handler and return HTTP 500.
- The exception must be logged at `ERROR` level with a stack trace by the existing logging framework (SLF4J / Logback).
- No custom error page is required for this change; the existing error handling applies.

**Verification:**
- Unit test in `VisitControllerTests`: mock `ClinicService.findVets()` to throw `DataAccessException`; assert that the controller does not catch it and that the exception propagates.
- Confirm the error appears in the application log at `ERROR` level during the test.

---

### TR-006: Test Coverage — Unit and Integration

The new vet-assignment logic must be covered by both controller-level unit tests and service/repository-level integration tests.

**Category:** Testability

**Constraint / Target:**
- **Controller unit tests** (`VisitControllerTests`):
  - `testInitNewVisitForm_populatesVetList`: asserts the model contains a non-null vet list.
  - `testProcessNewVisitForm_withVet`: asserts the visit is saved with the selected vet.
  - `testProcessNewVisitForm_withoutVet`: asserts a visit with no vet is accepted (optional field).
- **Service/repository integration tests** (added to `AbstractClinicServiceTests`):
  - `saveVisit_withVetAssignment`: saves a visit with a vet and reloads; asserts `visit.getVet() != null` and ID matches.
  - `saveVisit_withoutVetAssignment`: saves a visit without a vet; asserts `visit.getVet() == null`.
- Tests must run against all three profiles (JDBC, JPA, Spring Data JPA) via the existing `ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, and `ClinicServiceSpringDataJpaTests` subclasses.

**Verification:**
- `./mvnw test` passes with 0 failures across all profiles.
- Surefire report shows the new test methods present and green.

---

### TR-007: Maintainability — Shared Test Base

All repository-layer test logic for vet-assignment must reside in `AbstractClinicServiceTests`; no duplication across the three concrete test subclasses is permitted.

**Category:** Maintainability

**Constraint / Target:**
- New test methods are added only to `AbstractClinicServiceTests`.
- `ClinicServiceJdbcTests`, `ClinicServiceJpaTests`, and `ClinicServiceSpringDataJpaTests` inherit the tests with no override or duplication.
- The same pattern applies to any new `ClinicService` method introduced (e.g., `findVets()` if not already present).

**Verification:**
- Code review: search for `saveVisit_withVet` in the concrete test classes; expect zero matches.
- All three concrete test classes pass `./mvnw test` without adding any new methods.

---

### TR-008: Interoperability — Visit JSON Representation

When a `Visit` resource is serialized to JSON (e.g., via content negotiation or a REST endpoint), the `vet_id` of the assigned veterinarian must be included as a scalar integer field. The full `Vet` object must not be embedded inline.

**Category:** Interoperability

**Constraint / Target:**
- The `Visit` JSON response includes `"vetId": <integer>` (or `"vet_id"`) when a vet is assigned, and `"vetId": null` when not assigned.
- No additional nested `vet` object (with specialties, etc.) is added to the Visit payload.
- Jackson 3.x (`tools.jackson` coordinates) serialization must not trigger lazy-loading of the `Vet` entity during JSON serialization.

**Verification:**
- Integration or manual test: request `GET /owners/{id}/pets/{petId}/visits` with `Accept: application/json`; assert the response includes `vetId` field.
- Confirm no `LazyInitializationException` occurs during serialization.

---

### TR-009: Portability — Support for Future Vet Filtering

The data model and service interface must not prevent future filtering of the vet dropdown by specialty or pet type without requiring a structural breaking change.

**Category:** Portability / Extensibility

**Constraint / Target:**
- The `vet_id` FK references `vets(id)` with no application-level specialty constraint enforced now.
- `ClinicService.findVets()` returns `Collection<Vet>`; the signature must remain stable so a filtered variant can be added as an overload without breaking existing callers.
- No hard-coded specialty filtering logic is introduced in this change.

**Verification:**
- Code review confirms `ClinicService` interface and `ClinicServiceImpl` expose `findVets()` with a stable, unfiltered signature.
- No specialty or pet-type filter is applied in the controller or repository for the dropdown population.
