## Proposal
**Session name:** docs(cbn-3-definition): generate artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->

## Why

Clinic staff have no way to remove incorrectly entered or stale pet records through the UI, causing data accumulation with no cleanup mechanism. Adding a "Delete" button to the owner profile page resolves this gap and keeps the system data accurate.

## What Changes

- Add a "Delete" button next to each pet on the owner's profile page (`ownerDetails.jsp`)
- Add a `deletePet(Pet pet)` method to `ClinicService` interface and `ClinicServiceImpl` (transactional, with cascade deletion of visits)
- Implement `deletePet` in all three repository backends: `jdbc/`, `jpa/`, `springdatajpa/`
- Add a `@PostMapping` endpoint in `PetController` (or `OwnerController`) to handle the delete action
- Ensure cascade deletion of all visit records associated with the deleted pet
- Add controller tests (`PetControllerTests`) and service integration tests extending `AbstractClinicServiceTests`

## Capabilities

### New Capabilities

- `delete-pet`
   - Requirement(s): FR-001, FR-002, TR-001, TR-002, TR-003, TR-004, TR-005, TR-006, TR-007, TR-008
   - Allows clinic staff to permanently delete a pet and all its associated visit records from the owner profile page via an HTTP POST form submission

### Modified Capabilities

<!-- No existing spec-level capabilities are changing -->

## Impact

- **Views**: `src/main/webapp/WEB-INF/jsp/owners/ownerDetails.jsp` — add delete form/button per pet
- **Controller**: `src/main/java/.../web/PetController.java` — new `@PostMapping` for delete
- **Service**: `ClinicService` interface + `ClinicServiceImpl` — new `deletePet` method with `@Transactional`
- **Repositories**: All three backends (`jdbc/`, `jpa/`, `springdatajpa/`) — new `deletePet` method
- **Model**: JPA `Pet` entity — verify `CascadeType.ALL` / `orphanRemoval = true` on visits collection
- **Tests**: New `PetControllerTests.java` and additions to `AbstractClinicServiceTests`
- **No new dependencies** required
