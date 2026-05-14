## Functional Requirements
**Session name:** docs(cbn-1-functional-requirements): generate expected artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->
**Kind of change:** fix

### FR-001: Delete Pet from Owner Profile Page

Clinic staff currently have no way to remove a pet from the system through the user interface. This leaves stale or incorrectly entered pet records accumulating in the system with no cleanup mechanism. A "Delete" button must be added to the owner's profile page next to each listed pet so that staff can permanently remove a pet and all its associated visit records.

#### Acceptances Criteria
- **GIVEN** a clinic staff member is viewing an owner's profile page
- **WHEN** they click the "Delete" button next to a specific pet
- **THEN** the pet is permanently removed from the system along with all its associated visit records
- **AND** the user remains on the owner's profile page, which no longer lists the deleted pet

### FR-002: Cascade Deletion of Associated Visits

When a pet is deleted, all visit records associated with that pet must also be deleted to avoid orphaned data in the system.

#### Acceptances Criteria
- **GIVEN** a pet has one or more visit records
- **WHEN** the pet is deleted via the "Delete" button on the owner's profile page
- **THEN** all visit records belonging to that pet are also permanently removed
- **AND** no visit records for the deleted pet remain accessible anywhere in the application
