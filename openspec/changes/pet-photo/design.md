## Design
**Session name:** 2026-05-06 13:59:00 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-photo
**Session id:** ses_202e0ebd1ffeoAR5ogs3DzpJvj
**Background and current state:** PetClinic uses plain Spring Framework 7.x (no Spring Boot), WAR packaging on Jetty, JSP views, and three persistence profiles (JPA, JDBC, Spring Data JPA). The `Pet` entity has no photo field. The `createOrUpdatePetForm.jsp` is a plain HTML form with no `enctype="multipart/form-data"`. No multipart resolver is configured anywhere in the application. The database schema has no photo column in any dialect.

## Goals / Non-Goals

**Goals:**
- Store one JPEG/PNG photo (≤ 2 MB) per pet as a BLOB in the database.
- Display the photo on the owner detail page (where pet info already appears) when present; show nothing when absent.
- Allow upload (replace) and explicit delete from the pet edit form.
- Validate file type and size on both client (HTML `accept` + JS) and server sides.
- Cascade-delete the photo when the pet record is deleted.
- Abstract storage behind a `PetPhotoStore` interface to allow future migration.
- Cover all flows with unit and integration tests.

**Non-Goals:**
- Multiple photos per pet (gallery).
- Cloud or filesystem storage (BLOB only for this change).
- A new photo display page (photo served inline via a streaming endpoint).
- Changes to the Visit, Owner, or Vet flows.
- Role-based differentiation beyond existing OWNER/STAFF authentication.

## Decisions

### D-001: Separate `PetPhoto` table (not a BLOB column on `pets`)

**Decision:** Store photo data in a dedicated `pet_photos` table (`id`, `pet_id` FK, `content` BLOB, `content_type` VARCHAR) rather than adding a BLOB column to `pets`.

**Rationale:**
- Keeps the `pets` table row small; BLOB columns degrade query performance even when not selected, because some JDBC drivers buffer the full row.
- Cascade delete is handled naturally by the FK + `CascadeType.ALL` (JPA) or an `ON DELETE CASCADE` constraint (JDBC/DDL).
- The `PetPhoto` entity is a clean extension point for future metadata (file name, upload timestamp, etc.).
- The `PetPhotoStore` abstraction hides the table behind an interface; the storage medium can change without touching controllers.

**Alternatives considered:**
- _BLOB column on `pets`_: simpler DDL, but couples photo size to every pet query.
- _Filesystem path stored in DB_: introduces file-system dependency and complicates cascade delete and rollback.

---

### D-002: `StandardServletMultipartResolver` (not Commons FileUpload)

**Decision:** Enable multipart support via `StandardServletMultipartResolver` configured in `mvc-core-config.xml`, and register a `MultipartConfigElement` (max 2 MB) in `PetclinicInitializer.customizeRegistration()`.

**Rationale:**
- `StandardServletMultipartResolver` uses the Servlet 3.0+ container API — no additional dependency (Commons FileUpload requires `commons-fileupload` on the classpath, which is not present).
- Jetty 12 (used by the Maven plugin) is Servlet 5.0 / Jakarta EE 9+ compatible; the standard resolver works out of the box.
- Size limit enforced at the container level before Spring even sees the request.

**Alternatives considered:**
- _CommonsMultipartResolver_: requires adding `commons-fileupload` dependency; unnecessary given Servlet 3.0+ support.

---

### D-003: Dedicated `PetPhotoService` interface + implementation

**Decision:** Introduce a `PetPhotoService` interface with methods `save(int petId, byte[] data, String contentType)`, `findByPetId(int petId): Optional<PetPhoto>`, and `deleteByPetId(int petId)`. The implementation delegates to `PetPhotoRepository`.

**Rationale:**
- Keeps `PetController` free of persistence logic (TR-007).
- The interface boundary makes unit-testing the controller trivial (mock the service).
- Aligns with the existing `ClinicService` pattern in the codebase.

---

### D-004: Photo served via a dedicated streaming endpoint

**Decision:** Add `GET /owners/{ownerId}/pets/{petId}/photo` to `PetController`. It reads the BLOB from `PetPhotoService` and writes it to `HttpServletResponse` with the stored `content_type` header. The JSP uses `<img src="...photo">` to reference it.

**Rationale:**
- Avoids embedding Base64 in the HTML page (increases page size significantly for a 2 MB image).
- Browser caches the image URL independently of the page.
- Consistent with standard Spring MVC practice for binary responses.

**Alternatives considered:**
- _Base64 `data:` URI in JSP_: no extra endpoint, but bloats the HTML page; poor caching.

---

### D-005: Schema changes applied to all four SQL dialects

**Decision:** Add `pet_photos` table DDL to `db/h2/schema.sql`, `db/mysql/schema.sql`, `db/postgresql/schema.sql`, and `db/hsqldb/schema.sql`.

**Rationale:** The Maven build selects dialect via profile; all four must stay in sync or tests on alternate profiles will fail.

---

### D-006: Client-side validation via HTML `accept` attribute + JavaScript size check

**Decision:** Set `accept="image/jpeg,image/png"` on the file input and add a small inline `<script>` that validates `file.size <= 2097152` before form submit, displaying an inline error and cancelling submission on violation.

**Rationale:**
- No additional JS library needed; the existing JSP already uses WebJars (Bootstrap, Flatpickr) so a small inline script is acceptable.
- Server-side validation (TR-002) remains authoritative; client check is UX-only.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| BLOB storage grows unboundedly as photos accumulate | Out of scope for this change; flagged for future operational review. A `MAX_ALLOWED_PACKET` setting may need tuning in MySQL. |
| Multipart config missing causes silent 400 errors | Integration test covers the upload endpoint; CI will catch misconfiguration early. |
| JDBC profile has no ORM cascade — orphan photo rows possible if pet deleted via raw JDBC | Add `ON DELETE CASCADE` FK constraint to all dialect DDL files so the DB enforces cascade independently of the ORM layer. |
| BLOB column forces full read on every photo request | Mitigated by D-001 (separate table); photo only fetched by the streaming endpoint, never by pet-list queries. |
| `StandardServletMultipartResolver` requires `customizeRegistration()` in `PetclinicInitializer` | Already identified; task list includes this step. |
