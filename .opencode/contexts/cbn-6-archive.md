# Project Context — cbn-6-archive

## Archive Conventions

- Archive directory: `openspec/changes/archive/`
- Archive naming: `YYYY-MM-DD-<change-name>` using today's date
- Branch to push: `openspec/changes/<name>` (reuse — do NOT create a new branch)

## No Special Archive Rules

This project has no custom spec sync rules or branch naming overrides beyond the cbn defaults.

## Git Notes

- No pre-commit hooks — commits will not be blocked by linters or formatters
- CI runs `./mvnw -B verify` on PRs — the archive commit does not trigger a build by itself
- SonarCloud runs on main branch only — archive branch is not affected
