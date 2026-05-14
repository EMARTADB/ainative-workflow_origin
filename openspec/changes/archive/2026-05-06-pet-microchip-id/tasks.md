## Tasks
### Definition phase
**Session name:** 2026-05-06 15:23:30 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-microchip-id
**Session id:** ses_2028ad7fbffePuOwT3812K1vNN
-----------
### Apply/Construction phase
**Session name:**2026-05-06 15:29:31 - feat(cbn-4-construction.md): implement tasks for pet-microchip-id
**Session id:** ses_202856704ffefhaaHQ899K97V6

## 1. Database Schema

- [x] 1.1 Add `microchip_id VARCHAR(15) UNIQUE` column to `CREATE TABLE pets` in `src/main/resources/db/h2/schema.sql`
- [x] 1.2 Add `microchip_id VARCHAR(15) UNIQUE` column to `CREATE TABLE pets` in `src/main/resources/db/hsqldb/schema.sql`
- [x] 1.3 Add `microchip_id VARCHAR(15) UNIQUE` column to `CREATE TABLE pets` in `src/main/resources/db/mysql/schema.sql`
- [x] 1.4 Add `microchip_id VARCHAR(15) UNIQUE` column to `CREATE TABLE pets` in `src/main/resources/db/postgresql/schema.sql`

## 2. Domain Model

- [x] 2.1 Add `microchipId` field to `Pet.java` with `@Column(name = "microchip_id")` and `@Pattern(regexp = "^\\d{15}$")` (nullable — annotation only fires on non-null per JSR-380 default)
- [x] 2.2 Add getter `getMicrochipId()` and setter `setMicrochipId(String)` to `Pet.java`

## 3. Repository Layer

- [x] 3.1 Add `Owner findOwnerByPetMicrochipId(String microchipId)` to `OwnerRepository` interface
- [x] 3.2 Implement the method in `JpaOwnerRepositoryImpl` using JPQL: `SELECT o FROM Owner o JOIN o.pets p WHERE p.microchipId = :microchipId`
- [x] 3.3 Implement the method in `JdbcOwnerRepositoryImpl` using a JOIN SQL query on `pets.microchip_id`
- [x] 3.4 Add `findOwnerByMicrochipId(String microchipId)` derived query method to `SpringDataOwnerRepository` interface
- [x] 3.5 Update `JdbcPetRepositoryImpl` SQL INSERT to include `microchip_id` column in the insert statement
- [x] 3.6 Update `JdbcPetRepositoryImpl` SQL UPDATE to include `microchip_id = :microchipId`
- [x] 3.7 Update `JdbcPetRepositoryImpl` `RowMapper` (or `BeanPropertyRowMapper`) to map `microchip_id` → `microchipId`

## 4. Service Layer

- [x] 4.1 Add `Owner findOwnerByPetMicrochipId(String microchipId)` to `ClinicService` interface
- [x] 4.2 Implement the method in `ClinicServiceImpl` delegating to `ownerRepository.findOwnerByPetMicrochipId()`

## 5. Web Layer — Pet Form (Add / Edit)

- [x] 5.1 Add `@InitBinder` in `PetController` registering `StringTrimmerEditor(true)` to coerce empty microchipId → null
- [x] 5.2 In `PetController.processCreationForm()` and `processUpdateForm()`, catch `DataIntegrityViolationException` from `clinicService.savePet()` and add a `BindingResult` field error on `microchipId` with message "This microchip ID is already registered to another pet"
- [x] 5.3 Add `microchipId` input to `src/main/webapp/WEB-INF/jsp/pets/createOrUpdatePetForm.jsp` with label "Microchip ID" and Spring form binding (`<form:input path="microchipId">`) including error display (`<form:errors path="microchipId">`)

## 6. Web Layer — Owner / Pet Display Views

- [x] 6.1 Add read-only `microchipId` display row to `src/main/webapp/WEB-INF/jsp/owners/ownerDetails.jsp` pet section — show "—" when null
- [x] 6.2 Add read-only `microchipId` display to `src/main/webapp/WEB-INF/jsp/owners/ownersList.jsp` inline pet attributes — show "—" when null

## 7. Web Layer — Find Owners (Microchip Search)

- [x] 7.1 Add a dedicated "Microchip ID" input field to `src/main/webapp/WEB-INF/jsp/owners/findOwners.jsp`
- [x] 7.2 Update `OwnerController.processFindForm()` to detect a populated `microchipId` parameter: if present, call `clinicService.findOwnerByPetMicrochipId()` and redirect to owner detail or show empty-state; if absent or invalid format, show validation hint
- [x] 7.3 Ensure the `Owner` command object (or form model) used by `OwnerController` carries a `microchipId` field for binding

## 8. Tests — Service Integration

- [x] 8.1 Add `testFindOwnerByMicrochipId()` to `AbstractClinicServiceTests` — saves a pet with microchipId, calls `findOwnerByPetMicrochipId()`, asserts correct owner returned
- [x] 8.2 Add `testMicrochipIdUniquenessViolation()` to `AbstractClinicServiceTests` — saves two pets with the same microchipId, asserts a `DataIntegrityViolationException` (or wrapped exception) is thrown
- [x] 8.3 Add `testSavePetWithNullMicrochipId()` to `AbstractClinicServiceTests` — saves pet with null microchipId, verifies success and null value persisted

## 9. Tests — Controller (MVC)

- [x] 9.1 Add test to `PetControllerTests` for valid microchipId on add-pet form — asserts redirect (no form error)
- [x] 9.2 Add test to `PetControllerTests` for invalid-format microchipId — asserts form re-displayed with field error on `microchipId`
- [x] 9.3 Add test to `PetControllerTests` for empty microchipId — asserts null coercion and successful save
- [x] 9.4 Add test to `PetControllerTests` for duplicate microchipId — mock `savePet()` to throw `DataIntegrityViolationException`, assert form re-displayed with duplicate-error message
- [x] 9.5 Add test to `OwnerControllerTests` for microchip search — valid 15-digit match, assert redirect to owner detail
- [x] 9.6 Add test to `OwnerControllerTests` for microchip search — no match, assert "no owners found" view
- [x] 9.7 Add test to `OwnerControllerTests` for microchip search — invalid format, assert validation hint shown

## 10. Verification

- [x] 10.1 Run `./mvnw test` and confirm zero failures
- [x] 10.2 Run `./mvnw jetty:run-war` (H2/jpa default) and manually verify add/edit pet form, display on owner pages, and microchip search
- [x] 10.3 Run `./mvnw jetty:run-war -Dspring.profiles.active=jdbc` and smoke-test microchip field persistence and search
- [x] 10.4 Run `./mvnw jetty:run-war -Dspring.profiles.active=spring-data-jpa` and smoke-test microchip field persistence and search
