---
description: Define technical requirements for an existing change and create the technical-requirements artifact
---

Generate the technical-requirements artifact in one step.

I'll create an artifact for the existing change:
- `technical-requirements.md` (technical baseline)

When the requirements are ready, run `/cbn-3-definition <name>`

---

**Input**: Optionally specify a change name (e.g., `/cbn-2-technical-requirements add-auth`). If omitted, check if it can be inferred from conversation context. If it is vague or ambiguous, you MUST prompt with the available changes.

**Steps**

1. **Select the change**
   If a name is provided, use it. Otherwise:
   - Infer from conversation context if the user mentioned a change
   - Auto-select if only one active change exists
   - If ambiguous, run `openspec list --json` to get available changes and use the **AskUserQuestion tool** to let the user select

   Always announce: "Using change: <name>".

2. **Read the rules to create the artifact technical-requirements**
   - Get instructions:
      ```bash
      openspec instructions technical-requirements --change "<name>" --json
      ```
   - Read any completed dependency files for context

3. **Change the session name**
   Rename to "docs(cbn-2-technical-requirements): generate artifact technical-requirements.md for <name>" using the `set_session_title` tool and **Display the response JSON to the user**.

4. **Always anounce the list** to align on the technical approach.:
   - Performance
   - Security
   - Reliability
   - Availability
   - Scalability
   - Resilience / Recoverability
   - Operability / Observability
   - Maintainability
   - Testability
   - Interoperability
   - Compliance / Privacy
   - Portability / Extensibility

5. **Technical Requirements Discovery Checklist**
   - Detect missing information, ambiguities, and contradictions.
   - Separate facts from assumptions.
   - Confirm scope boundaries.
   - Summarize the requirement in technical language.
   - Identify unresolved questions before closing the session.
   - Ask targeted questions for te previous list using **AskUserQuestion tool**.
      - Always offer suggestions and mark one of them as "(Recommended)."
      - Always offer the option to ignore the question.

6. **Proactively ask the user if they have any other ideas**, follow-up questions, or areas they would like to explore in more depth.
   a. Use **AskUserQuestion tool** to collect additional context and clarify any new lines of inquiry.
   b. **Repeat step 7 as needed** until the user clearly states that they have no further ideas or topics to explore.

7. **Summarize and validate understanding**
   After each major round, summarize the current understanding and check for technical gaps.

8. **Create artifact technical-requirements**
   Use the **TodoWrite tool** to track progress through the artifacts.

   - Create the artifact file using `template` as the structure
   - Show brief progress: "Created <artifact-id>"

9. **Commit generated artifact**
   Run with the following parameters:
      - command_id: `cbn-2-technical-requirements`
      - message_with_purpose: `generate artifact technical-requirements.md for `
      - change_name: `<name>`

   The next command for commit changes (priorize launch command instead of skill):

   ```bash
   bash .opencode/scripts/commit-command_result.sh "<command_id>" "<message_with_purpose>" "<change_name>"
   ```
   Push changes
   ```bash
   bash git push -u origin openspec/changes/${CHANGE_NAME}
   ```

10. **Show final status**
   ```bash
   openspec status --change "<name>"
   ```

**Output**
After completing the requirements, summarize:
- Change name and location
- What's ready: "The `technical-requirements` artifact was created. Ready for the next step."
- Git branch name created or reused
- Whether the requirements changes were committed
- Prompt: "Run `/cbn-3-definition <name>` to generate proposal, design, and tasks."

**Guardrails**
- This command is for technical-requirements discovery and definition only
- If a change with that name already exists, ask if user wants to continue it or create a new one