## Technical Requirements
**Session name:** 2026-05-06 13:54:39 - docs(cbn-2-technical-requirements): generate artifact technical-requirements.md for pet-photo
**Session id:** ses_202e0ebd1ffeoAR5ogs3DzpJvj
**Kind of change:** small-change

---

### TR-001: Photo Storage as Database BLOB

Pet photos must be stored as binary data (BLOB) in the relational database, associated with the Pet entity. No external filesystem or cloud storage is used for this change. The storage implementation must be placed behind a dedicated abstraction (interface) so that future migration to filesystem or cloud storage does not require changes to controllers or service consumers.

**Category:** Portability / Extensibility

**Constraint / Target:**
- Photo binary data stored in a dedicated column or table linked to `Pet` by foreign key.
- Storage logic encapsulated behind a `PetPhotoStore` (or equivalent) interface.
- No direct BLOB read/write in controllers or JSP views.

**Verification:**
- Code review confirms storage abstraction is in place.
- Integration test stores and retrieves a photo via the interface.

---

### TR-002: File Validation — Server Side

The server must independently validate uploaded files regardless of any client-side check. Files that fail validation must be rejected with HTTP 400 and a descriptive error message. The existing photo must remain unchanged on rejection.

**Category:** Security

**Constraint / Target:**
- Accepted MIME types: `image/jpeg`, `image/png`.
- Maximum file size: 2 MB (2,097,152 bytes).
- Validation applied before any write operation to the database.

**Verification:**
- Unit test: submit file >2 MB → expect rejection with error message.
- Unit test: submit a `.gif` file → expect rejection.
- Integration test: existing photo is unchanged after a failed upload.

---

### TR-003: File Validation — Client Side

A client-side check (JavaScript or HTML5 `accept` + `size` attributes) must warn the user before form submission if the file does not meet constraints. This is a UX safeguard and does not replace server-side validation (TR-002).

**Category:** Security

**Constraint / Target:**
- `<input type="file" accept="image/jpeg,image/png">` enforced at the HTML level.
- JavaScript validates file size ≤ 2 MB before form submit and surfaces an inline error message.

**Verification:**
- Manual/browser test: selecting a non-JPEG/PNG file shows an inline error without submitting the form.
- Manual test: selecting a file >2 MB shows an inline error without submitting.

---

### TR-004: Access Control

Upload and delete operations require an authenticated session. Any unauthenticated request to the photo upload or delete endpoints must be rejected. View (display) of a pet photo is accessible to any authenticated user (owner or staff), consistent with existing access rules for the pet detail page.

**Category:** Security

**Constraint / Target:**
- Upload (`POST`) and delete (`DELETE`/`POST`) endpoints protected by existing Spring Security configuration.
- Unauthenticated requests return HTTP 401/403 or redirect to login.
- No new roles introduced; existing `OWNER` and `STAFF` roles apply.

**Verification:**
- Integration test: unauthenticated upload request returns 401/403.
- Integration test: authenticated staff can upload and delete; authenticated owner can upload and delete their own pet's photo.

---

### TR-005: Upload Performance

The photo upload flow must complete within acceptable time bounds to avoid degrading the user experience.

**Category:** Performance

**Constraint / Target:**
- Upload of a 2 MB file completes (server acknowledges) in ≤ 5 seconds under normal load.
- Photo is displayed on the pet detail page in ≤ 2 seconds after page load.

**Verification:**
- Load/timing test (manual or automated): upload a 2 MB file and measure server round-trip time.
- Browser DevTools or test assertion: photo `<img>` renders within 2 seconds on the pet detail page.

---

### TR-006: Cascade Delete

When a Pet record is deleted from the system, its associated photo must be deleted as well. No orphan photo data should remain in the database.

**Category:** Compliance / Privacy

**Constraint / Target:**
- Photo data deleted atomically with the Pet record (cascade delete at the ORM/DB level).
- No orphan rows in the photo storage table/column after pet deletion.

**Verification:**
- Integration test: create pet with photo → delete pet → assert no photo data remains in DB.

---

### TR-007: Encapsulation in Dedicated Service

All photo-related business logic (store, retrieve, delete, validate) must be encapsulated in a dedicated service component, separate from `PetController` and repository classes. Controllers invoke the service; they do not contain photo logic directly.

**Category:** Maintainability

**Constraint / Target:**
- A `PetPhotoService` (or equivalent) class handles all photo operations.
- `PetController` delegates upload/delete/retrieve to this service.
- No photo-handling code scattered across multiple controllers or JSP scriptlets.

**Verification:**
- Code review confirms single-responsibility separation.
- No photo binary logic present in controller or view layer.

---

### TR-008: Test Coverage

The pet-photo feature must be covered by automated tests at both unit and integration level.

**Category:** Testability

**Constraint / Target:**
- Unit tests for: file validation logic, service-layer upload/replace/delete logic.
- Integration tests for: full upload flow, delete flow, invalid file rejection, cascade delete with pet.
- Tests follow existing Surefire naming convention (`*Tests.java`).

**Verification:**
- `./mvnw test` passes with no failures.
- Test coverage includes at minimum the happy path (upload, display, delete) and two negative paths (oversized file, wrong format).
