import sys
import time
import pika  # Add import for pika module
from env import rabbitmq_user, rabbitmq_pass, rabbitmq_host, rabbitmq_port

def print_message(message):
    """
    Function to print messages with timestamp.
    """
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    print(f"{timestamp}: {message}")
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout

def setup_producer_connection(max_retries=None):
    """
    Set up RabbitMQ connection and channel with configurable retry.
    """
    retry_delay = 2  # seconds
    retry_count = 0

    print_message('host:' + rabbitmq_host + ':' + rabbitmq_port)
    print_message('credentials:' + rabbitmq_user + '/' + rabbitmq_pass)

    while max_retries is None or retry_count < max_retries:
        try:
            credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
            parameters = pika.ConnectionParameters(rabbitmq_host, int(rabbitmq_port), '/', credentials)
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()
            channel.exchange_declare(exchange='messages', exchange_type='fanout')
            print_message("Producer Connection to RabbitMQ established successfully.")
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

def setup_consumer_connection(max_retries=None):
    """
    Set up RabbitMQ connection and channel with configurable retry.
    """
    retry_delay = 2  # seconds
    retry_count = 0

    print_message('host:' + rabbitmq_host + ':' + rabbitmq_port)
    print_message('credentials:' + rabbitmq_user + '/' + rabbitmq_pass)

    while max_retries is None or retry_count < max_retries:
        try:
            credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
            parameters = pika.ConnectionParameters(rabbitmq_host, int(rabbitmq_port), '/', credentials)
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()

            # Declare the queue
            channel.exchange_declare(exchange='messages', exchange_type='fanout')
            result = channel.queue_declare(queue='', exclusive=False)
            generated_queue = result.method.queue
            print_message(generated_queue)
            channel.queue_bind(exchange='messages', queue=generated_queue)

            print_message("Consumer Connection to RabbitMQ established successfully.")
            return connection, channel, generated_queue
        except pika.exceptions.AMQPConnectionError as e:
            print_message(f"Failed to connect to RabbitMQ: {e}")
            if max_retries is not None:
                print_message(f"Retry {retry_count + 1} of {max_retries}...")
            else:
                print_message(f"Retrying in {retry_delay} seconds...")
            time.sleep(retry_delay)
            retry_count += 1

    print_mes
