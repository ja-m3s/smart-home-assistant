FROM bitnami/kafka:latest

USER root
RUN apt update && apt install -y zip
USER 1001

#Copy jdbc connector
RUN mkdir -p /opt/bitnami/kafka/plugins

WORKDIR /opt/bitnami/kafka/plugins
COPY confluentinc-kafka-connect-jdbc-10.7.6.zip .
RUN unzip confluentinc-kafka-connect-jdbc-10.7.6.zip
WORKDIR /

# Expose Kafka and Kafka Connect ports
EXPOSE 9092 9093 8083