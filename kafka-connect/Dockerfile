FROM confluentinc/cp-kafka-connect-base:latest


#Copy properties files
COPY postgres-sink.properties /etc/kafka/

ENV CONNECT_BOOTSTRAP_SERVERS=event-bus-service.default.svc.cluster.local:9092 \
    CONNECT_REST_PORT="28082" \
    CONNECT_GROUP_ID="quickstart" \
    CONNECT_CONFIG_STORAGE_TOPIC="quickstart-config" \
    CONNECT_OFFSET_STORAGE_TOPIC="quickstart-offsets" \
    CONNECT_STATUS_STORAGE_TOPIC="quickstart-status" \
    CONNECT_KEY_CONVERTER="org.apache.kafka.connect.json.JsonConverter" \
    CONNECT_VALUE_CONVERTER="org.apache.kafka.connect.json.JsonConverter" \
    CONNECT_INTERNAL_KEY_CONVERTER="org.apache.kafka.connect.json.JsonConverter" \
    CONNECT_INTERNAL_VALUE_CONVERTER="org.apache.kafka.connect.json.JsonConverter" \
    CONNECT_REST_ADVERTISED_HOST_NAME="localhost" \
    CONNECT_PLUGIN_PATH="/usr/share/java"


# Install the JDBC connector plugin
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest

# Expose Kafka and Kafka Connect ports
EXPOSE 9092 9093 8083