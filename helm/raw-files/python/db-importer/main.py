import pika
import json
import psycopg2
from shared.helpers import print_message, setup_consumer_connection
from database import setup_db_connection, db_schema

# Function to consume messages from RabbitMQ queue and insert records into the database
def consume_messages():
    connection = None
    channel = None
    db_connection = None
    db_cursor = None

    try:
        # Establish connection to RabbitMQ
        connection, channel, queue = setup_consumer_connection()

        # Establish DB connection
        db_connection, db_cursor = setup_db_connection()

        def callback(ch, method, properties, body):
            # Message consumed successfully
            message_body = json.loads(body.decode('utf-8'))  # Parse JSON string to dictionary
            print_message('Received message: {}'.format(message_body))

            # Insert record into the database
            query = "INSERT INTO {} (name, type, current_state) VALUES (%s, %s, %s)".format(db_schema + ".messages")
            params = (message_body['name'], message_body['type'], message_body['current_state'])
            print_message("Executing query: {} with params: {}".format(query, params))
            db_cursor.execute(query, params)
            db_connection.commit()

        # Consume messages from the queue
        channel.basic_consume(queue=queue, on_message_callback=callback, auto_ack=True)

        # Start consuming messages
        print_message('Waiting for messages.')
        channel.start_consuming()

    except Exception as e:
        print_message(f"Error: {e}")
    finally:
        # Close the connections
        #if connection:
            #connection.close()
        if db_cursor:
            db_cursor.close()
        if db_connection:
            db_connection.close()

if __name__ == '__main__':
    consume_messages()
