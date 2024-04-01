import pika
import sys
import time
import psycopg2
from env import rabbitmq_user, rabbitmq_pass, rabbitmq_host, rabbitmq_port, rabbitmq_queue, db_host, db_port, db_name, db_user, db_password

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout


def setup_db_connection(max_retries=None):
    """Set up Database connection."""
    retry_delay = 2  # seconds
    retry_count = 0

    print_message(db_host)
    print_message(db_port)
    print_message(db_name)
    print_message(db_user)
    print_message(db_password)

    while max_retries is None or retry_count < max_retries:
        try:
                # Establish connection to the database
            db_connection = psycopg2.connect(
                host=db_host,
                port=db_port,
                database=db_name,
                user=db_user,
                password=db_password
            )
            db_cursor = db_connection.cursor()
            print_message("Successfully established connection to database")
            return db_connection, db_cursor
        except Exception as e:
            print_message(f"Failed to connect to Database: {e}")
            if max_retries is not None:
                print_message(f"Retry {retry_count + 1} of {max_retries}...")
            else:
                print_message(f"Retrying in {retry_delay} seconds...")
            time.sleep(retry_delay)
            retry_count += 1

    print_message("Failed to connect after retries.")
    return None, None

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
