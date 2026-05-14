## Functional Requirements
**Session name:** 2026-05-06 14:22:13 - docs(cbn-1-functional-requirements): generate artifact functional-requirements.md for pet-microchip-id
**Session id:** ses_202c33881ffeEx7bzENZzR2JrL
**Kind of change:** small-change

---

### FR-001: Add microchipId field to Pet

Clinic staff must be able to record an official microchip ID on any pet profile. The field is optional — not all pets have a microchip — but when provided it must be a valid 15-digit numeric string conforming to ISO 11784/11785. The microchipId is stored as an attribute of the `Pet` entity.

The field must be present and editable on the **Add Pet** and **Edit Pet** forms. It must also be visible (read-only) on the **pet detail view** and on the **owner summary page** alongside other pet attributes.

All clinic staff roles have permission to read and write this field — no additional role restriction applies.

#### Acceptance Criteria
- **GIVEN** a staff member opens the Add Pet or Edit Pet form
- **WHEN** they enter a 15-digit numeric microchip ID
- **THEN** the value is saved and displayed on the pet detail view and owner summary page
- **AND** no error is shown

---

### FR-002: microchipId format validation

Any value entered for microchipId must match the ISO 11784/11785 standard: exactly 15 decimal digits (0–9), no letters, no spaces, no separators.

#### Acceptance Criteria
- **GIVEN** a staff member submits the Add/Edit Pet form
- **WHEN** the microchipId field contains a value that does not match `^\d{15}$`
- **THEN** a field-level validation error is displayed (e.g., "Microchip ID must be exactly 15 digits")
- **AND** the form is not saved

---

### FR-003: microchipId is optional

The microchipId field must not be required. Staff can save a pet record without entering a microchip ID. Existing pets in the system already have null microchipId; they remain valid and do not require the field to be filled on subsequent edits.

#### Acceptance Criteria
- **GIVEN** a staff member submits the Add/Edit Pet form
- **WHEN** the microchipId field is left blank
- **THEN** the pet is saved successfully with microchipId = null
- **AND** no validation error related to microchipId is displayed

---

### FR-004: microchipId uniqueness — system-wide

A microchip ID uniquely identifies one physical animal worldwide. The system must enforce that no two pets share the same microchipId. This constraint applies across all owners and all pet types.

#### Acceptance Criteria
- **GIVEN** a microchipId value already exists in the system for another pet
- **WHEN** a staff member submits a form (Add or Edit) with that same microchipId
- **THEN** the form is not saved
- **AND** a clear error message is shown (e.g., "This microchip ID is already registered to another pet")

---

### FR-005: microchipId displayed on all pet-related screens

Once saved, the microchipId must be visible (read-only) on:
- The **pet detail view** (accessible from the owner page)
- The **owner summary page** — shown inline alongside pet name, type, and birthdate

When microchipId is null, the field is either hidden or shown as "—" (not applicable).

#### Acceptance Criteria
- **GIVEN** a pet has a microchipId saved
- **WHEN** a staff member views the pet detail or owner summary page
- **THEN** the microchipId is displayed as a labeled field
- **AND** no edit controls are shown on these read-only views

---

### FR-006: Search pet by microchipId on Find Owners page

Staff must be able to look up a pet — and by extension its owner — by entering a microchip ID on the **Find Owners** page. The search accepts the exact 15-digit microchip ID. If a match is found, it returns the owner record associated with the matched pet.

If no match is found, the page shows the standard "No owners found" message.

This is an exact-match search — partial input does not return results.

#### Acceptance Criteria
- **GIVEN** a staff member is on the Find Owners page
- **WHEN** they enter a valid 15-digit microchip ID in the microchip search field and submit
- **THEN** the system finds the pet with that microchipId and displays its owner's record
- **AND** the owner details and their pets are shown in the results

- **GIVEN** a staff member enters a microchip ID that does not match any pet
- **WHEN** they submit the search
- **THEN** the results area shows "No owners found" (or equivalent empty-state message)

- **GIVEN** a staff member enters a partial or invalid-format microchip ID
- **WHEN** they submit the search
- **THEN** a validation hint is shown (e.g., "Microchip ID must be exactly 15 digits") and no search is executed

---

### Business Rules Summary

| Rule | Detail |
|---|---|
| BR-001 | microchipId is optional on all pet records |
| BR-002 | When provided, microchipId must be exactly 15 decimal digits (ISO 11784/11785) |
| BR-003 | microchipId must be unique across all pets in the system |
| BR-004 | Existing pets with null microchipId remain valid without change |
| BR-005 | All clinic staff may read and write microchipId — no role restriction |
| BR-006 | microchip ID search on Find Owners page is exact-match only |

---

### Out of Scope

- Audit logging of microchipId changes
- Integration with external national microchip registries
- Bulk import of microchip IDs for existing pets
- Printing microchipId on visit/medical summary documents
