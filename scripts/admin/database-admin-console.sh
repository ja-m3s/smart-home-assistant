#!/usr/bin/env bash

microk8s kubectl exec -it cockroachdb-secure-client-0 -- \
    ./cockroach sql \
    --certs-dir=/cockroach/cockroach-certs \
    --host=cockroach-db-public