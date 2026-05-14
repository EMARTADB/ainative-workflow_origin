## Proposal
**Session name:** 2026-05-06 19:37:18 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-transfer
**Session id:** ses_201a27cdfffeqS0McnCMut5SMb

## Why

Pets are regularly rehomed or adopted in real clinic practice, but the system provides no mechanism to reassign a pet to a different owner. This gap forces staff to use workarounds and leaves ownership records inaccurate, undermining the reliability of medical history tracking.

## What Changes

- **New**: "Transfer Ownership" action added to the pet detail page.
- **New**: Owner search UI within the transfer form (search by name or ID against existing registered owners).
- **New**: Confirmation step displaying pet name, current owner, and new owner before committing.
- **New**: `pet.owner_id` updated atomically alongside a new `PetTransfer` audit record on commit.
- **New**: `pet_transfers` database table with FK references to `pets` and `owners`.
- **New**: `PetTransferRepository` interface with implementations for all three persistence profiles (`jpa`, `jdbc`, `spring-data-jpa`).
- **New**: Service-layer validation blocking self-transfer and transfer when pending visits exist.
- **New**: Structured SLF4J logging for transfer outcomes (INFO/WARN/ERROR).
- **Constraint**: Transfers are permanent — no undo/reverse action is provided.
- **Constraint**: New owner must already exist in the system; inline owner creation is out of scope.

## Capabilities

### New Capabilities

- `pet-transfer`
  - Requirement(s): FR-001, FR-002, FR-003, FR-004, FR-005, FR-006, FR-007, FR-008, FR-009, TR-001, TR-002, TR-003, TR-004, TR-005, TR-006, TR-007, TR-008, TR-009, TR-010
  - End-to-end pet ownership transfer flow: UI entry point on pet detail, owner search and selection, confirmation step, atomic DB commit (owner update + audit record), service-layer validation (self-transfer / pending visits), multi-profile repository implementations, and structured logging.

### Modified Capabilities

<!-- None — this is a net-new capability with no existing spec to delta. -->

## Impact

- **Model**: New `PetTransfer` entity added.
- **Repository**: New `PetTransferRepository` interface + three implementations (`jpa`, `jdbc`, `spring-data-jpa`).
- **Service**: `ClinicService` / `ClinicServiceImpl` extended with `transferPet(...)` method; `@Transactional` wraps both writes.
- **Web**: New `PetTransferController` (or new handler methods in the existing pet controller) handling GET (form) and POST (confirm/commit) for `/pets/{id}/transfer`.
- **Views**: New JSP pages — transfer form and confirmation page.
- **Schema**: New `pet_transfers` table; DDL scripts for H2, MySQL, and PostgreSQL.
- **Spring XML config**: `business-config.xml` updated to wire the correct `PetTransferRepository` per active profile.
- **Tests**: New unit tests (`ClinicServiceTransferTests`) and integration tests extending `AbstractClinicServiceTests`.
- **No external dependencies added.**
