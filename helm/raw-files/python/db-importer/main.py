import json
from shared.helpers import print_message, setup_consumer_connection
from database import setup_db_connection, db_schema

# Function to consume messages from RabbitMQ queue and insert records into the database
def consume_messages():
    try:
        # Establish connection to RabbitMQ
        _, channel, queue = setup_consumer_connection()

        # Establish DB connection
        _, db_cursor = setup_db_connection()

        def callback(_, __, ___, body):
            # Message consumed successfully
            message_body = json.loads(body.decode('utf-8'))  # Parse JSON string to dictionary
            print_message(f'Received message: {message_body}')

            # Insert record into the database
            query = f"INSERT INTO {db_schema}.messages (name, type, current_state) VALUES (%s, %s, %s)"
            params = (message_body['name'], message_body['type'], message_body['current_state'])
            print_message(f"Executing query: {query} with params: {params}")
            db_cursor.execute(query, params)
            db_cursor.connection.commit()

        # Consume messages from the queue
        channel.basic_consume(queue=queue, on_message_callback=callback, auto_ack=True)

        # Start consuming messages
        print_message('Waiting for messages.')
        channel.start_consuming()

    except Exception as e:
        print_message(f"Error: {e}")

if __name__ == '__main__':
    consume_messages()
