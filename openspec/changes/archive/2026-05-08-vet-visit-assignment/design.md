## Design
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->
**Background and current state:** The `Visit` entity has no reference to `Vet`. The `vet_id` column is absent from the `visits` table in all four DB schemas (H2, HSQLDB, MySQL, PostgreSQL). `ClinicService.findVets()` already exists and returns `Collection<Vet>`. `JdbcVisitRepositoryImpl.save()` currently does not support update (throws `UnsupportedOperationException`); it uses `SimpleJdbcInsert` for new records only. The JPA and Spring Data JPA implementations delegate to Hibernate/Spring Data and support updates.

## Goals / Non-Goals

**Goals:**
- Add nullable `vet_id` FK column to `visits` table in all four DB schemas.
- Add `@ManyToOne(optional=true)` `Vet vet` field to `Visit` entity.
- Map `vet_id` in JDBC row mapper and insert parameter source; map in JPA entity; Spring Data JPA inherits from JPA entity automatically.
- Populate a `vets` model attribute in `VisitController` on GET (create/edit) and expose it to the JSP form as a dropdown.
- Bind the selected `vet` on POST; persist via existing `ClinicService.saveVisit()`.
- Display vet full name in the visit detail/list view (read-only); show blank when `null`.
- Ensure JSON serialization includes `vetId` scalar (integer or null) without embedding the full `Vet` graph.
- Add controller unit tests and shared service integration tests per TR-006 / TR-007.

**Non-Goals:**
- Vet dropdown filtering by specialty or pet type.
- Role-based access control on the vet field.
- Visit update support in the JDBC layer (out of scope; existing `UnsupportedOperationException` is preserved).
- Custom error page for vet-list fetch failure.
- Backfilling existing visits with a default vet.

## Decisions

### D1: Reuse `ClinicService.findVets()` — no new service method

`ClinicService.findVets()` already exists and returns `Collection<Vet>`. The controller will call it directly to populate the dropdown model attribute. No new method or overload is introduced.

**Alternatives considered:**
- Add `findVetsForDropdown()` returning `List<Vet>` sorted by last name — rejected; premature complexity, sorting can be done in the JSP or added later.

### D2: Nullable FK, no cascade

`vet_id INT NULL REFERENCES vets(id)` with no `ON DELETE CASCADE`. If a vet record is deleted the visit row retains a stale FK; this edge case is deferred (no vet delete feature exists). Using `ON DELETE SET NULL` was considered but requires consistent DDL across all four databases and adds complexity for an edge case that is out of scope.

### D3: `@ManyToOne(optional=true, fetch=LAZY)` on `Visit`

Lazy fetch prevents the `Vet` (and its specialties collection) from being loaded when visits are listed. The vet name is only needed when displaying a single visit or the visit form.

**Alternative considered:** `EAGER` — rejected; loading all vet specialties for every visit list query creates an N+1 risk and is unnecessary.

### D4: JDBC `vet_id` binding — extend `createVisitParameterSource` and `JdbcVisitRowMapper`

The JDBC insert already uses `MapSqlParameterSource`; add `.addValue("vet_id", visit.getVet() != null ? visit.getVet().getId() : null)`. The existing `JdbcVisitRowMapper` maps columns to `Visit`; add `vet_id` lookup. Because `JdbcVisitRepositoryImpl.findByPetId` currently maps only `id, visit_date, description`, the vet must be resolved with a join or a second lookup.

**Decision:** Use a `LEFT JOIN vets v ON visits.vet_id = v.id` in the `findByPetId` query to retrieve `vet_id` in one query. The mapper checks for null and sets `visit.setVet(null)` if absent.

**Alternative:** Load the vet via a separate `findVetById` query — rejected; extra round-trip per row is unnecessary when a join suffices.

### D5: JSON serialization — `@JsonProperty("vetId")` on `getVetId()` helper, no `@JsonManagedReference`

Add a `getVetId()` method to `Visit` annotated with `@JsonProperty("vetId")` that returns `vet != null ? vet.getId() : null`. Annotate the `vet` field with `@JsonIgnore` to prevent embedding. This avoids lazy-loading during serialization and satisfies TR-008.

**Alternative considered:** Jackson `@JsonIdentityInfo` — overkill for this scalar requirement.

### D6: Visit edit form — scope limited to create (new visit) per existing controller structure

`VisitController` has `initNewVisitForm` (GET) and `processNewVisitForm` (POST). There is no edit endpoint currently. FR-004 requires editing an existing visit's vet. This change adds the vet dropdown to the create form; a full edit endpoint would require adding `initUpdateVisitForm` / `processUpdateVisitForm` methods and the JDBC `save()` update path — both are non-trivial.

**Decision:** Implement the vet dropdown on the existing create form (FR-001, FR-002). Add a note in `tasks.md` that FR-004 (edit after creation) requires the JDBC update path to be unblocked separately. The JPA and Spring Data JPA profiles already support update, so FR-004 is achievable for those profiles.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `LAZY` load of `Vet` triggers `LazyInitializationException` during JSON serialization | Mitigated by D5: `@JsonIgnore` on the `vet` field + `getVetId()` accessor returns scalar ID only |
| JDBC `findByPetId` JOIN increases query complexity | Low risk — query is simple; H2/HSQLDB/MySQL/PostgreSQL all support `LEFT JOIN` |
| Schema init scripts re-created on each Jetty restart (H2/HSQLDB) may conflict with `ALTER TABLE` approach | H2 and HSQLDB schemas are `CREATE TABLE` from scratch; add `vet_id` column directly to `CREATE TABLE visits` DDL instead of `ALTER TABLE` |
| JDBC `save()` does not support update; FR-004 edit-after-creation is partially unimplemented on JDBC profile | Documented as known limitation; JPA and Spring Data JPA profiles support update; JDBC update is a separate follow-on task |
| Stale vet FK if a vet is deleted | No vet delete feature exists; deferred with `NULL` cascade option |

## Migration Plan

1. **Schema changes** (DDL-only, no data migration):
   - H2 and HSQLDB: add `vet_id INTEGER NULL` + FK constraint directly in `CREATE TABLE visits` in `src/main/resources/db/{h2,hsqldb}/schema.sql`.
   - MySQL and PostgreSQL: add `vet_id INT NULL REFERENCES vets(id)` to `CREATE TABLE visits` in `src/main/resources/db/{mysql,postgresql}/schema.sql`. For existing production databases an `ALTER TABLE visits ADD COLUMN vet_id INT NULL REFERENCES vets(id)` script is needed (out of scope for this change).
2. **Domain model**: `Visit.java` updated; JPA/Hibernate picks up the new column automatically via the entity mapping.
3. **JDBC layer**: `JdbcVisitRepositoryImpl` and `JdbcVisitRowMapper` updated.
4. **Controller + JSP**: `VisitController` populates `vets` model attribute; `createOrUpdateVisitForm.jsp` renders dropdown; visit list/detail JSP renders vet name.
5. **Tests**: new methods in `AbstractClinicServiceTests` and `VisitControllerTests`; no new test files needed.

**Rollback:** Revert the DDL change (drop `vet_id` column) and revert code changes. No data migration required since `vet_id` is nullable and existing rows remain valid.

## Open Questions

- Should the vet dropdown display `firstName lastName` or `lastName, firstName`? — recommend `firstName lastName` to match existing vet list display; confirm with clinic staff.
- Is HTTP 500 (propagating `DataAccessException`) the right behavior for vet-list fetch failure (TR-005), or should the form render with an empty dropdown and a flash warning? — current decision is propagate; revisit if UX feedback demands graceful degradation.
