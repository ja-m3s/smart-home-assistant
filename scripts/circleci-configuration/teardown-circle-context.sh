#!/usr/bin/env bash
# Name: teardown-circle-context.sh
# Description: Deletes the context called specified in env file
# Author: ja-m3s

source circle-env.sh

#Retrieve id of context
CONTEXT_ID=$(make_circleci_api_call "GET" \
    "https://circleci.com/api/v2/context?owner-id=${ORG_ID}" \
    | jq -r '.items[] | select(.name == "'"${CONTEXT_NAME}"'") | .id')

# Delete the circleci context
make_circleci_api_call "DELETE" \
"https://circleci.com/api/v2/context/${CONTEXT_ID}";
