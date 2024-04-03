#!/bin/bash
set -eux

# Check if required variables are set
if [ -z "$DB_NAME" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ] || [ -z "${DB_SCHEMA}" ]; then
    echo "Error: DB_NAME, DB_USER, DB_PASSWORD, and DB_SCHEMA environment variables must be set."
    exit 1
fi

# Schema
SQL_COMMANDS="CREATE USER IF NOT EXISTS roach WITH PASSWORD 'roach';
GRANT admin to roach;
CREATE DATABASE IF NOT EXISTS ${DB_NAME};
USE ${DB_NAME};
CREATE SCHEMA IF NOT EXISTS ${DB_SCHEMA};
CREATE USER IF NOT EXISTS ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
GRANT ALL ON DATABASE ${DB_NAME} TO ${DB_USER};
GRANT ALL ON SCHEMA ${DB_SCHEMA} TO ${DB_USER};
CREATE TABLE IF NOT EXISTS ${DB_SCHEMA}.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message VARCHAR(1000)
);
GRANT ALL ON ${DB_SCHEMA}.messages TO ${DB_USER};
"

# Execute SQL commands in CockroachDB
echo "$SQL_COMMANDS" | ./cockroach sql --certs-dir=./cockroach-certs --host=cockroach-db-public 
exit 0