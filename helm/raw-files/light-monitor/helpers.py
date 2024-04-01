import pika
import sys
import time

from env import rabbitmq_user, rabbitmq_pass, rabbitmq_host, rabbitmq_port, rabbitmq_queue

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout


def setup_connection(max_retries=None):
    """Set up RabbitMQ connection and channel with configurable retry."""
    retry_delay = 2  # seconds
    retry_count = 0

    print_message(rabbitmq_host)
    print_message(rabbitmq_port)
    print_message(rabbitmq_user)
    print_message(rabbitmq_pass)
    print_message(rabbitmq_queue)


    while max_retries is None or retry_count < max_retries:
        try:
            credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
            parameters = pika.ConnectionParameters(rabbitmq_host, int(rabbitmq_port), '/', credentials)
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()

            # Declare the queue
            channel.queue_declare(queue=rabbitmq_queue, durable=True)
            channel.basic_qos(prefetch_count=1)  # Limit each consumer to one message at a time

            print_message("Connection to RabbitMQ established successfully.")
            return connection, channel
        except pika.exceptions.AMQPConnectionError as e:
            print_message(f"Failed to connect to RabbitMQ: {e}")
            if max_retries is not None:
                print_message(f"Retry {retry_count + 1} of {max_retries}...")
            else:
                print_message(f"Retrying in {retry_delay} seconds...")
            time.sleep(retry_delay)
            retry_count += 1

    print_message("Failed to connect after retries.")
    return None, None
