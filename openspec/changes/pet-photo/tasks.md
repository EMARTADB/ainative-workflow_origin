## Tasks
### Definition phase
**Session name:** 2026-05-06 13:59:00 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-photo
**Session id:** ses_202e0ebd1ffeoAR5ogs3DzpJvj
-----------
### Apply/Construction phase
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->

## 1. Database Schema

- [x] 1.1 Add `pet_photos` table DDL (columns: `id`, `pet_id` FK → `pets(id)` ON DELETE CASCADE, `content` BLOB/BYTEA/LONGBLOB, `content_type` VARCHAR(50)) to `db/h2/schema.sql`
- [x] 1.2 Add `pet_photos` table DDL to `db/mysql/schema.sql`
- [x] 1.3 Add `pet_photos` table DDL to `db/postgresql/schema.sql`
- [x] 1.4 Add `pet_photos` table DDL to `db/hsqldb/schema.sql`

## 2. Domain Model

- [x] 2.1 Create `model/PetPhoto.java` entity (`@Entity`, `@Table(name = "pet_photos")`) with fields: `id` (int), `petId` (int FK), `content` (byte[]), `contentType` (String)
- [x] 2.2 Add `@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "petId") PetPhoto photo` association to `model/Pet.java` (JPA profile)

## 3. Storage Abstraction

- [x] 3.1 Create `repository/PetPhotoStore.java` interface with methods: `save(int petId, byte[] content, String contentType)`, `findByPetId(int petId): Optional<PetPhoto>`, `deleteByPetId(int petId)`
- [x] 3.2 Create `repository/jpa/JpaPetPhotoStoreImpl.java` implementing `PetPhotoStore` using `EntityManager`
- [x] 3.3 Create `repository/jdbc/JdbcPetPhotoStoreImpl.java` implementing `PetPhotoStore` using `NamedParameterJdbcTemplate`
- [x] 3.4 Create `repository/springdatajpa/SpringDataPetPhotoRepository.java` extending `JpaRepository<PetPhoto, Integer>` and `PetPhotoStore`
- [x] 3.5 Register the appropriate `PetPhotoStore` bean in `business-config.xml` for each Spring profile (jpa, jdbc, spring-data-jpa)

## 4. Service Layer

- [x] 4.1 Create `service/PetPhotoService.java` interface with methods: `save(int petId, byte[] content, String contentType)`, `findByPetId(int petId): Optional<PetPhoto>`, `deleteByPetId(int petId)`
- [x] 4.2 Create `service/PetPhotoServiceImpl.java` implementing `PetPhotoService`, delegating to `PetPhotoStore`
- [x] 4.3 Register `PetPhotoServiceImpl` as a Spring bean in `business-config.xml`

## 5. Multipart Configuration

- [x] 5.1 Override `customizeRegistration(Dynamic registration)` in `PetclinicInitializer.java` to add `MultipartConfigElement` with max file size 2 MB and max request size 2 MB
- [x] 5.2 Add `<bean class="org.springframework.web.multipart.support.StandardServletMultipartResolver" id="multipartResolver"/>` to `mvc-core-config.xml`

## 6. Controller

- [x] 6.1 Inject `PetPhotoService` into `PetController` (constructor injection)
- [x] 6.2 Add `POST /owners/{ownerId}/pets/{petId}/photo` endpoint: accept `@RequestParam("photo") MultipartFile`, validate type (JPEG/PNG) and size (≤ 2 MB) server-side, call `petPhotoService.save(...)`, redirect to owner page; return 400 with error message on validation failure
- [x] 6.3 Add `POST /owners/{ownerId}/pets/{petId}/photo/delete` endpoint: call `petPhotoService.deleteByPetId(petId)`, redirect to owner page
- [x] 6.4 Add `GET /owners/{ownerId}/pets/{petId}/photo` endpoint: stream photo bytes with stored `Content-Type` header; return 404 if no photo exists

## 7. Views

- [x] 7.1 Update `createOrUpdatePetForm.jsp`: add `enctype="multipart/form-data"` to `<form:form>`, add `<input type="file" name="photo" accept="image/jpeg,image/png">`, add inline JS to validate file size ≤ 2 MB before submit and display an error message on violation
- [x] 7.2 Update `ownerDetails.jsp`: for each pet, conditionally render `<img src="/owners/{ownerId}/pets/{petId}/photo">` only when a photo exists (check via model attribute or a helper tag)
- [x] 7.3 Add delete photo button/link in `ownerDetails.jsp` or `createOrUpdatePetForm.jsp` that posts to the delete endpoint (visible only when a photo exists)

## 8. Unit Tests

- [x] 8.1 Create `web/PetPhotoControllerTests.java`: test upload with valid file (200/redirect), upload with wrong MIME type (400), upload with oversized file (400), unauthenticated upload (401/redirect), delete (redirect), streaming endpoint returns bytes and content-type
- [x] 8.2 Create `service/PetPhotoServiceTests.java`: test save/retrieve/delete delegation to `PetPhotoStore` mock

## 9. Integration Tests

- [x] 9.1 Create `service/ClinicServicePetPhotoTests.java` (or extend an existing abstract test): test full upload flow, delete flow, and cascade delete (create pet → upload photo → delete pet → assert no orphan in `pet_photos`)
- [x] 9.2 Verify `./mvnw test` passes on H2 profile with all new tests green
