---
description: Functionally validate a change using Playwright
---

Functionally validate a change against its artifacts.

I'll create an artifact with the validation results:
- `validation-results.md` artifact with the full pass/fail report

When the review passes, run `/cbn-6-archive <name>`

---

**Input**: Optionally specify a change name (e.g., `/cbn-5-review add-auth`). If omitted, check if it can be inferred from conversation context. If it is vague or ambiguous, you MUST prompt with the available changes.

**Steps**

1. **Select the change**
   If a name is provided, use it. Otherwise:
   - Infer from conversation context if the user mentioned a change
   - Auto-select if only one active change exists
   - If ambiguous, run `openspec list --json` to get available changes and use the **AskUserQuestion tool** to let the user select

   Always announce: "Using change: <name>"

2. **Read the rules to create the artifact validation-results**
   - If already exists a validations-results.md in openspec/changes/<name> path, use it and proceed to step 5.
   - If not exists a validations-results.md in openspec/changes/<name> path, creat it finishing this step 3 and with step 4 instructions:
      - Get instructions:
         ```bash
         openspec instructions validation-results --change "<name>" --json
         ```
      - Read any completed dependency files for context

3. **Change the session name**
   Rename to "docs(cbn-5-review): generate artifact validation-results.md for <name>" using the `set_session_title` tool and **Display the response JSON to the user**.

4. **Group requirements in testable categories**
   * Extract from these files:
      - The exact functional requirements and acceptance criteria
      - UI/UX expectations (routes, pages, forms, fields, buttons, labels)
      - Data validation rules and error messages
      - Non-functional expectations (performance, accessibility) if documented

   * Build an ordered **validation checklist** mapping each requirement to a testable assertion, grouped by category:
      - Functional (FVAL)
      - UI/UX (UVAL)
      - Data (DVAL)
      - Technical / Non-functional (TVAL)

   *  Use the **TodoWrite tool** to track progress through validation checklist

5. **Compile the project**
   ```bash
   bash ./mvnw verify
   ```
   - If the build fails, report the exact error and stop. Do NOT proceed to start the application.

6. **Start the application**
   ```bash
   bash nohup .opencode/scripts/launch-app.sh 
   ```
   - If the launch fails, report the exact error and stop.

7. **Execute Playwright validation**
   Use the Playwright MCP to perform a point-by-point functional validation in the browser.

   For each item in the todo validation checklist:
   - Navigate to the relevant page/route
   - Perform the user action described in the requirement
   - Assert the expected outcome (element presence, text content, navigation, data persistence)
   - Record result as ✓ Pass or ✗ Fail with exact details (selector, error message, observed value)

   Guidelines:
   - Test each requirement sequentially
   - If a page is unreachable, record it as Blocked and continue with remaining items
   - Do NOT invent requirements — only validate what is documented in the checklist

8. **Stop the application**
   After all validations are complete (or if validation cannot proceed), stop the running application process.
   Close browser used by PlayWright 

9. **Create artifact validation-results**
   Use the **TodoWrite tool** to track progress through the artifacts.

   - Create the artifact file using `template` as the structure, populating it with the actual results collected during Playwright validation:
     - Fill every FVAL, UVAL, DVAL, TVAL entry with real observed data
     - Set the correct status (Passed / Failed / Blocked / Not Testable) per item
     - Set the Overall Result and Score in the summary header
     - List all findings in the Findings table with severity
     - Prompt: "Run `/cbn-6-archive <name>` to generate proposal, design, and tasks."
   - Show brief progress: "Created validation-results"

9. **Commit generated artifact**
   Run with the following parameters:
      - command_id: `cbn-5-review`
      - message_with_purpose: `generate artifact validation-results.md for`
      - change_name: `<name>`

   The next command for commit changes (priorize launch command instead of skill):

   ```bash
   bash .opencode/scripts/commit-command_result.sh  "<command_id>" "<message_with_purpose>" "<change_name>"
   ```

   Push changes
   ```bash
   bash git push -u origin openspec/changes/${CHANGE_NAME}
   ```

10. **Show final status**
   ```bash
   openspec status --change "<name>"
   ```

11. Create a pull request using GitHub MCP:
   - base: main
   - head: openspec/changes/{{CHANGE_NAME}}
   - title: infer from change name and artifacts
   - body: summarize generated artifacts and intent

Ensure the PR is concise and follows conventional commit style.

**Output**
After completing the review, summarize:
- Change name and location
- What's ready: "The `validation-results` artifact was created. Ready for the next step."
- Git branch name reused
- Overall result (Passed / Failed / Passed with issues) and score (X/N)
- If passed: "Run `/cbn-6-archive <name>` to archive this change."
- If failed: list the specific items that need rework before rerunning the review.

**Guardrails**
- This command is strictly for testing and reporting — do NOT make code changes during validation.
- Only validate what is documented in the specification artifacts — do NOT guess features.
- If the application crashes during testing or Playwright cannot connect, stop and report immediately in the artifact.
- If a change with that name does not exist or has no completed artifacts, inform the user and stop.