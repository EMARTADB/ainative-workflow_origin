#!/usr/bin/env bash
set -euo pipefail

COMMAND_NAME="$1"
MESSAGE_WITH_PURPOSE="$2"
CHANGE_NAME="$3"

EXPECTED_BRANCH="openspec/changes/${CHANGE_NAME}"
CHANGE_DIR="openspec/changes/${CHANGE_NAME}"

CURRENT_BRANCH="$(git branch --show-current)"

if [ "$CURRENT_BRANCH" != "$EXPECTED_BRANCH" ]; then
  echo "ERROR: Not on expected branch."
  echo "Current branch:  ${CURRENT_BRANCH}"
  echo "Expected branch: ${EXPECTED_BRANCH}"
  echo "Switch to the branch created in /cbn-1-functional-requirements ${CHANGE_NAME}"
  exit 1
fi

git status --short

git add "${CHANGE_DIR}/"

git commit -m "docs(${COMMAND_NAME}): ${MESSAGE_WITH_PURPOSE} for ${CHANGE_NAME}"

git push -u origin "${CURRENT_BRANCH}"

git status --short
