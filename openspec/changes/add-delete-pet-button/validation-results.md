# Validation Report

## 1. Validation Summary
**Session name:** docs(cbn-5-review): generate expected artifacts for add-delete-pet-button
**Session id:** <!-- opencode_session_id -->
**Overall result:** Failed
**Requirement coverage:** FR-001, FR-002, TR-001, TR-002, TR-003, TR-004, TR-005, TR-006, TR-007, TR-008

---

## 2. Executive Summary

### 2.1 Result by Area

| Area | Passed | Failed | Blocked | Not testable | Notes |
|---|---:|---:|---:|---:|---|
| Functional | 0 | 2 | 2 | 0 | Delete button absent; delete endpoint returns 404 |
| UI/UX | 0 | 1 | 1 | 0 | No delete button rendered; redirect behavior untestable |
| Data validation | 0 | 0 | 1 | 0 | Hard-delete behavior untestable — endpoint missing |
| Technical | 0 | 1 | 2 | 0 | Endpoint returns 404 for both GET and POST |
| **Total** | **0** | **4** | **6** | **0** | |

### 2.2. Status Legend

| Status | Meaning |
|---|---|
| Passed | The expected behavior was validated successfully. |
| Failed | The behavior was tested and did not meet the requirement. |
| Blocked | Validation could not be completed because another issue prevents execution. |
| Not testable | The requirement could not be validated in the current environment, dataset, or tooling. |
| Passed with issue | The main behavior passed, but a related non-blocking issue was found. |

---

## 3. Validation

### FVAL-001 — Delete button present on owner profile page

| Field | Value |
|---|---|
| Related requirement | FR-001, TR-002, TR-007 |
| Category | Functional |
| Status | Failed |
| Scenario | Navigate to owner profile page (e.g. `/owners/6`) and inspect the pet list for a "Delete" button next to each pet |
| Expected result | A "Delete" button (or form with submit button) is rendered next to each pet entry |
| Actual result | No "Delete" button exists. The pet row only shows "Edit Pet", "Add Visit", and "Transfer Ownership". `document.body.innerHTML.toLowerCase().includes('delete')` returned `false`. |

---

### FVAL-002 — Clicking Delete removes pet and redirects to owner profile

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | Functional |
| Status | Blocked |
| Scenario | Click the "Delete" button next to a pet and verify the pet is removed and the user is redirected to the owner profile page |
| Expected result | Pet is permanently removed; user is redirected to `/owners/{id}`; deleted pet no longer appears in the list |
| Actual result | Blocked — no Delete button exists on the page (see FVAL-001) |

---

### FVAL-003 — Deleted pet no longer appears in pet list

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | Functional |
| Status | Blocked |
| Scenario | After deleting a pet, reload the owner profile page and verify the pet is absent |
| Expected result | The deleted pet does not appear in the "Pets and Visits" table |
| Actual result | Blocked — delete action cannot be triggered (see FVAL-001) |

---

### FVAL-004 — Cascade deletion of associated visit records

| Field | Value |
|---|---|
| Related requirement | FR-002, TR-003, TR-006, TR-008 |
| Category | Functional |
| Status | Blocked |
| Scenario | Delete a pet that has visit records (e.g. pet Max with visits "neutered" and "rabies shot") and verify all visits are removed |
| Expected result | All visit records for the deleted pet are permanently removed; no visits for that pet are accessible |
| Actual result | Blocked — delete action cannot be triggered (see FVAL-001) |

---

### UVAL-001 — Delete button label and placement

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | UI/UX |
| Status | Failed |
| Scenario | Inspect the owner profile page to verify a "Delete" button is rendered next to each pet, consistent with the existing "Edit Pet" / "Add Visit" / "Transfer Ownership" action buttons |
| Expected result | A "Delete" button appears in the pet actions row for each pet |
| Actual result | No "Delete" button is rendered. Only "Edit Pet", "Add Visit", and "Transfer Ownership" are present. |

---

### UVAL-002 — User remains on owner profile page after deletion

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | UI/UX |
| Status | Blocked |
| Scenario | After clicking Delete, verify the browser URL remains at `/owners/{id}` and the page title is "Owner Information" |
| Expected result | User is redirected to and remains on the owner profile page |
| Actual result | Blocked — delete action cannot be triggered (see FVAL-001) |

---

### DVAL-001 — Hard delete — records not retrievable after deletion

| Field | Value |
|---|---|
| Related requirement | TR-008, FR-002 |
| Category | Data validation |
| Status | Blocked |
| Scenario | Delete a pet, then attempt to navigate to the pet's edit page (`/owners/{ownerId}/pets/{petId}/edit`) to confirm the record is gone |
| Expected result | Pet record is physically removed; navigating to the edit page returns 404 or redirects |
| Actual result | Blocked — delete action cannot be triggered (see FVAL-001) |

---

### TVAL-001 — Delete endpoint uses HTTP POST only

| Field | Value |
|---|---|
| Related requirement | TR-007 |
| Category | Technical |
| Status | Failed |
| Scenario | Submit a POST request to the expected delete URL (`/owners/6/pets/8/delete`) and verify it is handled by the server |
| Expected result | POST to delete URL is accepted and processed (redirect 302 or success) |
| Actual result | POST to `/owners/6/pets/8/delete` returns HTTP 404 — endpoint does not exist |

---

### TVAL-002 — GET request to delete URL does not perform deletion

| Field | Value |
|---|---|
| Related requirement | TR-007 |
| Category | Technical |
| Status | Blocked |
| Scenario | Submit a GET request to the delete URL and verify the server returns 405 Method Not Allowed or another non-destructive response |
| Expected result | GET to delete URL returns 405 or redirects without performing deletion |
| Actual result | Blocked — GET to `/owners/6/pets/8/delete` returns 404 (endpoint does not exist at all); cannot distinguish "method not allowed" from "not implemented" |

---

### TVAL-003 — CSRF protection on delete form

| Field | Value |
|---|---|
| Related requirement | TR-002 |
| Category | Technical |
| Status | Blocked |
| Scenario | Inspect the delete form HTML to verify a hidden CSRF token field is present; submit a POST without a CSRF token and verify a 4xx response |
| Expected result | Delete form includes a valid CSRF token; POST without token is rejected with 4xx |
| Actual result | Blocked — no delete form exists on the page (see FVAL-001) |

---

## 4. Findings

| # | Severity | Area | Description |
|---|---|---|---|
| F-001 | Critical | Functional | The "Delete" button is entirely absent from `ownerDetails.jsp`. The feature has not been implemented in the view layer. |
| F-002 | Critical | Backend | The delete endpoint (`/owners/{ownerId}/pets/{petId}/delete`) does not exist — both GET and POST return HTTP 404. No controller mapping has been added. |
| F-003 | Critical | Backend | No `deletePet` service method or repository implementation is present (inferred from 404 on endpoint). |
| F-004 | High | Testing | No controller or service integration tests for the delete feature can be confirmed to exist (feature not implemented). |

---

## 5. Conclusion

**Overall Result: Failed — 0/10 items passed.**

The `add-delete-pet-button` feature has **not been implemented**. The following items are required before this change can proceed to `/cbn-6-archive`:

1. **View** (`ownerDetails.jsp`): Add a "Delete" form/button next to each pet in the "Pets and Visits" table.
2. **Controller** (`PetController` or `OwnerController`): Add a `@PostMapping` endpoint for the delete action.
3. **Service** (`ClinicService` + `ClinicServiceImpl`): Add a `deletePet(Pet pet)` method annotated with `@Transactional`.
4. **Repositories**: Implement `deletePet` in all three backends (`jdbc`, `jpa`, `spring-data-jpa`) with cascade deletion of visits.
5. **Tests**: Add `PetControllerTests.java` and extend `AbstractClinicServiceTests` with delete scenarios.
6. **CSRF**: Ensure the delete form includes a CSRF token.
7. **Logging**: Add `WARN`/`ERROR` log entries for non-existent pet ID and transaction failure scenarios.
