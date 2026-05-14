---
description: Review the functional and technical requirements artifacts for an existing change and update them if necessary.
---

Generate the change definition from an existing requirements baseline.

I'll update the existing change by updating these artifacts:
- `functional-requirements.md`
- `technical-requirements.md`

When the requirements are ready, `run /cbn-3-definition <name>`
---

**Input**: Optionally specify a change name (e.g., `/cbn-3-definition add-auth`). If omitted, check if it can be inferred from conversation context. If it is vague or ambiguous, you MUST prompt with the available changes.

**Steps**

1. **Select the change**
   If a name is provided, use it. Otherwise:
   - Infer from conversation context if the user mentioned a change
   - Auto-select if only one active change exists
   - If ambiguous, run `openspec list --json` to get available changes and use the **AskUserQuestion tool** to let the user select

   Always announce: "Using change: <name>".

2. **Get functional-requirements-human-review instructions**
   ```bash
   openspec instructions functional-requirements-human-review --change "<name>" --json
   ```

3. **Get technical-requirements-human-review instructions**
   ```bash
   openspec instructions technical-requirements-human-review --change "<name>" --json
   ```

4. **Change the session name**
   Rename to "docs(cbn-2.5-requirements-human-review): Update artifacts technical-requirements.md and functional-requirements.md for <name>" using the `set_session_title` tool and **Display the response JSON to the user**.

5. **Read context files**
   - **Read all the completed dependency files** using:
   ```bash
   cat <file>
   ```

6. **Open conersation with the user** to reseolve functional-requirements
   Talk and discuss the functional requirements
   a. Use **AskUserQuestion tool** to collect additional context and clarify any new lines of inquiry.
   b. **Repeat step as needed** until the user clearly states that they have no further ideas or topics to explore.

7. **Open conersation with the user** to reseolve technical-requirements
   Talk and discuss the technical requirements
   a. Use **AskUserQuestion tool** to collect additional context and clarify any new lines of inquiry.
   b. **Repeat step as needed** until the user clearly states that they have no further ideas or topics to explore.

8. **Reap**
   - Recap all conversations and, if there are changes to be made, give the user a preview of the changes you will make to functional-requirements.md and/or technical-requirements.md.
   - Request the user to validate the changes.

9. **Commit generated artifact**
   Run with the following parameters:
      - command_id: `cbn-2.5-requirements-human-review`
      - message_with_purpose: `update artifacts technical-requirements.md and functional-requirements.md for `
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
After completing all artifacts, summarize:
- Change name and location
- List of artifacts updated with brief descriptions
- Whether the generated artifact changes were committed
- Prompt: "Run `/cbn-3-definition <name>` to start implementing."

**Guardrails**
- This command is for **definition generation from approved requirements**, not for requirements discovery or implementation
- Do NOT update artifacts from guesswork when requirements are materially incomplete
- Verify each artifact file exists after writing before proceeding to the next