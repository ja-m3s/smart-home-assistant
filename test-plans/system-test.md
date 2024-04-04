# System Test

## Pre-Test

Run ./helm-deploy.sh to deploy the applications to K8S.

## Tests - Light Bulb

- Sends Messages containing details of the light bulb
- Receives messages from the light bulb monitor
- Turns the light off when it receives a message from the light bulb monitor

## Tests - Light Bulb Monitor

- Receives all light bulb messages
- Sends a message containg the light bulb hostname as 'target' when it detects a light bulb has been on longer than the light limit

## Tests - DBImporter

- Inserts all messages on the RabbitMQ exchange into the database (once)

## Tests Database

- Has messages written to the messages table on the table.

## Tests Prometheus

- Applied correctly to cluster

## Tests Grafana

- Applied correctly to cluster
- Prometheus is added as a datasource

## Tests Secure Client Cockroach

- Applys the schema to the database
- Can manipulate the database
