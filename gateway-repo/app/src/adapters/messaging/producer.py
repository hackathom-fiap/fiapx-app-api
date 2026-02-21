import os
import pika
import json
from src.ports.interfaces import MessageBroker

class RabbitMQProducer(MessageBroker):
    def __init__(self, rabbitmq_url: str, queue_name: str):
        self.rabbitmq_url = rabbitmq_url
        self.queue_name = queue_name

        if not self.rabbitmq_url:
            raise ValueError("RABBITMQ_URL must be provided.")

    def publish_video_processing(self, video_id: int, filename: str):
        try:
            # Parse the URL to get components
            url_params = pika.URLParameters(self.rabbitmq_url)

            # Construct ConnectionParameters with extracted components and explicit SSL
            connection_params = pika.ConnectionParameters(
                host=url_params.host,
                port=url_params.port,
                virtual_host=url_params.virtual_host,
                credentials=url_params.credentials,
                ssl=True  # Ensure SSL is enabled for Amazon MQ
            )

            connection = pika.BlockingConnection(connection_params)
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