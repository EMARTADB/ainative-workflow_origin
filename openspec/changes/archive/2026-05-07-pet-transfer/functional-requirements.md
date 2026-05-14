## Functional Requirements
**Session name:** 2026-05-06 17:56:02 - docs(cbn-1-functional-requirements): generate artifact functional-requirements.md for pet-transfer
**Session id:** ses_201ff8d94ffetny5DKDYZoz2da
**Kind of change:** standard-change

---

### Context

**Business driver:**
Pets are regularly rehomed, adopted, or transferred between owners in real clinic practice. The system must support this lifecycle event to reflect accurate ownership at any point in time.

**Current way of working:**
Pet ownership transfer is not possible today. There is no mechanism in the application to reassign a pet to a different owner.

**Expected outcome:**
Clinic staff can transfer a pet from one registered owner to another via a dedicated UI flow. The full medical history (visits, diagnoses, notes) remains visible under the pet and is accessible by the new owner. A permanent, non-reversible audit record is kept for every transfer.

**Scope:**
- "Transfer Ownership" action on the pet detail page.
- Search and selection of the new owner (must already exist in the system).
- Validation guardrails (pending visits, self-transfer).
- Update of the pet's `owner_id` foreign key.
- Creation of a `PetTransfer` audit record.
- No notifications to owners in this version.

**Out of scope:**
- Inline creation of a new owner during the transfer flow.
- Reversal or undo of a completed transfer (must perform a new transfer).
- Owner-initiated self-service transfer.
- Email or system notifications to previous or new owners.
- Billing or invoice reassignment.

**Users involved:**
- Clinic receptionist / administrative staff (initiates and confirms the transfer).

---

### FR-001: Initiate Pet Transfer from Pet Detail Page

Staff can trigger a pet ownership transfer directly from the pet's detail page. A clearly labelled "Transfer Ownership" button or link is present on that page. Clicking it opens the transfer form.

#### Acceptance Criteria
- **GIVEN** a staff member is viewing a pet's detail page
- **WHEN** they click "Transfer Ownership"
- **THEN** a transfer form is displayed showing the pet name and current owner

---

### FR-002: Select New Owner via Search

The transfer form allows staff to search for and select the new owner by name or ID. The new owner must already exist in the system.

#### Acceptance Criteria
- **GIVEN** the transfer form is open
- **WHEN** the staff member types a name or ID in the owner search field
- **THEN** the system returns matching registered owners
- **AND** the staff member can select one as the new owner

---

### FR-003: Confirm Transfer Before Committing

Before the transfer is executed, the system presents a confirmation summary showing the pet name, current owner, and selected new owner. Staff must explicitly confirm to proceed.

#### Acceptance Criteria
- **GIVEN** the staff member has selected a new owner
- **WHEN** they review the confirmation summary
- **THEN** the summary displays: pet name, current owner, new owner
- **AND** the transfer is only executed after an explicit Confirm action

---

### FR-004: Transfer Assigns New Owner and Preserves Full Medical History

Upon confirmation, the pet's `owner_id` is updated to the new owner. All existing visits, diagnoses, and notes remain linked to the pet and are fully visible through the new owner's profile.

#### Acceptance Criteria
- **GIVEN** a transfer has been confirmed
- **WHEN** the new owner views their pet list
- **THEN** the transferred pet appears with its complete visit history
- **AND** the previous owner no longer sees the pet in their active pet list

---

### FR-005: Audit Record Created on Every Transfer

Every completed transfer creates a permanent audit record containing: pet identifier, previous owner identifier, new owner identifier, transfer timestamp, and the staff user who performed it.

#### Acceptance Criteria
- **GIVEN** a transfer has been successfully completed
- **WHEN** an administrator queries the transfer history
- **THEN** a `PetTransfer` record exists with: `pet_id`, `from_owner_id`, `to_owner_id`, `transferred_at`, `performed_by`

---

### FR-006: Block Transfer When Pet Has Active or Scheduled Visits

If the pet has any visit that is scheduled or currently open (not completed/cancelled), the system blocks the transfer and informs staff of the reason.

#### Acceptance Criteria
- **GIVEN** a staff member initiates a transfer for a pet
- **WHEN** the pet has one or more active or scheduled visits
- **THEN** the system blocks the transfer
- **AND** displays an error message explaining that pending visits must be resolved first

---

### FR-007: Block Transfer to Current Owner (Self-Transfer)

The system prevents a transfer where the selected new owner is the same as the current owner.

#### Acceptance Criteria
- **GIVEN** a staff member is filling out the transfer form
- **WHEN** they select the pet's current owner as the new owner
- **THEN** the system blocks the confirmation step
- **AND** displays a validation message indicating the new owner must be different from the current owner

---

### FR-008: Transfers Are Permanent

Once a transfer is confirmed and committed, it cannot be undone or reversed within the application. To correct a mistaken transfer, staff must initiate a new transfer back to the original owner.

#### Acceptance Criteria
- **GIVEN** a transfer has been completed
- **WHEN** a staff member views the transfer history or pet detail
- **THEN** there is no "undo" or "reverse" action available
- **AND** the system documentation (and UI help text if applicable) states that transfers are permanent

---

### FR-009: New Owner Must Pre-Exist in the System

A pet can only be transferred to an owner already registered in the system. Inline creation of a new owner record is not supported within the transfer flow.

#### Acceptance Criteria
- **GIVEN** a staff member opens the transfer form
- **WHEN** they search for a new owner
- **THEN** only existing registered owners are selectable
- **AND** the form provides no option to create a new owner inline
