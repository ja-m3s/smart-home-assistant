#!/usr/bin/env bash
# Name: database-admin-console.sh
# Description: Connects up to the database for admin tasks
# Author: ja-m3s

microk8s kubectl exec -it cockroachdb-secure-client-0 -- \
    ./cockroach sql \
    --certs-dir=/cockroach/cockroach-certs \
    --host=cockroach-db-public
