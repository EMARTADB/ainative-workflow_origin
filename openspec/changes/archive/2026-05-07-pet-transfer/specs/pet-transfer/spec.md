## Spec
**Session name:** 2026-05-06 19:37:18 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-transfer
**Session id:** ses_201a27cdfffeqS0McnCMut5SMb

## ADDED Requirements

### Requirement: Initiate Transfer from Pet Detail Page
- Requirement(s): FR-001
- Description: A "Transfer Ownership" button is present on the pet detail page. Clicking it opens the transfer form pre-populated with the pet name and current owner.

#### Scenario: Staff clicks Transfer Ownership
- **WHEN** an authenticated staff member views a pet's detail page and clicks "Transfer Ownership"
- **THEN** the transfer form is displayed showing the pet name and the current owner's name

---

### Requirement: Owner Search on Transfer Form
- Requirement(s): FR-002, FR-009, TR-001
- Description: The transfer form allows staff to search for a new owner by last name or ID. Only existing registered owners are returned. Results appear within 500 ms under normal load.

#### Scenario: Staff searches by last name
- **WHEN** a staff member types a last name in the owner search field
- **THEN** matching registered owners are returned and selectable

#### Scenario: No inline owner creation
- **WHEN** a staff member opens the transfer form
- **THEN** no option to create a new owner inline is available

---

### Requirement: Confirmation Step Before Commit
- Requirement(s): FR-003
- Description: After selecting the new owner, a confirmation page shows pet name, current owner, and new owner. The transfer is executed only after explicit confirmation.

#### Scenario: Staff reviews and confirms
- **WHEN** a staff member selects a new owner and reviews the confirmation summary
- **THEN** the summary displays pet name, current owner name, and new owner name
- **AND** the transfer only executes after the staff member submits the Confirm action

---

### Requirement: Atomic Owner Update with Full History Preservation
- Requirement(s): FR-004, TR-004
- Description: On confirmation, `pet.owner_id` is updated to the new owner atomically alongside the audit record insertion. All existing visits, diagnoses, and notes remain linked to the pet.

#### Scenario: New owner views transferred pet
- **WHEN** a transfer is confirmed and the new owner views their pet list
- **THEN** the transferred pet appears with its complete visit history

#### Scenario: Previous owner no longer sees the pet
- **WHEN** a transfer is confirmed
- **THEN** the previous owner no longer sees the pet in their active pet list

#### Scenario: Partial failure rolls back
- **WHEN** the audit record insertion fails during a transfer attempt
- **THEN** `pet.owner_id` is not changed and the database remains consistent

---

### Requirement: Permanent Audit Record on Every Transfer
- Requirement(s): FR-005, TR-007
- Description: Every completed transfer creates a `PetTransfer` record containing `pet_id`, `from_owner_id`, `to_owner_id`, `transferred_at`, and `performed_by`. Records are immutable; no delete path exists.

#### Scenario: Audit record created on success
- **WHEN** a transfer is successfully completed
- **THEN** a `PetTransfer` record exists with the correct `pet_id`, `from_owner_id`, `to_owner_id`, `transferred_at`, and `performed_by`

#### Scenario: No delete method available
- **WHEN** an administrator inspects the repository API
- **THEN** no `delete` or `deleteAll` method is exposed on `PetTransferRepository`

---

### Requirement: Block Transfer with Pending Visits
- Requirement(s): FR-006, TR-005
- Description: If the pet has any active or scheduled visit, the service blocks the transfer and the UI displays an explanatory error message.

#### Scenario: Transfer blocked due to pending visits
- **WHEN** a staff member attempts to transfer a pet that has one or more active or scheduled visits
- **THEN** the system blocks the transfer
- **AND** displays an error message indicating that pending visits must be resolved first

---

### Requirement: Block Self-Transfer
- Requirement(s): FR-007, TR-005
- Description: The system prevents selecting the pet's current owner as the new owner. The validation is enforced at the service layer.

#### Scenario: Self-transfer rejected
- **WHEN** a staff member selects the pet's current owner as the new owner
- **THEN** the system blocks the confirmation step
- **AND** displays a validation message indicating the new owner must be different from the current owner

---

### Requirement: Transfers Are Permanent
- Requirement(s): FR-008
- Description: No undo or reverse action is available after a transfer is committed. To correct a mistaken transfer, a new transfer must be initiated.

#### Scenario: No reverse action available
- **WHEN** a staff member views the pet detail or transfer history after a completed transfer
- **THEN** no "undo" or "reverse" option is presented

---

### Requirement: Multi-Profile Repository Support
- Requirement(s): TR-006
- Description: `PetTransferRepository` has three implementations (jpa, jdbc, spring-data-jpa) wired via Spring XML profiles. All profiles pass the same integration test suite.

#### Scenario: JPA profile functions correctly
- **WHEN** the application runs with the `jpa` profile
- **THEN** the transfer operation persists the audit record via Hibernate/JPA

#### Scenario: JDBC profile functions correctly
- **WHEN** the application runs with the `jdbc` profile
- **THEN** the transfer operation persists the audit record via plain JDBC

#### Scenario: Spring Data JPA profile functions correctly
- **WHEN** the application runs with the `spring-data-jpa` profile
- **THEN** the transfer operation persists the audit record via Spring Data JPA

---

### Requirement: Structured Logging of Transfer Events
- Requirement(s): TR-008
- Description: Transfer outcomes are logged via SLF4J: INFO on success, WARN on blocked transfer (with reason), ERROR on unexpected failure. No PII beyond numeric IDs is written to the log.

#### Scenario: Success logged at INFO
- **WHEN** a transfer completes successfully
- **THEN** an INFO log entry is written with `pet_id`, `from_owner_id`, `to_owner_id`, and `performed_by`

#### Scenario: Blocked transfer logged at WARN
- **WHEN** a transfer is blocked by a validation rule
- **THEN** a WARN log entry is written with `pet_id` and the blocking reason (SELF_TRANSFER or PENDING_VISITS)

---

### Requirement: DB Schema Integrity
- Requirement(s): TR-010
- Description: The `pet_transfers` table uses RESTRICT FK constraints on `pet_id → pets(id)`, `from_owner_id → owners(id)`, and `to_owner_id → owners(id)`. DDL provided for H2, MySQL, and PostgreSQL.

#### Scenario: Owner deletion blocked when transfer record exists
- **WHEN** an attempt is made to delete an owner that has an associated transfer record
- **THEN** the database raises a constraint violation and the deletion is rejected

---

### Requirement: Test Coverage
- Requirement(s): TR-009
- Description: Unit tests cover all service-layer branches. Integration tests extend the existing abstract base and run against each persistence profile. All test files follow the `*Tests.java` naming convention.

#### Scenario: All service branches covered by unit tests
- **WHEN** `./mvnw test` is run
- **THEN** unit tests for happy path, SELF_TRANSFER, PENDING_VISITS, and rollback-on-audit-failure all pass

#### Scenario: Integration tests pass for all profiles
- **WHEN** `./mvnw verify` is run against each persistence profile
- **THEN** integration tests for the transfer feature pass on jpa, jdbc, and spring-data-jpa
