# Functional Requirements — Orchestrator Input Template

<!--
  This template is used by the orchestrator when invoking the
  cbn-1-functional-requirements subagent.

  Required variables to interpolate before sending:
    {change_name}  — kebab-case name of the change (e.g., "add-user-auth")
                     If unknown at invocation time, pass an empty string and
                     the agent will ask the user.

  Optional variables:
    {description}  — Free-text description of what the user wants to build or
                     fix. If provided, the agent will derive the name from it
                     and confirm before proceeding.

  How to use:
    1. Replace all {placeholder} tokens with their runtime values.
    2. Send the resulting text as the first user message to the agent.
    3. Do not modify the instruction lines below; they are directives to the agent.
-->

Execute the functional requirements workflow for the following change.

**Change name**: {change_name}
**Description**: {description}
