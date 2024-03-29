import os
from confluent_kafka import Consumer, KafkaError
import sys

# Function to print messages
def print_message(message):
    print(message)
    sys.stdout.flush()  # Ensure the message is immediately flushed to stdout

# Function to consume messages from Kafka topic
def consume_messages():
    bootstrap_servers = os.getenv('BOOTSTRAP_SERVERS')
    group_id = os.getenv('GROUP_ID')
    topic = os.getenv('TOPIC')

    consumer = Consumer({
        'bootstrap.servers': bootstrap_servers,
        'group.id': group_id,
        'auto.offset.reset': 'earliest'
    })

    consumer.subscribe([topic])

    try:
        while True:
            msg = consumer.poll(1.0)
            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    # End of partition event
                    print_message('%% %s [%d] reached end at offset %d\n' %
                        (msg.topic(), msg.partition(), msg.offset()))
                else:
                    print_message("Kafka Error: {}".format(msg.error()))
            else:
                # Message consumed successfully
                print_message('Received message: {}'.format(msg.value().decode('utf-8')))
    except KeyboardInterrupt:
        pass
    finally:
        # Close the consumer
        consumer.close()

if __name__ == '__main__':
    consume_messages()