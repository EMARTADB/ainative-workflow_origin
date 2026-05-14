# Definition — Orchestrator Input Template

<!--
  Template for the cbn-3-definition subagent.

  Required variables to interpolate before sending:
    {change_name}  — kebab-case name of the change (e.g., "add-user-auth")
                     If unknown, pass empty string and the agent will resolve it.

  How to use:
    1. Replace all {placeholder} tokens with runtime values.
    2. Send the resulting text as the first user message to the agent.
-->

Execute the definition artifact generation workflow for the following change.

**Change name**: {change_name}
