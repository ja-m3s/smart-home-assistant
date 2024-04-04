#!/usr/bin/env bash

# I use pass to store these, but you could just enter these on the CLI

# Default values
ORG_ID_DEFAULT=$(pass circleci.com/ORG_ID)
CIRCLE_API_TOKEN_DEFAULT=$(pass circleci.com/PERSONAL_API_TOKENS/UPDATE_CIRCLECI_FROM_TERRAFORM)
CIRCLE_PROJECT_SLUG_DEFAULT=$(pass circleci.com/PROJECT_SLUG_SMART_HOME)

# Override default values if provided as arguments
ORG_ID="${1:-$ORG_ID_DEFAULT}"
CIRCLE_API_TOKEN="${2:-$CIRCLE_API_TOKEN_DEFAULT}"
CIRCLE_PROJECT_SLUG="${3:-$CIRCLE_PROJECT_SLUG_DEFAULT}"

DOCKER_REPO=$(pass hub.docker.com/repo)
DOCKER_USER=$(pass hub.docker.com/username)
DOCKER_PASSWORD=$(pass hub.docker.com/password)

# Function to make API calls
make_circleci_api_call() {
  local method="$1"
  local url="$2"
  local data="$3"

  curl --request "$method" \
    --url "$url" \
    --header "Circle-Token: $CIRCLE_API_TOKEN" \
    --header 'content-type: application/json' \
    --data "$data"
}

