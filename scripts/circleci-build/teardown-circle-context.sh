#!/usr/bin/env bash

#Deletes the context called 'org-context'

source circle-env.sh

#Retrieve id of org-context
CONTEXT_ID=$(make_circleci_api_call "GET" \
    "https://circleci.com/api/v2/context?owner-id=${ORG_ID}" \
    | jq -r '.items[] | select(.name == "org-context-smart-home") | .id')

# Delete the circleci context
make_circleci_api_call "DELETE" \
"https://circleci.com/api/v2/context/${CONTEXT_ID}";