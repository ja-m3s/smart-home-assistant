#!/usr/bin/env bash

# Update circleci with the environment variables of the temporary ftp server
# using credentials from pass
# MANUAL STEP
set -x

source circle-env.sh

CONTEXT_ID=$(make_circleci_api_call "POST" \
  "https://circleci.com/api/v2/context" \
  '{
    "name": "org-context-smart-home",
    "owner": {
        "id": "'"${ORG_ID}"'",
        "type": "organization"
    }
}' | jq -r '.id')

# Associative array for environment variables
declare -A env_variables=(
  ["CIRCLE_PROJECT_SLUG"]="$CIRCLE_PROJECT_SLUG"
  ["DOCKER_REPO"]=$DOCKER_REPO
  ["DOCKER_USER"]=$DOCKER_USER
  ["DOCKER_PASSWORD"]=$DOCKER_PASSWORD
)

# Make API calls for each environment variable
for var_name in "${!env_variables[@]}"; do
  make_circleci_api_call "PUT" \
    "https://circleci.com/api/v2/context/${CONTEXT_ID}/environment-variable/$var_name" \
    "{\"value\": \"${env_variables[$var_name]}\"}"
done