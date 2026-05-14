## Functional Requirements
**Session name:** 2026-05-06 13:49:34 - docs(cbn-1-functional-requirements): generate artifact functional-requirements.md for pet-photo
**Session id:** ses_202e0ebd1ffeoAR5ogs3DzpJvj
**Kind of change:** small-change

---

### FR-001: Upload Pet Photo

Pet owners and clinic staff can upload a photo for a pet from the pet detail page. The uploaded photo replaces any previously stored photo. The file must be JPEG or PNG format and must not exceed 2 MB.

#### Acceptance Criteria
- **GIVEN** a user (owner or staff) is viewing a pet profile page
- **WHEN** they upload a JPEG or PNG file of 2 MB or less
- **THEN** the photo is stored and immediately displayed on the pet profile page
- **AND** any previously stored photo is replaced

---

### FR-002: Display Pet Photo

When a pet has an associated photo, it is displayed on the pet profile page. When no photo has been uploaded, nothing is displayed in the photo area.

#### Acceptance Criteria
- **GIVEN** a pet profile has a stored photo
- **WHEN** any user (owner or staff) views the pet detail page
- **THEN** the photo is displayed on the page
- **AND** the photo is visually associated with the correct pet

---

### FR-003: No Photo State

When a pet has no photo uploaded, the photo section does not display a placeholder or default image — the area is simply absent.

#### Acceptance Criteria
- **GIVEN** a pet profile has no stored photo
- **WHEN** any user views the pet detail page
- **THEN** no image or placeholder is shown in the photo area

---

### FR-004: Delete Pet Photo

Pet owners and clinic staff can delete the current photo of a pet from the pet detail page. After deletion, no photo is shown (per FR-003).

#### Acceptance Criteria
- **GIVEN** a pet profile has a stored photo
- **WHEN** the user (owner or staff) chooses to delete the photo
- **THEN** the photo is removed from the pet profile
- **AND** no image is displayed on the pet detail page after deletion

---

### FR-005: File Validation

Only JPEG and PNG files up to 2 MB are accepted. Files that do not meet these constraints must be rejected with a clear error message.

#### Acceptance Criteria
- **GIVEN** a user attempts to upload a photo
- **WHEN** the file is not JPEG or PNG, or exceeds 2 MB
- **THEN** the upload is rejected
- **AND** an error message is displayed to the user explaining the constraint
- **AND** the existing photo (if any) remains unchanged

---

### Business Rules

- One photo per pet; a new upload always replaces the existing one.
- Photo is optional — pets without a photo are valid.
- Allowed formats: JPEG, PNG.
- Maximum file size: 2 MB.
- Delete is in scope for this change.
- Photo deletion is not the same as upload failure — it is an explicit user action.

### Constraints

- Only owners and clinic staff are affected; no other roles are in scope.
- Multiple photos (gallery) are out of scope.
- Photo storage mechanism (filesystem, database blob, cloud) is a technical decision deferred to technical requirements.
