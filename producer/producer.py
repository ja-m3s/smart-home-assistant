import kafka
import json

producer = kafka.KafkaProducer(bootstrap_servers='kafka-broker:9092')

def publish_event(topic, data):
    producer.send(topic, json.dumps(data))