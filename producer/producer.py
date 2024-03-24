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

# Sample data to publish
data = {'id': 1, 'name': 'LIGHTBULB', 'type': 'producer', 'current_state': 'off'}

# Publish messages to Kafka topic indefinitely
while True:
    try:
        # Randomly choose whether to change state to 'on'
        if random.random() < 0.1:  # Adjust the probability as needed
            data['current_state'] = 'off'
        else:
            data['current_state'] = 'on'
        
        # Send message to Kafka
        producer.produce(topic, json.dumps(data).encode('utf-8'))
        producer.flush()  # Ensure message is sent immediately
        
        print_message("Message sent: {}".format(data))
        
        random_sleep_seconds = random.uniform(1, 20)
        time.sleep(random_sleep_seconds)
    except Exception as e:
        print_message("Error: {}".format(e))
        continue
    except KeyboardInterrupt:
        print_message("Stopping message publishing.")
        break  # Exit the loop on KeyboardInterrupt

# Close the producer
producer.flush()  # Flush any remaining messages
producer.close()
