## Proposal
**Session name:** 2026-05-06 13:59:00 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-photo
**Session id:** ses_202e0ebd1ffeoAR5ogs3DzpJvj

## Why

Pet profiles in PetClinic are text-only: owners and clinic staff have no way to attach or view a photo of a pet. Adding a photo capability completes the pet profile, improves visual identification at the clinic, and makes the application more useful in day-to-day veterinary workflows.

## What Changes

- A pet photo can be uploaded (JPEG or PNG, ≤ 2 MB) from the pet detail page.
- The uploaded photo is stored as a BLOB in the database, associated with the pet record.
- The photo is displayed on the pet detail page when one exists; nothing is shown when absent.
- A previously uploaded photo is replaced when a new one is uploaded.
- The photo can be explicitly deleted by the user; after deletion nothing is shown.
- File validation is enforced on both client (HTML/JS) and server sides.
- Upload and delete require an authenticated session; view is available to any authenticated user.
- Photo data is cascade-deleted when the pet record is deleted.
- Photo storage is encapsulated behind a dedicated service/interface to allow future migration to filesystem or cloud storage without controller changes.

## Capabilities

### New Capabilities

- `pet-photo-management`
   - Requirement(s): FR-001, FR-002, FR-003, FR-004, FR-005, TR-001, TR-002, TR-003, TR-004, TR-005, TR-006, TR-007, TR-008
   - Upload, display, replace, and delete a photo for a pet from the pet detail page; includes server- and client-side validation, access control, cascade delete, and storage abstraction.

### Modified Capabilities

_(none — no existing spec-level behavior is changing)_

## Impact

- **Model**: `Pet` entity extended with a photo association (new `PetPhoto` entity or BLOB column).
- **Repository**: New `PetPhotoRepository` (JPA, JDBC, Spring Data JPA variants) or extension of existing pet repositories.
- **Service**: New `PetPhotoService` interface + implementation encapsulating upload, retrieve, delete logic.
- **Controller**: `PetController` extended with upload and delete endpoints; photo served via a dedicated `GET` endpoint streaming the BLOB.
- **Views**: `petDetails.jsp` and `petForm.jsp` updated to include the photo upload form and photo display.
- **Database**: Schema change — new column or table to store photo binary data with FK to `pets`.
- **Tests**: New unit tests for validation/service logic; new integration tests for upload/delete/cascade flows.
- **Dependencies**: No new library dependencies; multipart file upload already supported by Spring MVC.
