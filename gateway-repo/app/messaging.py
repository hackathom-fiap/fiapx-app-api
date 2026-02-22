import pika
import json
import os
import ssl # Add this import
from urllib.parse import urlparse # Add this import

# Obtém as variáveis de ambiente individuais
MQ_USER = os.getenv("MQ_USER", "user")
MQ_PASSWORD = os.getenv("MQ_PASSWORD", "password")
MQ_HOST = os.getenv("MQ_HOST", "rabbitmq")
MQ_PORT = os.getenv("MQ_PORT", "5671") # Default to 5671 for Amazon MQ with TLS

QUEUE_NAME = "video_processing"

def send_to_queue(video_id: int, filename: str):
    # Construct RABBITMQ_URL directly using env vars
    RABBITMQ_URL_FULL = f"amqps://{MQ_USER}:{MQ_PASSWORD}@{MQ_HOST}:{MQ_PORT}/"
    url_components = urlparse(RABBITMQ_URL_FULL)

    mq_user = url_components.username
    mq_password = url_components.password
    mq_host = url_components.hostname
    mq_port = url_components.port if url_components.port else 5671 # Default to 5671 for amqps

    context = ssl.create_default_context()
    context.check_hostname = False
    context.verify_mode = ssl.CERT_NONE

    ssl_options = pika.SSLOptions(
        context=context
    )

    credentials = pika.PlainCredentials(mq_user, mq_password)

    connection_parameters = pika.ConnectionParameters(
        host=mq_host,
        port=mq_port,
        credentials=credentials,
        virtual_host="/", # Amazon MQ uses "/" as virtual host
        ssl_options=ssl_options
    )

    connection = None
    try:
        connection = pika.BlockingConnection(connection_parameters)
        channel = connection.channel()

        channel.queue_declare(queue=QUEUE_NAME, durable=True)

        message = {
            "video_id": video_id,
            "filename": filename
        }

        channel.basic_publish(
            exchange='',
            routing_key=QUEUE_NAME,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2,  # make message persistent
            )
        )
    finally:
        if connection:
            connection.close()
