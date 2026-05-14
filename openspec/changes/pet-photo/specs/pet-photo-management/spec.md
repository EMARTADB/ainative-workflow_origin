## Spec
**Session name:** 2026-05-06 13:59:00 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-photo
**Session id:** ses_202e0ebd1ffeoAR5ogs3DzpJvj

## ADDED Requirements

### Requirement: Upload pet photo
- Requirement(s): FR-001, TR-001, TR-002, TR-003, TR-004, TR-005
- Description: The system SHALL allow an authenticated user (owner or staff) to upload a JPEG or PNG photo (≤ 2 MB) for a pet from the pet edit form. The photo MUST be validated on both client and server. A successful upload SHALL replace any previously stored photo for that pet.

#### Scenario: Successful upload replaces existing photo
- **WHEN** an authenticated user submits the pet edit form with a valid JPEG or PNG file ≤ 2 MB
- **THEN** the photo is stored in the database linked to that pet and the pet detail view shows the new photo

#### Scenario: Upload rejected — wrong file type
- **WHEN** an authenticated user submits a file that is not JPEG or PNG
- **THEN** the server returns an error response and the existing photo remains unchanged

#### Scenario: Upload rejected — file too large
- **WHEN** an authenticated user submits a file exceeding 2 MB
- **THEN** the server returns an error response and the existing photo remains unchanged

#### Scenario: Unauthenticated upload attempt
- **WHEN** an unauthenticated request is sent to the photo upload endpoint
- **THEN** the request is rejected with HTTP 401 or redirected to login

---

### Requirement: Display pet photo
- Requirement(s): FR-002, FR-003, TR-005
- Description: The system SHALL display the pet photo on the owner detail page when a photo exists. When no photo has been uploaded, the photo area SHALL NOT render any image or placeholder. The photo SHALL be served via a dedicated streaming endpoint and load within 2 seconds under normal conditions.

#### Scenario: Photo displayed when present
- **WHEN** a user views the owner detail page for a pet that has a stored photo
- **THEN** an image element is rendered showing the pet photo

#### Scenario: No output when photo absent
- **WHEN** a user views the owner detail page for a pet with no stored photo
- **THEN** no image element or placeholder is rendered in the photo area

#### Scenario: Photo served via streaming endpoint
- **WHEN** the browser requests the photo URL (`GET /owners/{ownerId}/pets/{petId}/photo`)
- **THEN** the response contains the image binary with the correct `Content-Type` header (image/jpeg or image/png)

---

### Requirement: Delete pet photo
- Requirement(s): FR-004, TR-004
- Description: The system SHALL allow an authenticated user (owner or staff) to explicitly delete a pet's photo. After deletion, the photo SHALL be removed from the database and the owner detail page SHALL show no image for that pet.

#### Scenario: Successful deletion
- **WHEN** an authenticated user submits a delete photo request for a pet that has a photo
- **THEN** the photo is removed from the database and the owner detail page no longer shows an image for that pet

#### Scenario: Unauthenticated delete attempt
- **WHEN** an unauthenticated request is sent to the photo delete endpoint
- **THEN** the request is rejected with HTTP 401 or redirected to login

---

### Requirement: Client-side file validation
- Requirement(s): FR-005, TR-003
- Description: The pet photo upload form field SHALL restrict selectable files to JPEG and PNG via the `accept` attribute. A JavaScript check SHALL prevent form submission and display an inline error if the selected file exceeds 2 MB. This validation is a UX safeguard and does not replace server-side validation.

#### Scenario: Oversized file blocked before submission
- **WHEN** a user selects a file larger than 2 MB in the upload field
- **THEN** an inline error message is shown and the form is not submitted

#### Scenario: Non-image file type filtered by file dialog
- **WHEN** the user opens the file picker
- **THEN** the dialog filters to JPEG and PNG files by default (via the `accept` attribute)

---

### Requirement: Cascade delete photo with pet
- Requirement(s): TR-006
- Description: When a pet record is deleted, its associated photo SHALL be deleted automatically. No orphan photo data SHALL remain in the `pet_photos` table after pet deletion. This MUST be enforced both at the ORM level (JPA cascade) and at the database level (`ON DELETE CASCADE` FK constraint) to cover all persistence profiles.

#### Scenario: Photo removed when pet is deleted
- **WHEN** a pet record is deleted from the system
- **THEN** the associated row in `pet_photos` is also deleted and no orphan record remains

---

### Requirement: Storage abstraction via PetPhotoStore interface
- Requirement(s): TR-001, TR-007
- Description: All photo persistence operations (save, retrieve, delete) MUST be accessed through a `PetPhotoStore` interface. No controller or JSP SHALL interact with the photo repository directly. The current implementation SHALL use a database BLOB. The interface design MUST allow future implementations (filesystem, cloud) to be substituted without modifying controllers or views.

#### Scenario: Controller delegates to service
- **WHEN** the upload endpoint receives a valid file
- **THEN** the controller calls `PetPhotoService.save(petId, bytes, contentType)` and does not write to any repository directly

#### Scenario: Implementation is swappable
- **WHEN** a new `PetPhotoStore` implementation is registered as a Spring bean
- **THEN** the service and controller require no code changes to use it
