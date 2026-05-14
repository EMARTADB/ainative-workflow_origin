---
description: Define functional requirements for a new change, create the change, and generate the functional-requirements artifact
---

Create the change and generate functional-requirements artifact in one step.

Help remove ambiguity, ask focused questions, and document the scope, user flows, business rules, edge cases, constraints, and acceptance criteria.

I'll create a change with artifact:
- `functional-requirements.md` (functional baseline)

When the requirements are ready, run `/cbn-2-technical-requirements <name>`

---

**Input**: The argument after `/cbn-1-functional-requirements` is the change name (kebab-case), OR a description of what the user wants to build.

**Steps**
1. **If no full context provided, ask what they want to build**
   Use the **AskUserQuestion tool** (open-ended, no preset options) to ask:
   > "What change do you want to work on? Describe what you want to build or fix."

   From their description, derive a kebab-case name (e.g., "add user authentication" → `add-user-auth`).

   **IMPORTANT**: Do NOT proceed without understanding what the user wants to build.

2. **Create the change directory**
   ```bash
   openspec new change "<name>" --schema cbn-spec-driven
   ```
   This creates a scaffolded change at `openspec/changes/<name>/` with `.openspec.yaml`.

3. **Read the rules to create the artifact functional-requirements**
   - Get instructions:
      ```bash
      openspec instructions functional-requirements --change "<name>" --json
      ```

4. **Change the session name**
   Rename to "docs(cbn-1-functional-requirements): generate artifact functional-requirements.md for <name>" using the `set_session_title` tool and **Display the response JSON to the user**.

5. **The end user selects change_type**
   - Use **AskUserQuestion tool** to collect change_type, **The user must strictly select one of these options; do not allow open-ended responses.** .
      * **Fix**: Something is malfunctioning
      * **Small Change**: The change is specific and limited
      * **Standard Change**: Change and flow need to be understood, but without a major redesign
      * **Large Change**: Processes, roles, decisions, or multiple areas change

6. **Show the user the selected change_type and the associated list**
      a. Fix
         1. What is wrong
         2. What should happen
         3. When and where it happens
         4. Business impact
         5. How to confirm it is fixed
      b. Small Change
         1. Why this change is needed
         2. Expected business result
         3. Exact scope
         4. Affected users
         5. Business rule or data impacted
         6. Acceptance check
      c. Standard Change
         1. Business driver
         2. Expected outcome
         3. Scope
         4. Current way of working
         5. Target change
         6. Users involved
         7. Business rules
         8. Acceptance criteria
      d. Large Change
         1. Business driver
         2. Problem statement
         3. Expected outcome
         4. Scope
         5. Out of scope
         6. Current process
         7. Desired process
         8. Users and stakeholders
         9. Business rules
         10. Exceptions
         11. Organizational impact
         12. Acceptance criteria

7. **Functional Requirements Discovery Checklist**
   - Detect missing information, ambiguities, and contradictions.
   - Separate facts from assumptions.
   - Confirm scope boundaries.
   - Summarize the requirement in business language.
   - Identify unresolved questions before closing the session.
   - Ask targeted questions for te previous list using **AskUserQuestion tool**.
      - Always offer suggestions and mark one of them as "(Recommended)."
      - Always offer the option to ignore the question.

8. **Proactively ask the user if they have any other ideas**, follow-up questions, or areas they would like to explore in more depth.
   a. Use **AskUserQuestion tool** to collect additional context and clarify any new lines of inquiry.
   b. **Repeat step 7 as needed** until the user clearly states that they have no further ideas or topics to explore.

9. **Summarize and validate understanding**
   After each major round, summarize the current understanding and check for gaps.
   **Continue asking questions if any point remain unclear**.

10. **Create artifact functional-requirements**
   Use the **TodoWrite tool** to track progress through the artifacts.

   - Create the artifact file using `template` as the structure
   - Show brief progress: "Created <artifact-id>"

11. **Create git branch and commit requirements**
   Run with the following parameters:
      - command_id: `cbn-1-functional-requirements`
      - message_with_purpose: ` generate artifact functional-requirements.md for `
      - change_name: `<name>`

   The next commands for commit changes (priorize launch commands instead of skill):

   If the branch exists, switch to it; if not, create it.
      ```bash
      bash git switch "openspec/changes/${CHANGE_NAME}" 2>/dev/null || \
      bash git switch -c "openspec/changes/${CHANGE_NAME}"
      ```
   Commit changes
      ```bash
      bash .opencode/scripts/commit-command_result.sh "<command_id>" "<message_with_purpose>" "<change_name>"
      ```
   Push changes
      ```bash
      bash git push -u origin openspec/changes/${CHANGE_NAME}
      ```

12. **Show final status**
   ```bash
   openspec status --change "<name>"
   ```

**Output**
After completing the requirements, summarize:
- Change name and location
- What's ready: "The `functional-requirements` artifact was created. Ready for the next step."
- Git branch name created or reused
- Whether the requirements changes were committed
- Prompt: "Run `/cbn-2-technical-requirements <name>` to generate technical requirements."

**Guardrails**
- This command is for functional-requirements discovery and definition only
- If a change with that name already exists, ask if user wants to continue it or create a new one