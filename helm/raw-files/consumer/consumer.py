import os
import pika
import sys

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout

# Function to consume messages from RabbitMQ queue
def consume_messages():
    rabbitmq_host = os.getenv('RABBITMQ_HOST', 'localhost')
    rabbitmq_port = os.getenv('RABBITMQ_PORT', '5672')
    rabbitmq_user = os.getenv('RABBITMQ_USER', 'guest')
    rabbitmq_pass = os.getenv('RABBITMQ_PASS', 'guest')
    queue_name = os.getenv('RABBITMQ_QUEUE', 'my_queue')

    credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
    parameters = pika.ConnectionParameters(rabbitmq_host, int(rabbitmq_port), '/', credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # Declare the queue
    channel.queue_declare(queue=queue_name)

    try:
        def callback(ch, method, properties, body):
            # Message consumed successfully
            print_message('Received message: {}'.format(body.decode('utf-8')))

        # Consume messages from the queue
        channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=True)

        # Start consuming messages
        print_message('Waiting for messages. To exit press CTRL+C')
        channel.start_consuming()
    except KeyboardInterrupt:
        pass
    finally:
        # Close the connection
        connection.close()

if __name__ == '__main__':
    consume_messages()
