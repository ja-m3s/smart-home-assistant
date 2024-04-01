import threading
import pika
import json
import time
import psycopg2
from helpers import print_message, setup_connection, setup_db_connection
from env import rabbitmq_queue, hostname, db_schema

# Function to consume messages from RabbitMQ queue and insert records into the database
def consume_messages():

    # Establish connection to RabbitMQ
    connection, channel = setup_connection()

    db_connection, db_cursor = setup_db_connection()

    def callback(ch, method, properties, body):
        # Message consumed successfully
        message_body = json.loads(body.decode('utf-8'))  # Parse JSON string to dictionary
        print_message('Received message: {}'.format(message_body))
        
        # Insert record into the database
        db_cursor.execute("INSERT INTO " + db_schema + ".messages (name, type, current_state) VALUES (%s, %s, %s)", (message_body['name'], message_body['type'], message_body['current_state']))
        db_connection.commit()
        ch.basic_ack(delivery_tag=method.delivery_tag)

    # Consume messages from the queue
    channel.basic_consume(queue=rabbitmq_queue, on_message_callback=callback, auto_ack=False)

    # Start consuming messages
    print_message('Waiting for messages.')
    channel.start_consuming()

    # Close the connections
    db_cursor.close()
    db_connection.close()
    connection.close()

if __name__ == '__main__':
    consume_messages()
