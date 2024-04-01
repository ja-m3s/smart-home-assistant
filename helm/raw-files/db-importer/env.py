import os

hostname = os.getenv('HOSTNAME')

rabbitmq_host = os.getenv('RABBITMQ_HOST')
rabbitmq_port = os.getenv('RABBITMQ_PORT')
rabbitmq_user = os.getenv('RABBITMQ_USER')
rabbitmq_pass = os.getenv('RABBITMQ_PASS')
rabbitmq_queue = os.getenv('RABBITMQ_QUEUE')
db_host = os.getenv('DB_HOST')
db_port = os.getenv('DB_PORT')
db_name = os.getenv('DB_NAME')
db_schema = os.getenv('DB_SCHEMA')
db_user = os.getenv('DB_USER')
db_password = os.getenv('DB_PASSWORD')