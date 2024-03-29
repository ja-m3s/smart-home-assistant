from confluent_kafka import Producer
import json
import time
import random
import sys
import os

# Kafka broker configuration
bootstrap_servers = os.getenv('BOOTSTRAP_SERVERS')
topic = os.getenv('TOPIC')

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout

# Create Kafka producer configuration
conf = {'bootstrap.servers': bootstrap_servers}

# Create Kafka producer instance
producer = Producer(conf)

# Initialize id counter
id_counter = 1

# Sample data to publish
data_template = {
    "schema": {
        "type": "struct",
        "fields": [
            {"type": "int32", "optional": False, "field": "id", "primaryKey": True},
            {"type": "string", "optional": False, "field": "name"},
            {"type": "string", "optional": False, "field": "type"},
            {"type": "string", "optional": False, "field": "current_state"}
        ]
    },
    "payload": {
        "id": "",  # Placeholder for dynamic id
        "name": "LIGHT",
        "type": "Producer",
        "current_state": "on"
    }
}

# Publish messages to Kafka topic indefinitely
while True:
    try:
        # Create a copy of the template
        data = data_template.copy() 

        # Update the id field with the incremented value, wrapped in double quotes
        data["payload"]["id"] = str(id_counter)

        # Convert data to JSON format
        json_data = json.dumps(data)

        # Print the message to be sent
        print_message(json_data)

        # Publish message to Kafka topic
        producer.produce(topic, value=json_data.encode('utf-8'))

        # Increment id counter
        id_counter += 1

        # Sleep for random seconds
        random_sleep_seconds = random.uniform(1, 20)
        time.sleep(random_sleep_seconds)
    except Exception as e:
        print_message("Error: {}".format(e))
        continue

# Close the producer
producer.flush()  # Flush any remaining messages
producer.close()
