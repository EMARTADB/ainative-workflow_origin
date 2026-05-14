## Tasks
### Definition phase
**Session name:** docs(cbn-3-definition): generate artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->
-----------
### Apply/Construction phase
**Session name:**<!-- opencode_session_name -->
**Session id:** <!-- opencode_session_id -->

## 1. Model Layer

- [ ] 1.1 Verify `Pet` entity `@OneToMany` on `visits` has `cascade = CascadeType.ALL` and `orphanRemoval = true` (JPA backend cascade requirement)

## 2. Repository Layer — JPA Backend

- [ ] 2.1 Add `deletePet(Pet pet)` method to `PetRepository` interface (JPA)
- [ ] 2.2 Implement `deletePet` in the JPA `PetRepository` using `EntityManager.remove(pet)` (cascade to visits via JPA)

## 3. Repository Layer — JDBC Backend

- [ ] 3.1 Add `deletePet(Pet pet)` method to the JDBC `PetRepository` implementation
- [ ] 3.2 Implement JDBC `deletePet`: execute `DELETE FROM visits WHERE pet_id = ?` then `DELETE FROM pets WHERE id = ?` within the same transaction

## 4. Repository Layer — Spring Data JPA Backend

- [ ] 4.1 Add `deletePet(Pet pet)` method to the Spring Data JPA `PetRepository` interface
- [ ] 4.2 Implement Spring Data JPA `deletePet` by delegating to `petRepository.delete(pet)` (inherits JPA cascade)

## 5. Service Layer

- [ ] 5.1 Add `deletePet(Pet pet)` method signature to the `ClinicService` interface
- [ ] 5.2 Implement `deletePet` in `ClinicServiceImpl` annotated with `@Transactional`
- [ ] 5.3 Add WARN-level log in `deletePet` when the pet ID does not exist in the database
- [ ] 5.4 Add ERROR-level log in `deletePet` when the delete transaction fails due to an unexpected exception

## 6. Controller Layer

- [ ] 6.1 Add `@PostMapping("/owners/{ownerId}/pets/{petId}/delete")` handler method to `PetController`
- [ ] 6.2 Handler calls `clinicService.deletePet(pet)` and redirects to `/owners/{ownerId}` on success
- [ ] 6.3 Handler emits WARN log and redirects gracefully when pet ID does not exist (no 500 error)

## 7. View Layer

- [ ] 7.1 Add a delete form/button next to each pet in `src/main/webapp/WEB-INF/jsp/owners/ownerDetails.jsp`
- [ ] 7.2 Ensure the delete form uses HTTP POST and includes the CSRF token as a hidden field

## 8. Tests — Controller

- [ ] 8.1 Create `PetControllerTests.java` (name must end in `Tests` for Surefire)
- [ ] 8.2 Test: DELETE endpoint returns redirect to owner profile on success
- [ ] 8.3 Test: DELETE endpoint returns appropriate error response when pet does not exist
- [ ] 8.4 Test: DELETE endpoint rejects POST without a valid CSRF token (returns 4xx, no deletion)
- [ ] 8.5 Test: GET request to delete URL does not perform deletion

## 9. Tests — Service Integration

- [ ] 9.1 Add `testDeletePet` test to `AbstractClinicServiceTests`: assert pet is removed after deletion
- [ ] 9.2 Add `testDeletePetCascadesVisits` test to `AbstractClinicServiceTests`: assert all visits for the pet are removed after deletion
- [ ] 9.3 Add `testDeletePetNotFound` test to `AbstractClinicServiceTests`: assert WARN log is emitted for non-existent pet ID
- [ ] 9.4 Verify all new service tests pass for all three backends (`jpa`, `jdbc`, `spring-data-jpa`)

## 10. Verification

- [ ] 10.1 Run `./mvnw test` and confirm all new test classes are picked up by Surefire and pass
- [ ] 10.2 Run `./mvnw jetty:run-war` and manually verify the delete button appears and works end-to-end
- [ ] 10.3 Run `./mvnw verify` (CI equivalent) and confirm no failures across all backends
