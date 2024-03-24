FROM bitnami/kafka:latest
USER root
RUN apt update && apt install -y zip
USER 1001
RUN mkdir -p /opt/bitnami/kafka/plugins
WORKDIR /opt/bitnami/kafka/plugins
COPY confluentinc-kafka-connect-jdbc-10.7.6.zip .
RUN unzip confluentinc-kafka-connect-jdbc-10.7.6.zip
WORKDIR /