#!/bin/sh

set -eu

PORT="${PORT:-8080}"
URL="${URL:-http://localhost:${PORT}/}"
INTERVAL="${INTERVAL:-5}"
MAX_TIME="${MAX_TIME:-300}"
READY=0

log() {
  echo "$@" >&2
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "ERROR: necesitas tener instalado $1"
    exit 1
  fi
}

get_pids_on_port() {
  log "Checking processes on port ${PORT}..."

  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true
    return 0
  fi

  if command -v fuser >/dev/null 2>&1; then
    fuser "${PORT}/tcp" 2>/dev/null || true
    return 0
  fi

  log "ERROR: necesitas tener instalado lsof o fuser para liberar el puerto ${PORT}"
  exit 1
}

kill_processes_on_port() {
  log "Calling get_pids_on_port..."
  PIDS="$(get_pids_on_port)"

  if [ -z "$PIDS" ]; then
    log "Port ${PORT} is free"
    return 0
  fi

  log "WARNING: stopping process(es) listening on port ${PORT}: ${PIDS}"
  kill $PIDS 2>/dev/null || true

  i=0
  while [ "$i" -lt 10 ]; do
    sleep 1
    PIDS="$(get_pids_on_port)"

    if [ -z "$PIDS" ]; then
      log "Port ${PORT} released"
      return 0
    fi

    i=$((i + 1))
  done

  log "Force killing process(es) on port ${PORT}: ${PIDS}"
  kill -9 $PIDS 2>/dev/null || true

  sleep 1
  PIDS="$(get_pids_on_port)"

  if [ -n "$PIDS" ]; then
    log "ERROR: could not release port ${PORT}. Still used by: ${PIDS}"
    exit 1
  fi

  log "Port ${PORT} released"
}

cleanup_before_ready() {
  if [ "${READY:-0}" != "1" ] &&
     [ -n "${JETTY_PID:-}" ] &&
     kill -0 "$JETTY_PID" 2>/dev/null; then
    log "Stopping app before it became ready..."
    kill "$JETTY_PID" 2>/dev/null || true
  fi
}

trap cleanup_before_ready EXIT
trap 'cleanup_before_ready; exit 130' INT
trap 'cleanup_before_ready; exit 143' TERM

require_command curl

kill_processes_on_port

log "Starting app..."
./mvnw jetty:run-war -DskipTests &

JETTY_PID=$!

log "Jetty PID: ${JETTY_PID}"

elapsed=0

while [ "$elapsed" -lt "$MAX_TIME" ]; do
  if ! kill -0 "$JETTY_PID" 2>/dev/null; then
    log "ERROR: Jetty process stopped before app was ready"
    exit 1
  fi

  HTTP_CODE="$(curl -s -o /dev/null -w "%{http_code}" "$URL" || true)"

  if [ "$HTTP_CODE" = "200" ]; then
    READY=1
    echo "OK"
    log "App started successfully at ${URL}"
    log "Console released. Jetty keeps running in background with PID ${JETTY_PID}"
    exit 0
  fi

  log "Waiting... (${elapsed}s elapsed). HTTP status: ${HTTP_CODE}"

  sleep "$INTERVAL"
  elapsed=$((elapsed + INTERVAL))
done

log "ERROR: App did not start within ${MAX_TIME} seconds"
exit 1
