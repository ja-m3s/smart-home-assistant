import os
import sys
import time
import psycopg2
from shared.helpers import print_message

db_host = os.getenv('DB_HOST')
db_port = os.getenv('DB_PORT')
db_name = os.getenv('DB_NAME')
db_schema = os.getenv('DB_SCHEMA')
db_user = os.getenv('DB_USER')
db_password = os.getenv('DB_PASSWORD')

def setup_db_connection(max_retries=None):
    """
    Set up Database connection.
    """
    retry_delay = 2  # seconds
    retry_count = 0

    print_message('dbhost: ' + db_host + ':' + db_port + '/' + db_name)
    print_message('db credentials: ' + db_user + '/' + db_password)

    while max_retries is None or retry_count < max_retries:
        try:
            # Establish connection to the database
            db_connection = psycopg2.connect(
                host=db_host,
                port=db_port,
                database=db_name,
                user=db_user,
                password=db_password
            )
            db_cursor = db_connection.cursor()
            print_message("Successfully established connection to database")
            return db_connection, db_cursor
        except psycopg2.Error as e:
            print_message(f"Failed to connect to Database: {e}")
            if max_retries is not None:
                print_message(f"Retry {retry_count + 1} of {max_retries}...")
            else:
                print_message(f"Retrying in {retry_delay} seconds...")
            time.sleep(retry_delay)
            retry_count += 1

    print_message("Failed to connect after retries.")
    return None, None

