## Spec
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->

## ADDED Requirements

### Requirement: Vet dropdown displayed on visit create form
- Requirement(s): FR-001, TR-001
- Description: The visit create form SHALL display a "Veterinarian" dropdown populated with the full name (`firstName lastName`) of every vet in the `vets` table. An empty/blank option SHALL be the default so the field can remain unset. The dropdown MUST be populated on every form load via a single call to `ClinicService.findVets()`.

#### Scenario: Dropdown renders with all vets
- **WHEN** a staff member navigates to the visit create form (`GET /owners/*/pets/{petId}/visits/new`)
- **THEN** the model contains a `vets` attribute with a non-empty collection
- **AND** the form renders a `<select>` labelled "Veterinarian" with one `<option>` per vet plus a blank first option

#### Scenario: Dropdown renders with no pre-selection
- **WHEN** the visit is new (no existing assignment)
- **THEN** the blank option is selected by default and no vet is pre-selected

### Requirement: Vet assignment persisted as nullable FK
- Requirement(s): FR-002, TR-003, TR-004
- Description: When a visit is saved, the `vet_id` column in the `visits` table SHALL store the selected vet's primary key. When no vet is selected, `vet_id` SHALL be `NULL`. This behavior MUST be consistent across the JDBC, JPA, and Spring Data JPA repository implementations.

#### Scenario: Save visit with vet selected
- **WHEN** a staff member selects a vet from the dropdown and submits the visit create form
- **THEN** the visit record is stored with the selected vet's ID in `vet_id`
- **AND** reloading the visit returns `visit.getVet() != null` with a matching ID

#### Scenario: Save visit without vet selected
- **WHEN** a staff member submits the visit create form with the blank option selected
- **THEN** the visit record is stored with `vet_id = NULL`
- **AND** reloading the visit returns `visit.getVet() == null`

#### Scenario: Cross-repository consistency
- **WHEN** the same save/retrieve cycle is executed under the JDBC, JPA, and Spring Data JPA profiles
- **THEN** the persisted `vet_id` value and the returned `vet` reference are identical across all three profiles

### Requirement: Assigned vet displayed in visit detail view
- Requirement(s): FR-003
- Description: The visit detail/list view SHALL display the assigned vet's full name (`firstName lastName`) in a read-only field labelled "Veterinarian". When `vet_id` is `NULL`, the field SHALL render as blank without an error.

#### Scenario: Visit with assigned vet shown in detail view
- **WHEN** a staff member views a visit that has a vet assigned
- **THEN** the vet's full name is displayed in the "Veterinarian" field

#### Scenario: Visit without assigned vet shown in detail view
- **WHEN** a staff member views a visit that has no vet assigned (`vet_id = NULL`)
- **THEN** the "Veterinarian" field is blank and no error or exception is shown

### Requirement: Pre-selected vet on visit edit form
- Requirement(s): FR-004
- Description: When a visit with an existing vet assignment is opened for editing, the dropdown SHALL pre-select the currently assigned vet. The staff member SHALL be able to change the selection to any other vet or clear it, and saving SHALL update the persisted `vet_id`.

#### Scenario: Dropdown pre-selects current vet
- **WHEN** a staff member opens the edit form for a visit that has vet A assigned
- **THEN** the dropdown shows vet A as the selected option

#### Scenario: Vet reassignment persisted
- **WHEN** a staff member changes the dropdown from vet A to vet B and saves
- **THEN** the visit record is updated with vet B's ID in `vet_id`

#### Scenario: Vet assignment cleared on edit
- **WHEN** a staff member selects the blank option and saves
- **THEN** `vet_id` is set to `NULL`

### Requirement: Legacy visits compatible after schema migration
- Requirement(s): FR-005, TR-003
- Description: Existing visit rows present before the `vet_id` column is added SHALL default to `NULL`. Those visits SHALL be readable and display a blank "Veterinarian" field without errors. Staff SHALL be able to assign a vet to them via the edit form.

#### Scenario: Pre-existing visits accessible after migration
- **WHEN** the schema is updated to add `vet_id INT NULL`
- **THEN** existing visit rows retain `vet_id = NULL` with no backfill required
- **AND** those visits are returned by the repository without `NullPointerException`
- **AND** the UI displays a blank "Veterinarian" field for them

### Requirement: Vet list fetch failure propagates as HTTP 500
- Requirement(s): TR-005
- Description: If `ClinicService.findVets()` throws a `DataAccessException` during form rendering, the exception SHALL NOT be caught by the controller. It SHALL propagate to the Spring MVC exception handler, resulting in HTTP 500. The exception SHALL be logged at `ERROR` level.

#### Scenario: DataAccessException propagates from controller
- **WHEN** `ClinicService.findVets()` throws `DataAccessException`
- **THEN** the exception propagates out of the controller method without being caught
- **AND** the Spring MVC error handler returns HTTP 500
- **AND** an `ERROR`-level log entry with a stack trace is produced

### Requirement: Visit JSON includes vetId scalar field
- Requirement(s): TR-008
- Description: When a `Visit` resource is serialized to JSON, the response SHALL include a `vetId` field containing the integer ID of the assigned vet, or `null` when no vet is assigned. The full `Vet` object (including specialties) SHALL NOT be embedded. No `LazyInitializationException` SHALL occur during serialization.

#### Scenario: JSON response includes vetId when vet assigned
- **WHEN** a visit with an assigned vet is serialized to JSON
- **THEN** the JSON contains `"vetId": <integer>` matching the assigned vet's ID
- **AND** no nested `vet` object with specialties is present

#### Scenario: JSON response includes null vetId when no vet assigned
- **WHEN** a visit with no vet is serialized to JSON
- **THEN** the JSON contains `"vetId": null`
- **AND** no `LazyInitializationException` is thrown
