import pika
import json
import time
import random
import sys
import os

# RabbitMQ broker configuration
rabbitmq_host = os.getenv('RABBITMQ_HOST', 'localhost')
rabbitmq_port = os.getenv('RABBITMQ_PORT', '5672')
rabbitmq_user = os.getenv('RABBITMQ_USER', 'guest')
rabbitmq_pass = os.getenv('RABBITMQ_PASS', 'guest')
queue_name = os.getenv('RABBITMQ_QUEUE', 'my_queue')

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout

# RabbitMQ connection parameters
credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
parameters = pika.ConnectionParameters(rabbitmq_host, int(rabbitmq_port), '/', credentials)
connection = pika.BlockingConnection(parameters)
channel = connection.channel()

# Declare the queue
channel.queue_declare(queue=queue_name)

# Initialize id counter
id_counter = 1

# Sample data to publish
data_template = {
    "name": "LIGHT",
    "type": "Producer",
    "current_state": "on"
}

# Publish messages to RabbitMQ queue indefinitely
while True:
    try:
        # Create a copy of the template
        data = data_template.copy()

        # Convert data to JSON format
        json_data = json.dumps(data)

        # Print the message to be sent
        print_message(json_data)

        # Publish message to RabbitMQ queue
        channel.basic_publish(exchange='', routing_key=queue_name, body=json_data)

        # Sleep for random seconds
        random_sleep_seconds = random.uniform(1, 20)
        time.sleep(random_sleep_seconds)
    except Exception as e:
        print_message("Error: {}".format(e))
        continue

# Close the connection
connection.close()
