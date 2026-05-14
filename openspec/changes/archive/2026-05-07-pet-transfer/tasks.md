## Tasks
### Definition phase
**Session name:** 2026-05-06 19:37:18 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-transfer
**Session id:** ses_201a27cdfffeqS0McnCMut5SMb
-----------
### Apply/Construction phase
**Session name:**2026-05-06 19:42:00 - feat(cbn-4-construction.md): implement tasks for pet-transfer
**Session id:** ses_2019e43cfffebpP2vo5EU44N5j

## 1. Database Schema

- [x] 1.1 Create `pet_transfers` DDL for H2 (test schema): columns `id`, `pet_id`, `from_owner_id`, `to_owner_id`, `transferred_at`, `performed_by`; FK RESTRICT constraints on `pet_id`, `from_owner_id`, `to_owner_id`
- [x] 1.2 Create `pet_transfers` DDL for MySQL dialect
- [x] 1.3 Create `pet_transfers` DDL for PostgreSQL dialect
- [x] 1.4 Verify FK RESTRICT prevents owner/pet deletion when transfer records exist (manual + integration test)

## 2. Domain Model

- [x] 2.1 Create `PetTransfer` entity/model class with fields: `id`, `petId`, `fromOwnerId`, `toOwnerId`, `transferredAt`, `performedBy`
- [x] 2.2 Annotate `PetTransfer` for JPA (`@Entity`, `@Table`, mappings)
- [x] 2.3 Ensure `PetTransfer` has no delete method exposed anywhere in the model

## 3. Repository Layer

- [x] 3.1 Define `PetTransferRepository` interface in `org.springframework.samples.petclinic.repository` with `save(PetTransfer)` and `findByPetId(int petId)` methods — no delete methods
- [x] 3.2 Implement `JpaPetTransferRepositoryImpl` under `repository/jpa/`
- [x] 3.3 Implement `JdbcPetTransferRepositoryImpl` under `repository/jdbc/` using `SimpleJdbcInsert` or named parameter template
- [x] 3.4 Implement `SpringDataJpaPetTransferRepository` under `repository/springdatajpa/` extending `CrudRepository`
- [x] 3.5 Update `business-config.xml` to wire the correct `PetTransferRepository` implementation per active Spring profile (`jpa`, `jdbc`, `spring-data-jpa`)

## 4. Service Layer

- [x] 4.1 Add `transferPet(int petId, int newOwnerId, String performedBy)` method signature to `ClinicService` interface
- [x] 4.2 Implement `transferPet(...)` in `ClinicServiceImpl` with `@Transactional`
- [x] 4.3 Implement self-transfer validation: throw `PetTransferException(SELF_TRANSFER)` if `newOwnerId == pet.getOwner().getId()`
- [x] 4.4 Implement pending-visits validation: throw `PetTransferException(PENDING_VISITS)` if any visit is active/scheduled
- [x] 4.5 Create `PetTransferException` domain exception class with reason codes `SELF_TRANSFER` and `PENDING_VISITS`
- [x] 4.6 Add SLF4J INFO log on successful transfer (`pet_id`, `from_owner_id`, `to_owner_id`, `performed_by`)
- [x] 4.7 Add SLF4J WARN log on blocked transfer (`pet_id`, blocking reason)
- [x] 4.8 Add SLF4J ERROR log on unexpected exception during transfer (full stack trace)

## 5. Web Layer

- [x] 5.1 Create `PetTransferController` with GET `/pets/{petId}/transfer` — loads transfer form (pet + current owner info, owner search)
- [x] 5.2 Create POST `/pets/{petId}/transfer` — validates input, renders confirmation page with pet name, current owner, new owner
- [x] 5.3 Create POST `/pets/{petId}/transfer/confirm` — calls `ClinicService.transferPet(...)`, redirects to pet detail on success, re-renders form with error on failure
- [x] 5.4 Protect all transfer URLs via the existing authentication interceptor/filter (TR-003)
- [x] 5.5 Wire `PetTransferController` into `mvc-core-config.xml` component scan (or register explicitly)

## 6. JSP Views

- [x] 6.1 Create `transferForm.jsp`: shows pet name, current owner; owner search field; submit button to proceed to confirmation
- [x] 6.2 Create `transferConfirm.jsp`: shows pet name, current owner, selected new owner; Confirm and Cancel buttons
- [x] 6.3 Add "Transfer Ownership" link/button to the pet detail JSP (`petDetails.jsp` or equivalent)
- [x] 6.4 Ensure error messages for SELF_TRANSFER and PENDING_VISITS are rendered clearly in the form view

## 7. Unit Tests

- [x] 7.1 Create `ClinicServiceTransferTests` (named `*Tests.java` for Surefire): happy path transfer — verify `pet.owner_id` updated and `PetTransfer` record saved
- [x] 7.2 Unit test: SELF_TRANSFER validation — assert `PetTransferException` thrown, no DB writes
- [x] 7.3 Unit test: PENDING_VISITS validation — assert `PetTransferException` thrown, no DB writes
- [x] 7.4 Unit test: rollback-on-audit-failure — mock `PetTransferRepository.save(...)` to throw `RuntimeException`; assert `pet.owner_id` is unchanged
- [x] 7.5 Create `PetTransferControllerTests`: unauthenticated GET/POST returns 401 or redirect to login (TR-003)
- [x] 7.6 Controller test: SELF_TRANSFER error message rendered in form view
- [x] 7.7 Controller test: PENDING_VISITS error message rendered in form view

## 8. Integration Tests

- [x] 8.1 Add transfer integration tests to (or extending) `AbstractClinicServiceTests`: happy path, self-transfer block, pending-visits block
- [x] 8.2 Verify integration tests pass with `jpa` profile (`./mvnw test`)
- [x] 8.3 Verify integration tests pass with `jdbc` profile (`./mvnw test -Dspring.profiles.active=jdbc`)
- [x] 8.4 Verify integration tests pass with `spring-data-jpa` profile (`./mvnw test -Dspring.profiles.active=spring-data-jpa`)
- [x] 8.5 Integration test: assert INFO log entry on successful transfer (log appender spy or test logger)
- [x] 8.6 Integration test: assert FK constraint violation when attempting to delete an owner with existing transfer records

## 9. Full Build Verification

- [x] 9.1 Run `./mvnw verify` (default JPA profile) — all tests pass, no compilation errors
- [ ] 9.2 Run `./mvnw jetty:run-war` and smoke-test the transfer flow end-to-end in the browser (H2 in-memory)
- [ ] 9.3 Smoke-test with MySQL profile (`./mvnw jetty:run-war -P MySQL`) to verify schema and repository work
