## Technical Requirements
**Session name:** docs(cbn-2-technical-requirements): generate expected artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->
**Kind of change:** fix

### TR-001: Delete Operation Response Time

The delete action (removing a pet and all its associated visit records) must complete and return the refreshed owner profile page within an acceptable time for a synchronous, single-record operation.

**Category:** Performance

**Constraint / Target:**
- End-to-end response time for the delete request (from form submission to page reload) must be < 500ms at the 95th percentile under normal load conditions.

**Verification:**
- Manual timing during local development with H2 in-memory database.
- If a load-testing step is added to CI in the future, this target must be met at p95.

---

### TR-002: CSRF Protection on Delete Form

The delete form must include a CSRF token to prevent cross-site request forgery attacks. This is consistent with the existing form-based MVC pattern and protects against malicious third-party pages triggering unintended deletions.

**Category:** Security

**Constraint / Target:**
- The HTML form that triggers the delete action must include a valid CSRF token as a hidden field.
- The server must reject any POST request to the delete endpoint that does not carry a valid CSRF token.

**Verification:**
- Code review confirms the CSRF token is present in the JSP form template.
- Automated controller test verifies that a POST without a CSRF token returns an error (4xx) and does not perform deletion.

---

### TR-003: Atomic Cascade Deletion (Transactional Integrity)

The deletion of a pet and all its associated visit records must be executed as a single atomic database transaction. No partial state (pet deleted but visits remaining, or vice versa) is acceptable.

**Category:** Reliability

**Constraint / Target:**
- If any part of the delete operation fails (e.g., a database constraint violation or connection error), the entire transaction must roll back, leaving both the pet and its visit records unchanged.
- The operation must be annotated with `@Transactional` (or equivalent XML transaction configuration) in the service layer.

**Verification:**
- Service integration test simulates a failure mid-deletion and asserts that neither the pet nor its visits are removed.
- Code review confirms `@Transactional` is applied at the service method level.

---

### TR-004: Structured Logging for Delete Failures and Invalid Requests

The delete operation must produce structured log output for error and warning conditions to aid in diagnosing production issues without adding noise for normal successful operations.

**Category:** Operability / Observability

**Constraint / Target:**
- A `WARN`-level log entry must be emitted when a delete is attempted for a pet ID that does not exist in the database.
- An `ERROR`-level log entry must be emitted if the delete transaction fails (e.g., due to a database error or unexpected exception).
- No log entry is required for successful deletions.

**Verification:**
- Unit/controller test asserts that the appropriate log level is triggered when a non-existent pet ID is submitted.
- Code review confirms logger calls are present in the service or controller for the specified conditions.

---

### TR-005: Test Coverage — Controller and Service Integration Tests

The delete feature must be covered by automated tests following the project's existing test conventions to prevent regressions.

**Category:** Testability

**Constraint / Target:**
- A controller test class named `*Tests.java` (Surefire-compatible) must verify:
  - The delete endpoint returns a redirect to the owner profile page on success.
  - The delete endpoint returns an appropriate error response when the pet does not exist.
  - The delete endpoint rejects requests without a valid CSRF token.
- A service integration test extending `AbstractClinicServiceTests` must verify:
  - Deleting a pet removes the pet record from the database.
  - Deleting a pet removes all associated visit records from the database.
  - The test must pass for all three persistence backends: `jpa`, `jdbc`, and `spring-data-jpa`.

**Verification:**
- `./mvnw test` passes with all new test classes included.
- Tests are confirmed to run under Surefire (file names end in `Tests.java`).

---

### TR-006: Cross-Backend Compatibility

The delete feature must be implemented in a way that works correctly across all three supported persistence backends without requiring backend-specific workarounds.

**Category:** Maintainability

**Constraint / Target:**
- The `deletePet(Pet pet)` method (or equivalent) must be implemented in the `ClinicService` interface and in all three repository implementations: `jdbc`, `jpa`, and `spring-data-jpa`.
- Cascade deletion of visits must be handled at the persistence layer (e.g., via JPA `CascadeType.ALL` / `orphanRemoval = true` for the JPA backend, and explicit SQL `DELETE` for the JDBC backend).
- Switching Spring profiles (`-Dspring.profiles.active=jdbc`, `jpa`, `spring-data-jpa`) must not break the delete functionality.

**Verification:**
- Service integration tests run against all three backends and pass.
- CI pipeline (`./mvnw -B verify`) passes with no backend-specific failures.

---

### TR-007: HTTP Method Constraint for Delete Action

The delete action must use HTTP POST to comply with HTTP semantics for destructive operations and to prevent accidental deletion via browser pre-fetching or link crawling.

**Category:** Interoperability

**Constraint / Target:**
- The delete action must be triggered exclusively via an HTTP POST form submission.
- No HTTP GET endpoint for deletion is permitted.
- No REST API endpoint (e.g., `DELETE /pets/{id}`) is required or permitted as part of this change.

**Verification:**
- Code review confirms the controller maps the delete action to `@PostMapping` (or equivalent XML mapping).
- Controller test verifies that a GET request to the delete URL does not perform deletion.

---

### TR-008: Hard Delete — No Soft-Delete or Audit Trail

Pet and visit records must be permanently and physically removed from the database when deleted. No soft-delete mechanism (e.g., a `deleted` flag or `deleted_at` timestamp) is required for this change.

**Category:** Compliance / Privacy

**Constraint / Target:**
- The delete operation must issue physical `DELETE` SQL statements (or JPA `EntityManager.remove()`) for both the pet and its associated visits.
- No `deleted`, `active`, or `archived` column is to be introduced as part of this change.
- Deleted records must not be retrievable through any application query after deletion.

**Verification:**
- Service integration test confirms that querying for the deleted pet ID returns null/empty after deletion.
- Service integration test confirms that querying for visits belonging to the deleted pet returns an empty list after deletion.
- Code review confirms no soft-delete columns or flags are introduced.
