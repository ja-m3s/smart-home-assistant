import kafka
import json

consumer = kafka.KafkaConsumer(bootstrap_servers='kafka-broker:9092', group_id='my-group')
consumer.subscribe(['event-topic'])

def subscribe_to_events():
    for message in consumer:
        data = json.loads(message.value)
        # Process the event and trigger appropriate actions based on the data