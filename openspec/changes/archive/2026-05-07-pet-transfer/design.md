## Design
**Session name:** 2026-05-06 19:37:18 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-transfer
**Session id:** ses_201a27cdfffeqS0McnCMut5SMb
**Background and current state:** The PetClinic application uses plain Spring Framework 7.x with XML configuration (no Spring Boot auto-configuration). Persistence is handled through three interchangeable profiles (`jpa`, `jdbc`, `spring-data-jpa`), each providing its own repository implementations wired via `business-config.xml`. The `Pet` entity holds a `owner_id` FK to the `Owner` entity. There is currently no mechanism to change `pet.owner_id` after initial assignment, and no audit trail for ownership changes.

## Goals / Non-Goals

**Goals:**
- Provide an atomic, audited pet ownership transfer operation accessible to clinic staff from the pet detail page.
- Support all three persistence profiles without feature divergence.
- Enforce service-layer validation (self-transfer, pending visits) that cannot be bypassed via direct HTTP calls.
- Maintain a permanent, immutable audit log (`pet_transfers` table) for every completed transfer.
- Keep the implementation within the existing Spring MVC + JSP + XML-config architecture.

**Non-Goals:**
- Inline creation of new owners during the transfer flow.
- Reversal or undo of a completed transfer.
- Owner notifications (email, in-app).
- Role-based access control beyond "authenticated staff".
- Billing or invoice reassignment.
- Pagination or advanced filtering on the owner search.

## Decisions

### D-001: Transfer Logic in `ClinicServiceImpl`, not in the Controller

**Decision:** All business logic (validation, owner update, audit insertion) lives in `ClinicServiceImpl.transferPet(petId, newOwnerId, performedBy)`. The controller is thin.

**Rationale:** Keeps business rules testable in isolation, enforces TR-005 (service-layer validation cannot be bypassed), and aligns with the existing service-layer pattern in the codebase.

**Alternatives considered:**
- Validation in the controller only — rejected because it can be bypassed with direct HTTP calls.

---

### D-002: Single `@Transactional` Method Wrapping Both Writes

**Decision:** The `transferPet(...)` service method is annotated `@Transactional`. It updates `pet.owner_id` and inserts a `PetTransfer` audit record in one transaction. If either write fails, both are rolled back.

**Rationale:** Satisfies TR-004 (atomic transfer). Consistent with how other write operations in `ClinicServiceImpl` are managed.

**Alternatives considered:**
- Separate transactions — rejected; would allow partial commits leaving the DB inconsistent.

---

### D-003: New `PetTransferController` (Separate from `PetController`)

**Decision:** Add a dedicated `PetTransferController` for routes `/pets/{petId}/transfer` (GET — form) and `/pets/{petId}/transfer` (POST — commit).

**Rationale:** Keeps `PetController` focused on CRUD for pets. Transfer is a distinct workflow with its own validation and confirmation step.

**Alternatives considered:**
- Adding handler methods to `PetController` — acceptable but would increase controller size; separate controller is cleaner and easier to test.

---

### D-004: Owner Search via Existing `findOwners` Service Method

**Decision:** The transfer form's owner search reuses `ClinicService.findOwnerByLastName(query)` (or equivalent existing search). No new search endpoint is introduced.

**Rationale:** Avoids duplicating search logic; the existing index on `owners.last_name` already satisfies TR-001's < 500 ms target.

**Alternatives considered:**
- Dedicated `/api/owners/search` JSON endpoint — rejected; adds REST complexity not needed for a JSP-rendered form in this version.

---

### D-005: `performed_by` Stored as String (Staff Username), Not a FK

**Decision:** `pet_transfers.performed_by` stores the authenticated staff's username as a `VARCHAR`, not a FK to a users table.

**Rationale:** The application has no formal user management entity. TR-010 explicitly calls for a string identifier to decouple from a future user management feature.

---

### D-006: Three Separate Repository Implementations

**Decision:** Define a `PetTransferRepository` interface in `org.springframework.samples.petclinic.repository` and provide three concrete implementations under `repository/jpa/`, `repository/jdbc/`, and `repository/springdatajpa/`. Wire via Spring XML profiles in `business-config.xml`.

**Rationale:** Required by TR-006 for multi-backend portability. Matches the pattern used by all other repositories in the project.

---

### D-007: DDL Scripts for All Supported Dialects

**Decision:** Add `pet_transfers` table DDL to the existing schema scripts for H2 (test), MySQL, and PostgreSQL. FK constraints use RESTRICT on delete (no cascade) per TR-010.

**Rationale:** Prevents orphaned audit records if an owner or pet is ever deleted. Consistent with existing FK strategy in the schema.

---

### D-008: Confirmation Page as a Separate JSP View

**Decision:** The transfer POST route renders a confirmation JSP (`transferConfirm.jsp`) that displays pet name, current owner, and new owner. Staff submit a second POST to actually commit.

**Rationale:** Satisfies FR-003 (explicit confirmation before commit). Prevents accidental one-click transfers.

**Alternatives considered:**
- JavaScript confirmation dialog — rejected; inconsistent with existing JSP-only UI pattern and harder to test.

## Risks / Trade-offs

- **Stale confirmation data**: If another staff member changes the pet or owner record between the confirmation display and the commit POST, the service will re-validate at commit time, but the confirmation page may show stale info. → Mitigation: Re-read pet and owner state inside `transferPet(...)` before writing; display errors if state has changed.
- **Three repository implementations**: Maintaining three parallel implementations increases surface area for bugs. → Mitigation: A shared abstract integration test base (`AbstractPetTransferRepositoryTests` or extending `AbstractClinicServiceTests`) ensures all three implementations pass the same scenarios.
- **No pagination on owner search**: With very large owner lists a broad search could return many results. → Accepted for this version; search is bounded by the existing `findOwnerByLastName` query and the clinic scale assumed in TR-001 (up to 10,000 owners).
- **`performed_by` as a free string**: No referential integrity for staff identity. → Accepted per TR-010; decouples from a future user management feature.
