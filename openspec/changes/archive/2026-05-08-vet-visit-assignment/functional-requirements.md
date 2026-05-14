## Functional Requirements
**Session name:** 2026-05-06 18:10:16 - docs(cbn-1-functional-requirements): generate artifact functional-requirements.md for vet-visit-assignment
**Session id:** ses_201f2519effetAkt9h9RrheQQY
**Kind of change:** standard-change

---

### Context

**Business driver:** The clinic needs accountability and traceability over which veterinarian performed each visit. Currently no vet-visit relationship exists in the system; assignment happens outside the application, making it impossible to audit or report by vet.

**Expected outcome:** Each visit record stores and displays the assigned veterinarian, giving clinic staff full visibility without leaving the application.

**Current way of working:** The `Visit` entity has no reference to a `Vet`. The vet who attended a visit is not captured in the data model.

**Target change:** Add a `Visit.vet` `ManyToOne` relationship to the `Vet` entity, expose a dropdown in the visit create/edit form populated with all vets from the database, add the corresponding FK column to the schema, and display the assigned vet in the visit detail view. All three repository implementations (JDBC, JPA, Spring Data JPA) must support the new field.

**Users involved:** Clinic staff (receptionists, administrators).

---

### FR-001: Assign a Veterinarian to a Visit

Clinic staff must be able to assign a veterinarian to a visit when creating or editing it. The assignment is optional; a visit may be saved without a vet selected. Once saved, the assignment can be changed at any time.

#### Acceptance Criteria
- **GIVEN** a staff member is on the visit create or edit form
- **WHEN** the form renders
- **THEN** a dropdown labelled "Veterinarian" is displayed, populated with the full name of every vet stored in the `Vet` table
- **AND** an empty/blank option is available so the field can remain unset

---

### FR-002: Persist the Vet Assignment

The selected vet must be persisted as a foreign-key reference from the `visits` table to the `vets` table, across all active repository implementations.

#### Acceptance Criteria
- **GIVEN** a staff member selects a vet and saves a visit
- **WHEN** the form is submitted
- **THEN** the visit record is stored with the selected vet's ID as a FK (`vet_id` column)
- **AND** the same behavior is consistent across the JDBC, JPA (Hibernate), and Spring Data JPA repository implementations

---

### FR-003: Display the Assigned Vet in the Visit Detail View

The assigned vet must be visible in read-only mode so that staff can confirm who attended or will attend a visit without entering edit mode.

#### Acceptance Criteria
- **GIVEN** a visit has an assigned vet
- **WHEN** a staff member views the visit detail page (read-only)
- **THEN** the vet's full name is displayed in the visit detail section
- **AND** if no vet is assigned, the field is shown as blank (not an error)

---

### FR-004: Edit Vet Assignment After Visit Creation

The assigned vet must be editable after the visit has been created, with no restrictions on reassignment.

#### Acceptance Criteria
- **GIVEN** a visit already has a vet assigned
- **WHEN** a staff member opens the visit edit form
- **THEN** the dropdown shows the currently assigned vet pre-selected
- **AND** the staff member can change the selection to any other vet or clear it
- **AND** saving the form updates the persisted vet reference

---

### FR-005: Legacy Visit Data Compatibility

Existing visit records that were created before this change must not be broken or require a vet assignment.

#### Acceptance Criteria
- **GIVEN** visits that existed before the schema migration
- **WHEN** the migration runs
- **THEN** the `vet_id` column is added with `NULL` as the default for existing rows
- **AND** those visits are accessible and display no vet (blank field), without errors
- **AND** staff can optionally assign a vet to them via the edit form

---

### Business Rules

| # | Rule |
|---|------|
| BR-01 | The vet dropdown is populated from the `Vet` table at the time the form is loaded. |
| BR-02 | Assigning a vet to a visit is optional; `vet_id` is nullable. |
| BR-03 | Any vet in the system can be assigned to any visit regardless of specialty or pet type. |
| BR-04 | The vet assignment can be changed or cleared by staff at any time. |
| BR-05 | Existing visits default to `vet_id = NULL` after the migration; no backfill is required. |

---

### Out of Scope

- Filtering vets by specialty or pet type in the dropdown.
- Vet-facing views or self-assignment workflows.
- Pet-owner visibility of the assigned vet.
- Visit locking or immutability rules based on vet assignment.
- Notifications or scheduling logic triggered by vet assignment.
