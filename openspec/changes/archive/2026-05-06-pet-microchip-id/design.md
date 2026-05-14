## Design
**Session name:** 2026-05-06 15:23:30 - docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for pet-microchip-id
**Session id:** ses_2028ad7fbffePuOwT3812K1vNN
**Background and current state:** PetClinic uses plain Spring Framework 7.x (no Spring Boot) with XML configuration, JSP views, and three switchable persistence backends (JPA, JDBC, Spring Data JPA) across four supported databases (H2, HSQLDB, MySQL, PostgreSQL). The `Pet` entity currently has `name`, `birthDate`, `type`, `owner`, and `visits` — no microchip field exists. The `ClinicService` facade exposes CRUD and owner-by-last-name search; no microchip-related query exists. DDL is managed via plain SQL init scripts, one per database.

## Goals / Non-Goals

**Goals:**
- Add `microchipId` (optional, unique, 15-digit ISO) to `Pet` across all three backends and four DB DDL files.
- Surface `microchipId` on Add/Edit Pet forms with Bean Validation and display it read-only on owner/pet views.
- Add exact-match microchip search on the Find Owners page.
- Gracefully surface duplicate-microchip errors to the user.
- Maintain CI green for all three persistence profiles.

**Non-Goals:**
- External microchip registry integration.
- Audit logging of microchip changes.
- Bulk microchip import.
- Microchip on visit/medical printouts.
- Role-based access restrictions on the new field.

## Decisions

### Decision 1: Where does `findOwnerByMicrochipId` live — `OwnerRepository` or `PetRepository`?

A microchip ID identifies a `Pet`, but the search on the Find Owners page must return an `Owner`. Two options:

| Option | Pros | Cons |
|---|---|---|
| **A. New method on `PetRepository`** — return `Pet`, then navigate to `pet.getOwner()` in the controller | Semantically clean (microchip → pet) | Controller must do two steps; JDBC impl must JOIN or do two queries |
| **B. New method on `OwnerRepository`** — JOIN pets in query, return `Owner` directly | One query; matches existing `findByLastName` pattern | Couples `OwnerRepository` to pet column |

**Decision: Option B** — add `Owner findOwnerByPetMicrochipId(String microchipId)` to `OwnerRepository`. This mirrors the existing `findByLastName` pattern, keeps the controller thin, and is straightforward for all three backends (JPA JPQL JOIN, JDBC JOIN, Spring Data derived query). Returns `null` when not found (consistent with the project's existing null-return pattern).

---

### Decision 2: Empty string → null coercion

HTML forms submit empty inputs as empty strings. `@Pattern(regexp = "^\\d{15}$")` would reject an empty string (FR-003 requires null to be valid). Two options:

| Option | Description |
|---|---|
| **A. Custom `@InitBinder`** | Register `StringTrimmerEditor(true)` in `PetController` to convert empty → null before validation |
| **B. Custom validator** | Write a `PetValidator` that special-cases the empty-string case |

**Decision: Option A** — `StringTrimmerEditor(true)` is already a Spring MVC idiom in this project (present in `OwnerController`). Add it to `PetController`'s `@InitBinder`. This converts `""` → `null` before Bean Validation runs, so `@Pattern` only fires on non-null values (which is JSR-380's default behaviour for `@Pattern` when the value is null).

---

### Decision 3: Duplicate-microchip error surface point

When two concurrent saves race past the application-layer check, the DB unique constraint fires and Spring throws `DataIntegrityViolationException`. Two options:

| Option | Description |
|---|---|
| **A. Catch in `ClinicServiceImpl.savePet()`** | Translate to a domain exception; controller maps to form error |
| **B. Catch in `PetController.processCreationForm()`** | Try/catch around `clinicService.savePet()`, add binding error directly |

**Decision: Option B** — simpler and keeps the service layer free of web concerns. The controller already handles binding errors for the form; catching `DataIntegrityViolationException` there and adding a `BindingResult` error on `microchipId` is consistent with the existing validation flow. The service layer adds an application-level pre-check (query before save) to avoid relying on exceptions for normal-case duplicates.

---

### Decision 4: Microchip search UI — separate input or reuse last-name field?

The Find Owners page currently has a single last-name text input and a submit button. Options:

| Option | Description |
|---|---|
| **A. Second dedicated input field** | "Search by microchip ID" input beneath last-name field; separate form or same form with two actions |
| **B. Extend existing input to detect format** | If input matches `^\d{15}$`, route to microchip search; otherwise last-name |

**Decision: Option A** — a dedicated input is explicit and avoids routing ambiguity (owner last names could theoretically be all digits). The JSP gets a second `<input>` for `microchipId`; the `OwnerController.processFindForm` method checks which parameter is populated and dispatches accordingly.

---

### Decision 5: `PetRepository` — no change needed for microchip persistence in JPA backend

In the JPA backend, `Pet` is a JPA `@Entity` managed by Hibernate. Adding `@Column(name = "microchip_id")` to `Pet.java` is sufficient — no changes to `JpaPetRepositoryImpl` are needed for basic save/load. The JDBC backend (`JdbcPetRepositoryImpl`) requires explicit column mapping in its SQL queries and `RowMapper`.

## Risks / Trade-offs

- **Race-condition duplicate inserts** → Mitigation: DB unique constraint is the safety net (TR-002). Application pre-check reduces but does not eliminate races; controller catches `DataIntegrityViolationException` and shows user-friendly error (TR-007).
- **HSQLDB NULL in UNIQUE constraint** → Standard SQL: multiple NULLs allowed in a UNIQUE column. H2, HSQLDB, MySQL, PostgreSQL all behave this way. No special handling needed.
- **JDBC backend SQL verbosity** → The JDBC implementations require manual column additions to INSERT/UPDATE/SELECT statements and RowMappers. Risk of omission in one query. Mitigation: integration tests on all three profiles catch missing column mappings.
- **JSP view duplication** — if `ownerDetails.jsp` and the owner-list template each render pet attributes, both need updating (FR-005). Missing one will cause inconsistent display. Mitigation: covered in tasks checklist.

## Migration Plan

1. Update all four DDL scripts (`h2`, `hsqldb`, `mysql`, `postgresql`) to add `microchip_id VARCHAR(15) UNIQUE` to the `pets` table.
2. Seed data (`data.sql`) files require no change — `NULL` is the column default.
3. Deploy as a full schema drop-and-recreate (existing project behaviour); no `ALTER TABLE` migration needed.
4. **Rollback**: revert DDL scripts and application code; redeploy with clean schema.

## Open Questions

- None — all technical decisions resolved above. The scope is small-change and fully covered by existing patterns in the codebase.
