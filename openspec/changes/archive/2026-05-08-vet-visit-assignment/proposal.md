## Proposal
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->

## Why

The clinic has no way to record which veterinarian performed a visit; the `Visit` entity has no reference to a `Vet`, so assignment happens outside the application and cannot be audited or reported. Adding a vet-visit relationship gives clinic staff full traceability without leaving the system.

## What Changes

- Add a `ManyToOne` nullable relationship from `Visit` to `Vet` (entity + mapping).
- Add `vet_id INT NULL REFERENCES vets(id)` column to the `visits` schema across all DB init scripts (H2, HSQLDB, MySQL, PostgreSQL).
- Expose `ClinicService.findVets()` (or reuse existing) and populate a "Veterinarian" dropdown on the visit create/edit form.
- Display the assigned vet's full name in the visit detail (read-only) view.
- Update all three repository implementations (JDBC, JPA, Spring Data JPA) to persist and retrieve the `vet` field.
- Extend `AbstractClinicServiceTests` with vet-assignment integration tests; extend `VisitControllerTests` with unit tests for dropdown population and vet persistence.
- Ensure JSON serialization includes `vetId` scalar field without lazy-loading the full `Vet` graph.

## Capabilities

### New Capabilities

- `vet-visit-assignment`
   - Requirement(s): FR-001, FR-002, FR-003, FR-004, FR-005, TR-001, TR-002, TR-003, TR-004, TR-005, TR-006, TR-007, TR-008, TR-009
   - Full lifecycle for assigning a veterinarian to a visit: schema migration, domain model change, service/repository updates across all three implementations, form UI (dropdown + display), and test coverage.

### Modified Capabilities

<!-- No existing spec-level capabilities are changing. -->

## Impact

- **Domain model**: `Visit.java` gains a `@ManyToOne(optional=true)` `Vet vet` field.
- **Schema**: `visits` DDL files updated in `src/main/resources/db/` for H2, HSQLDB, MySQL, and PostgreSQL.
- **Repository layer**: `JdbcVisitRepositoryImpl`, `JpaVisitRepositoryImpl`, and `SpringDataVisitRepositoryImpl` (or equivalents) updated to map and persist `vet_id`.
- **Service layer**: `ClinicService` interface and `ClinicServiceImpl` expose `findVets()` (may already exist; reuse if so).
- **Controller**: `VisitController` populates model with vet list on `initNewVisitForm` / `initUpdateVisitForm`; binds `vet` on form submission.
- **Views**: Visit create/edit JSP gains a `<select>` for vet; visit detail JSP gains a read-only vet name field.
- **Tests**: `AbstractClinicServiceTests`, `VisitControllerTests` extended; no new concrete test classes.
- **JSON**: `Visit` serialization includes `vetId` field; Jackson 3.x (`tools.jackson`) lazy-loading risk mitigated.
- **No breaking API changes**; `vet_id` is nullable so no existing data is invalidated.
