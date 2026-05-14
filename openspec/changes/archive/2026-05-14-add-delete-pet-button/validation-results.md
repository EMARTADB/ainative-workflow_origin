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
| Functional | 0 | 2 | 2 | 0 | Delete button absent from rendered page; endpoint returns 404 |
| UI/UX | 0 | 1 | 1 | 0 | Delete button not rendered; post-delete navigation untestable |
| Data validation | 0 | 0 | 2 | 0 | Deletion blocked by missing endpoint |
| Technical | 0 | 1 | 3 | 0 | Endpoint 404; CSRF, method constraint, cascade untestable |
| **Total** | **0** | **4** | **8** | **0** | |

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

### FVAL-001 — Delete button visible on owner profile page

| Field | Value |
|---|---|
| Related requirement | FR-001, spec: "Delete pet via owner profile page" |
| Category | Functional |
| Status | Failed |
| Scenario | Navigate to `/owners/1`; inspect rendered HTML for a "Delete Pet" button next to each pet |
| Expected result | A "Delete Pet" button (form submit) rendered next to each pet in the Pets and Visits table |
| Actual result | No delete button or form present in the rendered HTML. The page shows only "Edit Pet", "Add Visit", and "Transfer Ownership" links. `document.body.innerHTML.includes('Delete')` returned `false`. The JSP source (`ownerDetails.jsp` lines 118–128) contains the delete form code, but the running WAR does not reflect it — indicating the application was not rebuilt after the JSP change was made. |

---

### FVAL-002 — Clicking Delete removes pet and redirects to owner profile

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | Functional |
| Status | Blocked |
| Scenario | Click the "Delete Pet" button next to a pet on the owner profile page |
| Expected result | Pet is permanently removed; user is redirected to the owner profile page; deleted pet no longer listed |
| Actual result | Blocked — the Delete button is not rendered (see FVAL-001). Cannot execute the click action. |

---

### FVAL-003 — Deleted pet no longer appears in pet list

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | Functional |
| Status | Blocked |
| Scenario | After clicking Delete, verify the pet is absent from the owner's profile page |
| Expected result | The deleted pet does not appear in the Pets and Visits table |
| Actual result | Blocked — deletion cannot be triggered (see FVAL-001). |

---

### FVAL-004 — Cascade deletion of associated visits

| Field | Value |
|---|---|
| Related requirement | FR-002, TR-003 |
| Category | Functional |
| Status | Blocked |
| Scenario | Delete a pet that has visit records; verify visits are also removed |
| Expected result | All visit records for the deleted pet are permanently removed |
| Actual result | Blocked — deletion cannot be triggered (see FVAL-001). |

---

### UVAL-001 — Delete button placement next to each pet

| Field | Value |
|---|---|
| Related requirement | FR-001, spec: "Delete pet via owner profile page" |
| Category | UI/UX |
| Status | Failed |
| Scenario | Inspect the owner profile page layout for the Delete button position |
| Expected result | A "Delete Pet" button rendered inline next to each pet row, alongside "Edit Pet", "Add Visit", and "Transfer Ownership" |
| Actual result | No Delete button rendered. The fourth table cell that should contain the delete form is absent from the rendered HTML. The JSP source has the correct markup but the deployed WAR does not include it. |

---

### UVAL-002 — Owner profile page URL and navigation after delete

| Field | Value |
|---|---|
| Related requirement | FR-001 |
| Category | UI/UX |
| Status | Blocked |
| Scenario | After deletion, verify the user remains on `/owners/{ownerId}` and the page refreshes correctly |
| Expected result | Redirect to `/owners/{ownerId}` after successful deletion |
| Actual result | Blocked — deletion cannot be triggered (see FVAL-001). |

---

### DVAL-001 — Pet record not retrievable after deletion

| Field | Value |
|---|---|
| Related requirement | TR-008, FR-001 |
| Category | Data validation |
| Status | Blocked |
| Scenario | After deleting a pet, navigate to the owner profile and confirm the pet is absent |
| Expected result | The deleted pet ID is not listed anywhere in the application |
| Actual result | Blocked — deletion cannot be triggered (see FVAL-001). |

---

### DVAL-002 — Visit records not retrievable after pet deletion

| Field | Value |
|---|---|
| Related requirement | FR-002, TR-008 |
| Category | Data validation |
| Status | Blocked |
| Scenario | After deleting a pet with visits, verify no visits for that pet appear in the application |
| Expected result | Zero visit records for the deleted pet are accessible |
| Actual result | Blocked — deletion cannot be triggered (see FVAL-001). |

---

### TVAL-001 — Delete endpoint uses HTTP POST only (TR-007)

| Field | Value |
|---|---|
| Related requirement | TR-007 |
| Category | Technical |
| Status | Failed |
| Scenario | Issue a POST to `/owners/1/pets/1/delete`; then issue a GET to the same URL |
| Expected result | POST: redirect to owner profile (2xx/3xx). GET: 405 Method Not Allowed (no deletion performed) |
| Actual result | Both POST and GET return **HTTP 404**. The endpoint does not exist in the running application. Source code confirms `@PostMapping(value = "/pets/{petId}/delete")` is present in `PetController.java` (line 174), but the WAR has not been rebuilt. |

---

### TVAL-002 — CSRF token present in delete form (TR-002)

| Field | Value |
|---|---|
| Related requirement | TR-002 |
| Category | Technical |
| Status | Blocked |
| Scenario | Inspect the delete form HTML for a hidden CSRF token input field |
| Expected result | `<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>` rendered in the form |
| Actual result | Blocked — the delete form is not rendered in the browser. Source code review confirms the CSRF hidden input is present in `ownerDetails.jsp` line 124, but the running WAR does not serve it. |

---

### TVAL-003 — Delete endpoint rejects POST without CSRF token (TR-002)

| Field | Value |
|---|---|
| Related requirement | TR-002 |
| Category | Technical |
| Status | Blocked |
| Scenario | Submit a POST to the delete endpoint without a CSRF token; expect 4xx response |
| Expected result | Server returns 4xx (e.g., 403 Forbidden) and does not delete the pet |
| Actual result | Blocked — endpoint returns 404 (not deployed). Cannot test CSRF rejection. |

---

### TVAL-004 — Delete response time < 500ms (TR-001)

| Field | Value |
|---|---|
| Related requirement | TR-001 |
| Category | Technical |
| Status | Blocked |
| Scenario | Time the round-trip from delete form submission to page reload |
| Expected result | End-to-end response time < 500ms |
| Actual result | Blocked — endpoint not available (404). |

---

## 4. Findings

| # | Severity | Area | Finding | Recommendation |
|---|---|---|---|---|
| F-001 | Critical | Deployment | The running application at `http://localhost:8080` does not include the delete pet feature despite source code changes being present. The JSP (`ownerDetails.jsp`) has the delete form and the controller (`PetController.java`) has the `@PostMapping` for `/pets/{petId}/delete`, but the WAR was not rebuilt. POST and GET to `/owners/1/pets/1/delete` both return HTTP 404. | Rebuild the WAR with `./mvnw verify -DskipTests` and restart the application before re-running validation. |
| F-002 | High | UI/UX | The delete button is absent from the rendered owner profile page. All 10 owners checked show only "Edit Pet", "Add Visit", and "Transfer Ownership" — no "Delete Pet" button. | Rebuild and redeploy. After rebuild, verify the JSP renders the fourth action cell with the delete form. |
| F-003 | Informational | Code quality | Source code review confirms the implementation is structurally complete: `deletePet()` is defined in `ClinicService` interface, implemented in all three repository backends (`JpaPetRepositoryImpl`, `JdbcPetRepositoryImpl`, `SpringDataPetRepository`), the controller endpoint uses `@PostMapping`, and the JSP includes the CSRF hidden input. The feature appears correct in source — only the deployment is stale. | No code changes needed; rebuild only. |

---

## 5. Conclusion

**Overall Result: Failed**
**Score: 0/12**

The validation session could not confirm any functional requirement because the running application does not include the deployed changes. The root cause is a stale WAR: source code changes (JSP view, controller endpoint, service layer) are present in the repository but the application was not rebuilt before validation.

**Items requiring rework before re-validation:**
1. **[Deployment]** Rebuild the WAR (`./mvnw verify -DskipTests`) and restart the application.
2. **[Re-run all validations]** All 12 validation items (FVAL-001–004, UVAL-001–002, DVAL-001–002, TVAL-001–004) must be re-executed after the rebuild.

**Not ready for `/cbn-6-archive`** — re-run validation after rebuilding and redeploying the application.
