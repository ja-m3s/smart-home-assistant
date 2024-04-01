import threading
import pika
import json
import time
from helpers import print_message, setup_connection
from env import rabbitmq_queue, hostname, state, data_template

def publish_messages(channel):
    """Publish messages to RabbitMQ queue indefinitely."""
    while True:
        try:
            # Sleep for three seconds
            time.sleep(3)

            data = data_template.copy()
            data['name'] = hostname
            data['current_state'] = state

            # Convert data to JSON format
            json_data = json.dumps(data)

            # Print the message to be sent
            print_message("Sending Message: " + json_data)

            # Publish message to RabbitMQ queue
            channel.basic_publish(exchange='', routing_key=rabbitmq_queue, body=json_data)

        except Exception as e:
            print_message(f"Error: {e} ")

def consume_messages(channel):
    """Consume messages from RabbitMQ queue."""
    def callback(ch, method, properties, body):
        """Callback function to handle received messages."""
        message = json.loads(body)
        if message.get('current_state') == 'triggered':
            print_message(f"Received triggered message: {body}")
            # Update the state variable to 'off' 
            state = 'off'
        else:
            print_message(f"Ignoring message: {body}")

    # Set up the consumer
    channel.basic_consume(queue=rabbitmq_queue, on_message_callback=callback, auto_ack=True)

    # Start consuming messages
    print_message("Waiting for messages...")
    channel.start_consuming()

def main():
    # Setup RabbitMQ connection and channel
    connection, channel = setup_connection()

    # Create threads for publishing and consuming messages
    publish_thread = threading.Thread(target=publish_messages, args=(channel,))
    consume_thread = threading.Thread(target=consume_messages, args=(channel,))

    # Set threads as daemons
    publish_thread.daemon = True
    consume_thread.daemon = True

    # Start threads
    publish_thread.start()
    #consume_thread.start()

    # Keep the main thread running
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pass
    finally:
        connection.close()

if __name__ == "__main__":
    main()
