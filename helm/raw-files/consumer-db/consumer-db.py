import os
import pika
import sys
import psycopg2
import json

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout

# Function to consume messages from RabbitMQ queue and insert records into the database
def consume_messages():
    rabbitmq_host = os.getenv('RABBITMQ_HOST', 'event-bus-service')
    rabbitmq_port = os.getenv('RABBITMQ_PORT', '5672')
    rabbitmq_user = os.getenv('RABBITMQ_USER', 'guest')
    rabbitmq_pass = os.getenv('RABBITMQ_PASS', 'guest')
    queue_name = os.getenv('RABBITMQ_QUEUE', 'topic_producer')

    db_host = os.getenv('DB_HOST', 'database-service.default.svc.cluster.local')
    db_port = os.getenv('DB_PORT', '5432')
    db_name = os.getenv('DB_NAME', 'db_smart_home')
    db_schema = os.getenv('DB_SCHEMA', 's_smart_home')
    db_user = os.getenv('DB_USER', 'event_bus_connector')
    db_password = os.getenv('DB_PASSWORD', 'password')

    # Establish connection to RabbitMQ
    credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
    parameters = pika.ConnectionParameters(rabbitmq_host, int(rabbitmq_port), '/', credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # Declare the queue
    channel.queue_declare(queue=queue_name)

    # Establish connection to the database
    db_connection = psycopg2.connect(
        host=db_host,
        port=db_port,
        database=db_name,
        user=db_user,
        password=db_password
    )
    db_cursor = db_connection.cursor()

    def callback(ch, method, properties, body):
        # Message consumed successfully
        message_body = json.loads(body.decode('utf-8'))  # Parse JSON string to dictionary
        print_message('Received message: {}'.format(message_body))
        
        # Insert record into the database
        db_cursor.execute("INSERT INTO " + db_schema + ".messages (name, type, current_state) VALUES (%s, %s, %s)", (message_body['name'], message_body['type'], message_body['current_state']))
        db_connection.commit()

    # Consume messages from the queue
    channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=True)

    # Start consuming messages
    print_message('Waiting for messages. To exit press CTRL+C')
    channel.start_consuming()

    # Close the connections
    db_cursor.close()
    db_connection.close()
    connection.close()

if __name__ == '__main__':
    consume_messages()
