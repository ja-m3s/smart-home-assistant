import os

# Environment variables
hostname = os.getenv('HOSTNAME')
STATE = 'on'
data_template = {
    "name": "unset",
    "type": "light",
    "current_state": "unset"
}

rabbitmq_host = os.getenv('RABBITMQ_HOST')
rabbitmq_port = os.getenv('RABBITMQ_PORT')
rabbitmq_user = os.getenv('RABBITMQ_USER')
rabbitmq_pass = os.getenv('RABBITMQ_PASS')
