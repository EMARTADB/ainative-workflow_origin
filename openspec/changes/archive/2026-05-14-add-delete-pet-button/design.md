## Design
**Session name:** docs(cbn-3-definition): generate artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->
**Background and current state:** The PetClinic application is a plain Spring Framework 7.x MVC application (WAR packaging, no Spring Boot auto-configuration). Pet records can be created and edited but cannot be deleted through the UI. The `ClinicService` facade and all three repository backends (`jdbc`, `jpa`, `springdatajpa`) must be extended to support deletion. The `Pet` entity has a one-to-many relationship with `Visit` records.

## Goals / Non-Goals

**Goals:**
- Add a `deletePet` operation to the service layer and all three repository backends
- Expose a `@PostMapping` delete endpoint in `PetController`
- Render a delete form/button in `ownerDetails.jsp` with CSRF protection
- Ensure cascade deletion of visits is atomic and works across all persistence profiles
- Provide controller and service integration test coverage

**Non-Goals:**
- No REST API endpoint (no `DELETE /pets/{id}`)
- No soft-delete, audit trail, or `deleted` flag
- No UI confirmation dialog (not required by functional requirements)
- No changes to the `Visit` entity or visit-related UI

## Decisions

### Decision 1: Delete endpoint lives in `PetController`
**Rationale**: Pet lifecycle operations (create, edit, delete) belong together. `PetController` already handles pet creation and editing. Adding delete here keeps cohesion and avoids scattering pet logic across controllers.
**Alternative considered**: Adding to `OwnerController` — rejected because it mixes owner and pet concerns.

### Decision 2: HTTP POST form submission (no JavaScript / AJAX)
**Rationale**: Consistent with the existing MVC pattern (all mutations use form POST). TR-007 explicitly prohibits GET-based deletion. No REST endpoint is required. CSRF protection is naturally handled by Spring Security's form token mechanism.
**Alternative considered**: AJAX DELETE call — rejected because it requires a REST endpoint (prohibited by TR-007) and deviates from the project's JSP/form pattern.

### Decision 3: Cascade deletion strategy per backend
- **JPA backend**: Rely on `CascadeType.ALL` + `orphanRemoval = true` on `Pet.visits`. `EntityManager.remove(pet)` will cascade to visits automatically.
- **JDBC backend**: Execute explicit `DELETE FROM visits WHERE pet_id = ?` before `DELETE FROM pets WHERE id = ?` within the same transaction.
- **Spring Data JPA backend**: Delegate to the JPA `PetRepository` (which inherits JPA cascade behavior) or call `petRepository.delete(pet)`.
**Rationale**: Each backend must handle cascade deletion natively to maintain profile interchangeability (TR-006). No cross-backend workarounds.

### Decision 4: `@Transactional` at the service layer
**Rationale**: TR-003 requires atomic deletion. The service layer (`ClinicServiceImpl`) is the transaction boundary for all write operations in this project. Annotating `deletePet` with `@Transactional` ensures both the pet and visit deletions participate in a single transaction regardless of backend.

### Decision 5: WARN/ERROR logging in the service layer
**Rationale**: TR-004 requires structured logging for error conditions. The service layer is the appropriate place (not the controller) because it owns the business logic and has visibility into database exceptions. The controller delegates to the service and handles the redirect.

### Decision 6: Redirect to owner profile after deletion
**Rationale**: FR-001 states the user remains on the owner's profile page after deletion. A `redirect:/owners/{ownerId}` response (Post/Redirect/Get pattern) prevents duplicate form submissions on browser refresh.

## Risks / Trade-offs

- **[Risk] JPA cascade not configured on `Pet.visits`** → Mitigation: Verify and update the `Pet` entity's `@OneToMany` annotation to include `cascade = CascadeType.ALL, orphanRemoval = true` as part of the implementation task.
- **[Risk] JDBC backend visit deletion order** → Mitigation: Always delete visits before the pet to respect foreign key constraints. Wrap both statements in the same `@Transactional` service call.
- **[Risk] Pet not found on delete** → Mitigation: Service emits WARN log and returns without error (or throws a typed exception caught by the controller). No 500 error should surface to the user for a missing pet ID.
- **[Risk] CSRF token misconfiguration** → Mitigation: Controller test explicitly verifies that a POST without a CSRF token returns 4xx and does not delete.

## Migration Plan

1. No schema migration required — deletion uses existing tables.
2. Deploy as a standard WAR replacement. No data migration needed.
3. Rollback: redeploy the previous WAR. No database state changes are irreversible (deleted records are gone, but no schema changes exist to undo).

## Open Questions

- None. All requirements are unambiguous and implementation approach is fully determined by existing project conventions.
