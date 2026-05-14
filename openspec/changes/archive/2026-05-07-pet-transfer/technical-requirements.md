## Technical Requirements
**Session name:** 2026-05-06 18:16:15 - docs(cbn-2-technical-requirements): generate artifact technical-requirements.md for pet-transfer
**Session id:** ses_201ecacb9fferSrsQcnYC3NGrD
**Kind of change:** standard-change

---

### TR-001: Owner Search Response Time

The owner search executed from the transfer form must return results within an acceptable time frame for interactive use.

**Category:** Performance

**Constraint / Target:**
- Owner search response (from HTTP request to rendered results) must complete in < 500 ms under normal clinic-scale load (up to 10,000 registered owners).

**Verification:**
- Manual test with H2 seeded to 10,000 owner records; measure round-trip time in browser dev tools.
- Integration test asserts that the search query executes without full-table scan (indexed `owners.last_name`).

---

### TR-002: Transfer Commit Response Time

The operation that updates `pet.owner_id` and inserts the `PetTransfer` audit record must complete within an interactive threshold.

**Category:** Performance

**Constraint / Target:**
- The full transfer commit (HTTP POST → DB write → redirect) must complete in < 1 s under normal load.

**Verification:**
- Integration test measures total service-layer execution time for `transferPet(...)`.
- Manual smoke test on Jetty (local) and H2 confirms response is perceived as instant.

---

### TR-003: Access Control — Authenticated Staff Only

Only authenticated users (clinic receptionist / administrative staff) may initiate or confirm a pet transfer. Unauthenticated access to the transfer endpoint must be rejected.

**Category:** Security

**Constraint / Target:**
- All transfer URLs (`/pets/{id}/transfer`, `/pets/{id}/transfer/confirm`) must be protected by the existing authentication filter/interceptor.
- No role distinction beyond "authenticated staff" is required in this version.

**Verification:**
- Integration test: unauthenticated GET/POST to transfer URLs returns 401 or redirect to login.
- No transfer operation is reachable without a valid session.

---

### TR-004: Atomic Transfer Transaction

The owner update (`pet.owner_id`) and the `PetTransfer` audit record insertion must succeed or fail together within a single database transaction. A partial commit must never leave the system in an inconsistent state.

**Category:** Reliability

**Constraint / Target:**
- Both writes are wrapped in a single `@Transactional` service-layer call.
- If the audit record insertion fails (e.g., constraint violation, DB error), the transaction is rolled back and `pet.owner_id` is not changed.

**Verification:**
- Unit test: mock `PetTransferRepository.save(...)` to throw a `RuntimeException`; assert that `petRepository.save(...)` was never committed (verify via spy or rollback detection).
- Integration test: inject a deliberate failure after the pet update; confirm DB state is unchanged after rollback.

---

### TR-005: Input Validation at Service Layer

Validation rules for FR-006 (pending visits block) and FR-007 (self-transfer block) must be enforced at the service layer, not only in the UI, to prevent bypass via direct HTTP calls.

**Category:** Security / Reliability

**Constraint / Target:**
- `ClinicServiceImpl.transferPet(...)` must throw a checked or domain exception if:
  - The new owner equals the current owner (`PetTransferException: SELF_TRANSFER`).
  - The pet has any visit in an active/scheduled state (`PetTransferException: PENDING_VISITS`).

**Verification:**
- Unit tests for each validation branch in `ClinicServiceImpl`.
- Controller tests assert that the appropriate error message is rendered in the UI on each exception.

---

### TR-006: Multi-Backend Portability

The feature must function identically across all three persistence profiles: `jpa`, `jdbc`, and `spring-data-jpa`. Each profile requires its own repository implementation for `PetTransferRepository`.

**Category:** Portability / Extensibility

**Constraint / Target:**
- A `PetTransferRepository` interface is defined in the `repository` package.
- Three implementations are provided under `repository/jpa/`, `repository/jdbc/`, and `repository/springdatajpa/`.
- Spring XML config (`business-config.xml`) wires the correct implementation per active profile.
- All three profiles pass the same integration test suite (see TR-008).

**Verification:**
- CI runs `./mvnw verify` with the default profile (JPA).
- Manual verification with `-Dspring.profiles.active=jdbc` and `-P spring-data-jpa` before merge.

---

### TR-007: Audit Record Retention — Permanent

`PetTransfer` audit records are immutable and must never be deleted by application logic. No TTL, archival, or purge mechanism is introduced.

**Category:** Compliance / Privacy

**Constraint / Target:**
- The `pet_transfers` table has no soft-delete column and no application-level delete path.
- `PetTransferRepository` exposes no `delete` or `deleteAll` method.
- Records contain: `pet_id`, `from_owner_id`, `to_owner_id`, `transferred_at`, `performed_by` (staff identifier).

**Verification:**
- Code review confirms no delete method on `PetTransferRepository`.
- Schema DDL reviewed to confirm no cascade-delete from `pets` or `owners` to `pet_transfers`.

---

### TR-008: Structured Logging of Transfer Events

Transfer outcomes must be written to the application log at defined severity levels to support operational monitoring.

**Category:** Operability / Observability

**Constraint / Target:**
- `INFO`: transfer successfully completed — log `pet_id`, `from_owner_id`, `to_owner_id`, `performed_by`.
- `WARN`: transfer blocked by a validation rule — log `pet_id`, blocking reason (SELF_TRANSFER or PENDING_VISITS).
- `ERROR`: unexpected exception during transfer — log full stack trace.
- Log statements use SLF4J via the existing logger pattern in the codebase.
- No PII beyond owner/pet IDs (numeric) is written to the log.

**Verification:**
- Code review confirms log statements at the correct levels.
- Integration test captures log output (using a log appender spy or test logger) and asserts the expected INFO entry on success.

---

### TR-009: Test Coverage — Service and Persistence

The transfer feature must have verifiable test coverage at both the service and persistence layers.

**Category:** Testability

**Constraint / Target:**
- **Unit tests** in `*Tests.java` (Surefire naming convention) cover all service-layer branches:
  - Happy path transfer.
  - SELF_TRANSFER validation.
  - PENDING_VISITS validation.
  - Rollback on audit failure.
- **Integration tests** extend `AbstractClinicServiceTests` (or a new equivalent base) and run against each persistence profile (H2, JPA, JDBC, Spring Data JPA).
- No test file is named `*Test.java` (silently skipped by Surefire — see AGENTS.md).

**Verification:**
- `./mvnw test` produces a passing test report that includes all new test classes.
- Test class names confirmed to end in `Tests.java`.

---

### TR-010: DB Schema — Foreign Key Integrity

The `pet_transfers` table must reference `pets`, `owners` (×2), and a staff identifier with appropriate foreign key constraints to prevent orphaned audit records.

**Category:** Reliability / Maintainability

**Constraint / Target:**
- `pet_id` → `pets(id)` — RESTRICT on delete (no cascade; pet deletion blocked if transfer record exists).
- `from_owner_id` → `owners(id)` — RESTRICT on delete.
- `to_owner_id` → `owners(id)` — RESTRICT on delete.
- `performed_by` stores a string identifier (staff username or ID) — not a FK, to decouple from a future user management feature.
- DDL scripts provided for H2 (test), MySQL, and PostgreSQL.

**Verification:**
- Schema migration scripts reviewed for all supported dialects.
- Integration test verifies that attempting to delete an owner with an associated transfer record raises a constraint violation.
