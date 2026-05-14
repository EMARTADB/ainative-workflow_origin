## Proposal
**Session name:** 2026-05-06 15:23:30 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-microchip-id
**Session id:** ses_2028ad7fbffePuOwT3812K1vNN

## Why

Clinic staff have no way to record or search by a pet's ISO microchip ID, forcing them to rely on paper records or out-of-system lookups when an animal is brought in without its owner. Adding microchip ID support directly in PetClinic makes reuniting lost pets with their owners faster and more reliable.

## What Changes

- Add an optional `microchipId` field (exactly 15 decimal digits, ISO 11784/11785) to the `Pet` entity, persisted as `microchip_id` in the `pets` table.
- Expose the field on the **Add Pet** and **Edit Pet** forms with client-side and server-side format validation.
- Display `microchipId` (read-only) on the **pet detail view** and **owner summary page**.
- Enforce system-wide uniqueness of `microchipId` at both the DB constraint level and the application layer, with a user-friendly duplicate error message.
- Add an exact-match **search by microchip ID** on the **Find Owners** page.
- Update all three persistence backends (JPA, JDBC, Spring Data JPA) and all four DB DDL scripts (H2, HSQLDB, MySQL, PostgreSQL).

## Capabilities

### New Capabilities

- `pet-microchip-id`
   - Requirement(s): FR-001, FR-002, FR-003, FR-004, FR-005, FR-006, TR-001, TR-002, TR-003, TR-004, TR-005, TR-006, TR-007, TR-008
   - Full lifecycle management of a pet's microchip ID: storage, validation, display, uniqueness enforcement, and exact-match search across all persistence backends.

### Modified Capabilities

<!-- No existing spec-level capabilities have requirement changes — this is a net-new capability. -->

## Impact

- **Model**: `Pet.java` — new `microchipId` field with `@Pattern` Bean Validation annotation.
- **Repository / DAO**: All three backend implementations updated for CRUD and microchip search query (`ClinicService`, `JpaOwnerRepository`, `JdbcPetRepositoryImpl`, `SpringDataJpaOwnerRepository`).
- **Service layer**: `ClinicServiceImpl` — new `findPetByMicrochipId` method; `DataIntegrityViolationException` handling for duplicate microchip on save.
- **Controllers**: `PetController` (add/edit forms), `OwnerController` (search by microchip on Find Owners page).
- **JSP views**: `createOrUpdatePetForm.jsp`, `ownerDetails.jsp`, `owners/ownersList.jsp` (or equivalent find-owners view).
- **DDL scripts**: `src/main/resources/db/{h2,hsqldb,mysql,postgres}/schema.sql` — `UNIQUE` column addition.
- **Tests**: New test methods in `AbstractClinicServiceTests`, `OwnerControllerTests`, and `PetControllerTests`; potentially a new `PetMicrochipValidationTests` class.
- **Dependencies**: No new Maven dependencies required.
