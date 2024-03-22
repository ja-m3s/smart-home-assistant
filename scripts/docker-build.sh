#!/usr/bin/env bash
docker build -t nginx --file ../http-server/Dockerfile .
docker build -t kafka-producer --file ../producer/Dockerfile .
docker build -t kafka-consumer --file ../consumer/Dockerfile .
docker build -t postgres --file ../database/Dockerfile .