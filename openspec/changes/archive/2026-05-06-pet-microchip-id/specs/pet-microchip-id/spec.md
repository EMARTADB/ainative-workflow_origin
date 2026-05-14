## Spec
**Session name:** 2026-05-06 15:23:30 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-microchip-id
**Session id:** ses_2028ad7fbffePuOwT3812K1vNN

## ADDED Requirements

### Requirement: Pet microchip ID field
- Requirement(s): FR-001, FR-003, TR-001, TR-004, TR-005
- Description: The `Pet` entity SHALL have an optional `microchipId` attribute stored as `microchip_id` in the `pets` table. The field SHALL accept null (no microchip recorded) or a string that conforms to ISO 11784/11785 (exactly 15 decimal digits). All three persistence backends (JPA, JDBC, Spring Data JPA) and all four database DDL scripts (H2, HSQLDB, MySQL, PostgreSQL) MUST persist and retrieve the field consistently.

#### Scenario: Save pet without microchip ID
- **WHEN** a staff member submits the Add or Edit Pet form with the microchipId field left blank
- **THEN** the pet is saved with `microchipId = null` and no validation error is shown

#### Scenario: Save pet with valid microchip ID
- **WHEN** a staff member submits the form with a 15-digit numeric microchipId (e.g., `123456789012345`)
- **THEN** the pet is saved and the value is retrievable from all three persistence backends

#### Scenario: DDL scripts include microchip_id column
- **WHEN** the application starts with a clean schema on any supported database
- **THEN** the `pets` table contains a nullable `microchip_id` column with a UNIQUE constraint

---

### Requirement: Microchip ID format validation
- Requirement(s): FR-002, TR-001
- Description: Any non-null value for `microchipId` MUST match the pattern `^\d{15}$` (exactly 15 decimal digits). Validation SHALL occur at both the MVC layer (Bean Validation annotation on the form-backing object) and the service/persistence layer. Empty strings submitted via form MUST be coerced to null before validation runs.

#### Scenario: Valid 15-digit microchipId passes validation
- **WHEN** a staff member submits a microchipId of exactly 15 digits
- **THEN** no validation error is raised and the value is persisted

#### Scenario: Microchip ID shorter than 15 digits fails validation
- **WHEN** a staff member submits a microchipId with fewer than 15 digits (e.g., `12345`)
- **THEN** the form is not saved and a field-level error "Microchip ID must be exactly 15 digits" is displayed

#### Scenario: Microchip ID with non-digit characters fails validation
- **WHEN** a staff member submits a microchipId containing letters or symbols (e.g., `1234567890ABCDE`)
- **THEN** the form is not saved and a field-level validation error is displayed

#### Scenario: Empty microchip ID field treated as null
- **WHEN** a staff member submits the form with an empty microchipId input
- **THEN** the value is coerced to null, no format validation error is raised, and the pet is saved

---

### Requirement: Microchip ID uniqueness enforcement
- Requirement(s): FR-004, TR-002, TR-007
- Description: The `microchip_id` column SHALL have a UNIQUE constraint at the database level. The application MUST prevent two pets from sharing the same non-null microchipId. When a duplicate is detected — either by application pre-check or by catching a `DataIntegrityViolationException` from the DB — the form SHALL NOT be saved and the user SHALL see a clear error message ("This microchip ID is already registered to another pet").

#### Scenario: Duplicate microchipId rejected at save
- **WHEN** a staff member submits a microchipId that is already assigned to another pet
- **THEN** the form is not saved and the error "This microchip ID is already registered to another pet" is displayed on the form

#### Scenario: Multiple pets with null microchipId are allowed
- **WHEN** two or more pets have no microchipId (null)
- **THEN** both are saved without any uniqueness error

#### Scenario: Race-condition duplicate caught by DB constraint
- **WHEN** two concurrent save requests bypass the application pre-check and both attempt to persist the same microchipId
- **THEN** the DB unique constraint rejects the second insert, the controller catches `DataIntegrityViolationException`, and the user sees a form error instead of a 500 page

---

### Requirement: Microchip ID display on pet-related views
- Requirement(s): FR-005
- Description: Once saved, the `microchipId` SHALL be displayed in a read-only labeled field on the pet detail view and on the owner summary page alongside other pet attributes. When `microchipId` is null, the field SHALL either be hidden or show "—".

#### Scenario: MicrochipId shown on pet detail view
- **WHEN** a staff member views the detail page of a pet that has a microchipId
- **THEN** the microchipId is shown as a labeled read-only field with no edit controls

#### Scenario: Null microchipId shown as dash on owner summary
- **WHEN** a staff member views the owner summary page for a pet with no microchipId
- **THEN** the microchipId row shows "—" or is absent — no blank or broken layout

#### Scenario: MicrochipId shown on owner summary page
- **WHEN** a staff member views the owner summary page listing a pet that has a microchipId
- **THEN** the microchipId is displayed inline alongside pet name, type, and birthdate

---

### Requirement: Microchip ID search on Find Owners page
- Requirement(s): FR-006, TR-003
- Description: The Find Owners page SHALL provide a dedicated input field for searching by exact microchip ID. Submitting a valid 15-digit microchipId that matches a pet SHALL display the associated owner's record. A non-matching ID SHALL show the standard empty-state message. A partial or invalid-format input SHALL show a validation hint without executing the search.

#### Scenario: Exact microchip ID match returns owner
- **WHEN** a staff member enters a valid 15-digit microchipId that matches an existing pet and submits the microchip search
- **THEN** the owner associated with that pet is displayed in the results

#### Scenario: Non-matching microchip ID shows empty state
- **WHEN** a staff member enters a valid 15-digit microchipId that matches no pet
- **THEN** the results show "No owners found" (or equivalent empty-state message)

#### Scenario: Invalid-format microchipId shows validation hint
- **WHEN** a staff member enters a microchipId that does not match `^\d{15}$` in the microchip search field
- **THEN** a validation hint is shown and no search query is executed

#### Scenario: Partial microchipId does not return results
- **WHEN** a staff member enters fewer than 15 digits in the microchip search field
- **THEN** a validation hint is shown and no results are returned (no partial match)
