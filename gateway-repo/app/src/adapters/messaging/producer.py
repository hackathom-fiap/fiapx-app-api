import os
import pika
import json
from src.ports.interfaces import MessageBroker

class RabbitMQProducer(MessageBroker):
    def __init__(self):
        self.host = os.getenv("MQ_HOST")
        self.port = os.getenv("MQ_PORT")
        self.user = os.getenv("MQ_USER")
        self.password = os.getenv("MQ_PASSWORD")
        self.queue_name = "video-processing"

        if not all([self.host, self.port, self.user, self.password]):
            raise ValueError("MQ_HOST, MQ_PORT, MQ_USER, and MQ_PASSWORD must be set as environment variables")

    def publish_video_processing(self, video_id: int, filename: str):
        try:
            credentials = pika.PlainCredentials(self.user, self.password)
            params = pika.ConnectionParameters(
                host=self.host,
                port=int(self.port),
                virtual_host="/",
                credentials=credentials,
                ssl=True  # Habilita TLS para conexões seguras
            )
            connection = pika.BlockingConnection(params)
            channel = connection.channel()
            channel.queue_declare(queue=self.queue_name, durable=True)
            
            message = {"video_id": video_id, "filename": filename}
            
            channel.basic_publish(
                exchange='',
                routing_key=self.queue_name,
                body=json.dumps(message),
                properties=pika.BasicProperties(delivery_mode=2)
            )
            print(f"Message published to queue '{self.queue_name}': {message}")
        except pika.exceptions.AMQPConnectionError as e:
            print(f"Failed to connect to RabbitMQ: {e}")
        except Exception as e:
            print(f"An error occurred: {e}")
        finally:
            try:
                connection.close()
            except Exception:
                pass