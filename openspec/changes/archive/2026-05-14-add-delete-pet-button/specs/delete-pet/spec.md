## Spec
**Session name:** docs(cbn-3-definition): generate artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->

## ADDED Requirements

### Requirement: Delete pet via owner profile page
- Requirement(s): FR-001, TR-002, TR-007
- Description: The system SHALL provide a "Delete" button next to each pet listed on the owner's profile page. Clicking the button SHALL submit an HTTP POST form to the delete endpoint. The form SHALL include a valid CSRF token. No HTTP GET endpoint for deletion is permitted.

#### Scenario: Staff deletes a pet successfully
- **WHEN** a clinic staff member clicks the "Delete" button next to a pet on the owner's profile page
- **THEN** an HTTP POST request is submitted to the delete endpoint with a valid CSRF token
- **AND** the system permanently removes the pet record from the database
- **AND** the user is redirected back to the owner's profile page
- **AND** the deleted pet no longer appears in the pet list

#### Scenario: Delete request without CSRF token is rejected
- **WHEN** an HTTP POST is submitted to the delete endpoint without a valid CSRF token
- **THEN** the server returns a 4xx error response
- **AND** no pet record is deleted

#### Scenario: GET request to delete URL does not delete
- **WHEN** an HTTP GET request is made to the delete URL
- **THEN** the system does NOT perform any deletion
- **AND** the system returns an appropriate non-destructive response (e.g., 405 Method Not Allowed)

### Requirement: Cascade deletion of associated visit records
- Requirement(s): FR-002, TR-003, TR-006, TR-008
- Description: The system SHALL permanently delete all visit records associated with a pet when that pet is deleted. The deletion of the pet and its visits SHALL be executed as a single atomic database transaction. The operation SHALL be implemented as a hard delete (physical removal) with no soft-delete mechanism. This behavior SHALL work identically across all three persistence backends: `jpa`, `jdbc`, and `spring-data-jpa`.

#### Scenario: All visits are removed when a pet is deleted
- **WHEN** a pet with one or more visit records is deleted
- **THEN** all visit records belonging to that pet are permanently removed from the database
- **AND** no visit records for the deleted pet are retrievable through any application query

#### Scenario: Deletion is atomic — partial failure rolls back
- **WHEN** any part of the delete operation fails (e.g., database error)
- **THEN** the entire transaction is rolled back
- **AND** both the pet record and its visit records remain unchanged in the database

#### Scenario: Hard delete — records are not retrievable after deletion
- **WHEN** a pet is successfully deleted
- **THEN** querying for the deleted pet ID returns null or empty
- **AND** querying for visits belonging to the deleted pet returns an empty list

### Requirement: Structured logging for delete failures and invalid requests
- Requirement(s): TR-004
- Description: The system SHALL emit a WARN-level log entry when a delete is attempted for a pet ID that does not exist. The system SHALL emit an ERROR-level log entry if the delete transaction fails due to a database error or unexpected exception. No log entry is required for successful deletions.

#### Scenario: Delete attempted for non-existent pet ID
- **WHEN** a POST request is submitted to delete a pet ID that does not exist in the database
- **THEN** the system emits a WARN-level log entry
- **AND** no deletion is performed

#### Scenario: Delete transaction fails unexpectedly
- **WHEN** the delete transaction fails due to a database error or unexpected exception
- **THEN** the system emits an ERROR-level log entry
- **AND** the transaction is rolled back
