#!/usr/bin/env bash
# Name: create-circle-context.sh
# Description: Creates a circle ci context
# Author: ja-m3s

set -x

source circle-env.sh

CONTEXT_ID=$(make_circleci_api_call "POST" \
  "https://circleci.com/api/v2/context" \
  '{
    "name": "'"${CONTEXT_NAME}"'",
    "owner": {
        "id": "'"${ORG_ID}"'",
        "type": "organization"
    }
}' | jq -r '.id')

# Associative array for environment variables
declare -A env_variables=(
  ["DOCKER_REPO_DB_IMPORTER"]=$DOCKER_REPO_DB_IMPORTER
  ["DOCKER_REPO_LIGHT_BULB"]=$DOCKER_REPO_LIGHT_BULB
  ["DOCKER_REPO_LIGHT_BULB_MONITOR"]=$DOCKER_REPO_LIGHT_BULB_MONITOR
  ["DOCKER_REPO_REMOTE"]=$DOCKER_REPO_REMOTE
  ["DOCKER_USER"]=$DOCKER_USER
  ["DOCKER_PASSWORD"]=$DOCKER_PASSWORD
)

# Make API calls for each environment variable
for var_name in "${!env_variables[@]}"; do
  make_circleci_api_call "PUT" \
    "https://circleci.com/api/v2/context/${CONTEXT_ID}/environment-variable/$var_name" \
    "{\"value\": \"${env_variables[$var_name]}\"}"
done