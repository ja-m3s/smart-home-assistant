FROM confluentinc/cp-kafka-connect-base:latest

WORKDIR /

#Copy properties files
COPY connect-standalone.properties .
COPY postgres-sink.properties .

# Install the JDBC connector plugin
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest

# Expose Kafka and Kafka Connect ports
EXPOSE 9092 9093 8083