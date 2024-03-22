#!/usr/bin/env bash

docker run -p 8080:80 nginx
docker run -p 8081:80 kafka-producer
docker run -p 8082:80 kafka-consumer
docker run --name postgres-container \
    -e POSTGRES_PASSWORD=secretpassword \
    -p 5432:5432 postgres