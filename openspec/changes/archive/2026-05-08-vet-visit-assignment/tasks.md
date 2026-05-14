## Tasks
### Definition phase
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->
-----------
### Apply/Construction phase
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->

## 1. Schema Migration

- [x] 1.1 Add `vet_id INTEGER NULL` column and FK constraint to `CREATE TABLE visits` in `src/main/resources/db/h2/schema.sql`
- [x] 1.2 Add `vet_id INTEGER NULL` column and FK constraint to `CREATE TABLE visits` in `src/main/resources/db/hsqldb/schema.sql`
- [x] 1.3 Add `vet_id INT NULL REFERENCES vets(id)` to `CREATE TABLE visits` in `src/main/resources/db/mysql/schema.sql`
- [x] 1.4 Add `vet_id INT NULL REFERENCES vets(id)` to `CREATE TABLE visits` in `src/main/resources/db/postgresql/schema.sql`

## 2. Domain Model

- [x] 2.1 Add `@ManyToOne(optional=true, fetch=FetchType.LAZY) @JoinColumn(name="vet_id") private Vet vet;` field to `Visit.java`
- [x] 2.2 Add `getVet()` and `setVet(Vet vet)` accessors to `Visit.java`
- [x] 2.3 Add `@JsonIgnore` on the `vet` field to suppress full Vet embedding in JSON (TR-008)
- [x] 2.4 Add `@JsonProperty("vetId") public Integer getVetId()` helper method returning `vet != null ? vet.getId() : null`

## 3. JDBC Repository

- [x] 3.1 Update `JdbcVisitRepositoryImpl.createVisitParameterSource()` to include `vet_id` (null-safe: `visit.getVet() != null ? visit.getVet().getId() : null`)
- [x] 3.2 Update `JdbcVisitRepositoryImpl.findByPetId()` query to `LEFT JOIN vets v ON visits.vet_id = v.id` and select `visits.vet_id`
- [x] 3.3 Update `JdbcVisitRowMapper` to read `vet_id` and, if non-null, construct a minimal `Vet` stub (id only) and call `visit.setVet(vet)`

## 4. JPA Repository

- [x] 4.1 Verify `JpaVisitRepositoryImpl` uses the `Visit` JPA entity directly — no additional mapping change expected; run a quick smoke test to confirm `vet_id` is persisted and retrieved correctly after the entity change in step 2

## 5. Spring Data JPA Repository

- [x] 5.1 Verify `SpringDataVisitRepository` (Spring Data interface) requires no code change — inherits entity mapping from step 2; run a quick smoke test

## 6. Service Layer

- [x] 6.1 Confirm `ClinicService.findVets()` and `ClinicServiceImpl.findVets()` are present and return `Collection<Vet>` — no new method needed; document if already wired

## 7. Controller

- [x] 7.1 In `VisitController.initNewVisitForm()`, add `model.put("vets", this.clinicService.findVets())` to populate the dropdown
- [x] 7.2 Add `@ModelAttribute("vets")` method (or inline `model.put`) to keep `vets` available on POST validation failure (re-render form with errors)
- [x] 7.3 Ensure `WebDataBinder` does NOT disallow the `vet` field; update `setAllowedFields` if necessary

## 8. Views (JSP)

- [x] 8.1 Add a `<select>` labelled "Veterinarian" to `src/main/webapp/WEB-INF/jsp/pets/createOrUpdateVisitForm.jsp`, bound to `visit.vet`, with a blank first option and one option per vet (`${vet.firstName} ${vet.lastName}`)
- [x] 8.2 Display vet full name in the visit list/detail view (`visitList.jsp` or equivalent); show blank when `visit.vet` is null

## 9. Unit Tests — Controller

- [x] 9.1 Add `testInitNewVisitForm_populatesVetList`: mock `clinicService.findVets()` returns a list; assert model contains non-null, non-empty `vets`
- [x] 9.2 Add `testProcessNewVisitForm_withVet`: mock vet, set on visit; assert `clinicService.saveVisit()` called once and visit has vet set
- [x] 9.3 Add `testProcessNewVisitForm_withoutVet`: submit without vet; assert visit accepted (optional field, no validation error) and saved with `vet = null`
- [x] 9.4 Add `testInitNewVisitForm_vetListFetchFailure`: mock `clinicService.findVets()` throws `DataAccessException`; assert exception propagates (not caught by controller)

## 10. Integration Tests — Service / Repository

- [x] 10.1 Add `saveVisit_withVetAssignment()` to `AbstractClinicServiceTests`: save a `Visit` with an existing vet, reload via `findVisitsByPetId`, assert `visit.getVet() != null` and ID matches
- [x] 10.2 Add `saveVisit_withoutVetAssignment()` to `AbstractClinicServiceTests`: save a `Visit` with `vet = null`, reload, assert `visit.getVet() == null`
- [x] 10.3 Run `./mvnw test -Dspring.profiles.active=jdbc` — confirm new tests pass for JDBC profile
- [x] 10.4 Run `./mvnw test -Dspring.profiles.active=jpa` — confirm new tests pass for JPA profile
- [x] 10.5 Run `./mvnw test -Dspring.profiles.active=spring-data-jpa` — confirm new tests pass for Spring Data JPA profile

## 11. Full Build Verification

- [x] 11.1 Run `./mvnw verify` (default H2/JPA profile) — 0 failures
- [x] 11.2 Run `./mvnw verify -P HSQLDB` — 0 failures
- [x] 11.3 Manual smoke test: start `./mvnw jetty:run-war`, create a visit with vet selected, verify vet name appears in visit list/detail
- [x] 11.4 Manual smoke test: create a visit without vet, verify blank vet field renders without error

## 12. Known Limitation — JDBC Edit Path (FR-004 partial)

- [x] 12.1 Document that `JdbcVisitRepositoryImpl.save()` currently throws `UnsupportedOperationException` for updates; FR-004 (editing an existing visit's vet assignment) is fully supported only on JPA and Spring Data JPA profiles in this iteration
- [ ] 12.2 (Optional follow-on) Implement JDBC update path in `JdbcVisitRepositoryImpl` to fully satisfy FR-004 on the JDBC profile

## 13. Deferred — FR-004 Visit Edit Endpoint (out-of-scope for this iteration)

> **Decision (F-002 rework):** FR-004 requires editing an existing visit's vet assignment. This needs:
> 1. A `GET /owners/{ownerId}/pets/{petId}/visits/{visitId}/edit` endpoint (`initUpdateVisitForm`)
> 2. A `POST /owners/{ownerId}/pets/{petId}/visits/{visitId}/edit` endpoint (`processUpdateVisitForm`)
> 3. JDBC update path in `JdbcVisitRepositoryImpl` (currently throws `UnsupportedOperationException`)
>
> These are non-trivial additions beyond the scope of this change. FR-004 is **formally deferred** to a follow-on change.
> JPA and Spring Data JPA profiles already support update via Hibernate/Spring Data — only the JDBC profile is missing the update path.

- [ ] 13.1 (Follow-on) Add `initUpdateVisitForm` GET endpoint to `VisitController`
- [ ] 13.2 (Follow-on) Add `processUpdateVisitForm` POST endpoint to `VisitController`
- [ ] 13.3 (Follow-on) Implement JDBC update path in `JdbcVisitRepositoryImpl.save()` for existing visits
