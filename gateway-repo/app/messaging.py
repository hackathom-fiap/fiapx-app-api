import pika
import json
import os

# Obtém as variáveis de ambiente individuais
MQ_USER = os.getenv("MQ_USER", "user")
MQ_PASSWORD = os.getenv("MQ_PASSWORD", "password")
MQ_HOST = os.getenv("MQ_HOST", "rabbitmq")
MQ_PORT = os.getenv("MQ_PORT", "5672")

# Constrói a URL de conexão do RabbitMQ (usando amqps para o Amazon MQ)
RABBITMQ_URL = f"amqps://{MQ_USER}:{MQ_PASSWORD}@{MQ_HOST}:{MQ_PORT}/"
QUEUE_NAME = "video_processing"

def send_to_queue(video_id: int, filename: str):
    params = pika.URLParameters(RABBITMQ_URL)
    connection = pika.BlockingConnection(params)
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
    connection.close()
