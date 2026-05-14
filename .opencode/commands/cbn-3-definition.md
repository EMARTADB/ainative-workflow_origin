---
description: Generate change artifacts from approved requirements
---

Generate the change definition from an existing requirements baseline.

I'll update the existing change by generating these artifacts:
- `proposal.md` (what & why)
- `design.md` (how)
- `tasks.md` (implementation steps)

When ready to implement, run `/cbn-4-construction <name>`
---

**Input**: Optionally specify a change name (e.g., `/cbn-3-definition add-auth`). If omitted, check if it can be inferred from conversation context. If it is vague or ambiguous, you MUST prompt with the available changes.

**Steps**

1. **Select the change**
   If a name is provided, use it. Otherwise:
   - Infer from conversation context if the user mentioned a change
   - Auto-select if only one active change exists
   - If ambiguous, run `openspec list --json` to get available changes and use the **AskUserQuestion tool** to let the user select

   Always announce: "Using change: <name>"


2. **Get the artifact build order**
   ```bash
   openspec status --change "<name>" --json 
   ```
   Parse the JSON to get:
   - `applyRequires`: array of artifact IDs needed before implementation (e.g., `["tasks"]`)
   - `artifacts`: list of all artifacts with their status and dependencies

3. **Change the session name**
   Rename to "docs(cbn-3-definition): generate artifacts (proposal.md, design.md, tasks.md and spec.md file(s)) for <name>" using the `set_session_title` tool and **Display the response JSON to the user**.

4. **Create artifacts in sequence until apply-ready**
   Use the **TodoWrite tool** to track progress through the artifacts.

   Loop through artifacts in dependency order (artifacts with no pending dependencies first):

   a. **For each artifact that is `ready` (dependencies satisfied)**:
      - Get instructions:
        ```bash
         openspec status --change "<name>" --json | jq '.artifacts |= map(select(.id as $id | ["validation-results", "validation-plan"] | index($id) | not))'
        ```
      - The instructions JSON includes:
        - `context`: Project background (constraints for you - do NOT include in output)
        - `rules`: Artifact-specific rules (constraints for you - do NOT include in output)
        - `template`: The structure to use for your output file
        - `instruction`: Schema-specific guidance for this artifact type
        - `outputPath`: Where to write the artifact
        - `dependencies`: Completed artifacts to read for context
      - Read any completed dependency files for additional context
      - Create the artifact file using `template` as the structure
      - Use `requirements.md` as the main functional baseline
      - Apply `context` and `rules` as constraints - but do NOT copy them into the file
      - Show brief progress: "Created <artifact-id>"

   b. **Continue until all `applyRequires` artifacts are complete**
      - After creating each artifact, re-run `openspec status --change "<name>" --json`
      - Check if every artifact ID in `applyRequires` has `status: "done"` in the artifacts array
      - Stop when all `applyRequires` artifacts are done

   c. **If requirements are insufficient or contradictory for an artifact**:
      - Do NOT invent missing behavior
      - Do NOT ask the user to restate the feature from scratch

5. **Commit generated artifacts on the existing change branch**
   Run with the following parameters:
      - command_id: `cbn-3-definition`
      - message_with_purpose: `generate artifacts: proposal.md, design.md, tasks.md and spec.md file(s) for `
      - change_name: `<name>`

   The next command for commit changes (priorize launch command instead of skill):

   ```bash
   bash .opencode/scripts/commit-command_result.sh "<command_id>" "<message_with_purpose>" "<change_name>"
   ```

   Push changes
   ```bash
   bash git push -u origin openspec/changes/${CHANGE_NAME}
   ```

6. **Show final status**
   ```bash
   openspec status --change "<name>"
   ```

**Output**
After completing all artifacts, summarize:
- Change name and location
- List of artifacts created with brief descriptions
- What's ready: "All artifacts created! Ready for implementation."
- Prompt: "Run `/cbn-4-construction <name>` to start implementing."

**Artifact Creation Guidelines**
- Follow the `instruction` field from `openspec instructions` for each artifact type
- The schema defines what each artifact should contain - follow it
- Read dependency artifacts for context before creating new ones
- Use `template` as the structure for your output file - fill in its sections
- **IMPORTANT**: `context` and `rules` are constraints for YOU, not content for the file
  - Do NOT copy `<context>`, `<rules>`, `<project_context>` blocks into the artifact
  - These guide what you write, but should never appear in the output

**Guardrails**
- Do NOT ask the user what they want to build 
- Create ALL artifacts needed for implementation (as defined by schema's `apply.requires`)
- Always read dependency artifacts before creating a new one
- If context is critically unclear, ask the user - but prefer making reasonable decisions to keep momentum
- If a change with that name already exists, ask if user wants to continue it or create a new one
- Verify each artifact file exists after writing before proceeding to next