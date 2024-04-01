import threading
import pika
import json
import time
from helpers import print_message, setup_connection
from env import hostname, data_template

# Function to consume messages from RabbitMQ queue
def consume_messages():
    print_message(hostname +" monitoring.")
    connection, channel, queue = setup_connection()

    try:
        def callback(ch, method, properties, body):
            # Message consumed successfully
            print_message('Received message: {}'.format(body.decode('utf-8')))
            ch.basic_ack(delivery_tag=method.delivery_tag)

            message = json.loads(body.decode('utf-8'))
            
            # Check if the state is 'on'
            if message.get('current_state') == 'on':
                # Modify the data_template to send a new message
                new_message = data_template.copy()
                new_message['name'] = hostname
                new_message['current_state'] = 'triggered'  # Modify this as needed

                # Convert data to JSON format
                json_data = json.dumps(new_message)

                # Publish the new message back to the queue
                channel.basic_publish(exchange='messages', routing_key='', body=json_data)
                print_message('Published new message: {}'.format(json_data))

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

if __name__ == '__main__':
    consume_messages()

