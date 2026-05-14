# Project Context — cbn-5-review

## Application Start

**Use the launch script** (handles port cleanup, PID tracking, and readiness polling):

```bash
bash .opencode/scripts/launch-app.sh
```

This script:
- Kills any process already using port 8080
- Starts `./mvnw jetty:run-war` in the background
- Polls `http://localhost:8080/` every 30 seconds (max 5 minutes)
- Prints `OK` to stdout and exits 0 when the app is ready
- Exits 1 if the app fails to start or the process dies

**Do NOT** run `./mvnw jetty:run-war` directly in the foreground — it blocks the terminal.

## Build Command

```bash
./mvnw verify -DskipTests
```

## Application URL

- Base URL: `http://localhost:8080/`
- Owner list: `http://localhost:8080/owners`
- Find owners: `http://localhost:8080/owners/find`
- Vet list: `http://localhost:8080/vets.html`

## Known Test Environment Constraints

- H2 in-memory database — data resets on each restart
- No authentication — all pages are publicly accessible
- Default Spring profile is `jpa` — Hibernate/JPA backend is active during validation
- App runs as a WAR on embedded Jetty — context root is `/`

## Stop the Application

After validation, kill the Jetty process:

```bash
# Find and kill the process on port 8080
lsof -nP -tiTCP:8080 -sTCP:LISTEN | xargs kill 2>/dev/null || true
```

Or re-run the launch script (it kills existing processes before starting).

## Architecture Context for Validation

- Views are JSP — rendered server-side; no client-side routing
- All forms use POST/redirect/GET pattern
- No REST API — validate via browser navigation and form submission only
- Three persistence backends exist but only one is active per run (default: `jpa`)
