import threading
import pika
import json
import time
from shared.helpers import print_message, setup_producer_connection, setup_consumer_connection
from shared.env import hostname, state, data_template

def publish_messages():
    #Publish messages to RabbitMQ queue indefinitely.
    connection, channel = setup_producer_connection()

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
            channel.basic_publish(exchange='messages',routing_key='',body=json_data)

        except Exception as e:
            print_message(f"Error: {e} ")

def consume_light_monitor():
    print_message(hostname +" monitoring.")
    connection, channel,queue = setup_consumer_connection()

    try:
        def callback(ch, method, properties, body):
            # Message consumed successfully
            print_message('Received message: {}'.format(body.decode('utf-8')))
            ch.basic_ack(delivery_tag=method.delivery_tag)

            message = json.loads(body.decode('utf-8'))
            
            # Check if the state is 'triggered'
            if message.get('current_state') == 'triggered':
              global state
              state='off'
              print_message("turned light off")

        # Consume messages from the queue
        channel.basic_consume(queue=queue, on_message_callback=callback, auto_ack=False)

        # Start consuming messages
        print_message('Waiting for messages...')
        channel.start_consuming()
    except Exception as e:
            print_message(f"Error: {e} ")
            pass
    #finally:
        # Close the connection
        #connection.close()

def main():

    # Create threads for publishing and consuming messages
    publish_thread = threading.Thread(target=publish_messages, args=())
    consume_thread = threading.Thread(target=consume_light_monitor, args=())

    # Set threads as daemons
    publish_thread.daemon = True
    consume_thread.daemon = True

    # Start threads
    publish_thread.start()
    consume_thread.start()

    # Keep the main thread running
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pass

if __name__ == "__main__":
    main()