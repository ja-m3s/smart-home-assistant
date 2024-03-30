#!/usr/bin/env bash

microk8s kubectl exec -it test-release-client-secure -- ./cockroach sql --certs-dir=./cockroach-certs --host=test-release-cockroachdb-public