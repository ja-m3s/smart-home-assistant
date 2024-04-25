# CockroachDB Secure Client

## Brief Overview

This is a system which functions as the schema management and database administration console. On pod creation, it
creates or updates the database schema used by the suite of applications. 

## How to build

This is not a built application.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/./helm-apply.sh
```
## Technologies

The application uses the RabbitMQ docker image.
