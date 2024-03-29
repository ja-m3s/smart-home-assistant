#!/bin/bash

# Check if required variables are set
if [ -z "$DB_NAME" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
    echo "Error: DB_NAME, DB_USER, and DB_PASSWORD environment variables must be set."
    exit 1
fi

# SQL commands
SQL_COMMANDS="CREATE DATABASE ${DB_NAME};
\c ${DB_NAME};
CREATE SCHEMA s_smart_home;
CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
GRANT CONNECT ON DATABASE ${DB_NAME} TO ${DB_USER};
GRANT USAGE ON SCHEMA s_smart_home TO ${DB_USER};
CREATE TABLE s_smart_home.messages (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(255),
    current_state VARCHAR(255),
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
GRANT INSERT ON s_smart_home.messages TO event_bus_connector;
GRANT USAGE, SELECT ON SEQUENCE s_smart_home.messages_id_seq TO event_bus_connector;
"

# Execute SQL commands in psql as postgres user
echo "$SQL_COMMANDS" | psql -U postgres
