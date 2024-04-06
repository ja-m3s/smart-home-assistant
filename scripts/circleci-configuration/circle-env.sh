#!/usr/bin/env bash
# Name: circle-env.sh
# Description: Env file for circle scripts
# Author: ja-m3s

# I use pass to store these, but you could just enter these on the CLI

# Default values
ORG_ID_DEFAULT=$(pass circleci.com/ORG_ID)
CIRCLE_API_TOKEN_DEFAULT=$(pass circleci.com/PERSONAL_API_TOKENS/UPDATE_CIRCLECI_FROM_TERRAFORM)
CIRCLE_PROJECT_SLUG_DEFAULT=$(pass circleci.com/PROJECT_SLUG_SMART_HOME)

# Override default values if provided as arguments
ORG_ID="${1:-$ORG_ID_DEFAULT}"
CIRCLE_API_TOKEN="${2:-$CIRCLE_API_TOKEN_DEFAULT}"
CIRCLE_PROJECT_SLUG="${3:-$CIRCLE_PROJECT_SLUG_DEFAULT}"

DOCKER_REPO_DB_IMPORTER=sma-db-importer
DOCKER_REPO_LIGHT_BULB_MONITOR=sma-light-bulb-monitor
DOCKER_REPO_LIGHT_BULB=sma-light-bulb

DOCKER_USER=$(pass hub.docker.com/username)
DOCKER_PASSWORD=$(pass hub.docker.com/password)
CONTEXT_NAME=org-context-smart-home

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

