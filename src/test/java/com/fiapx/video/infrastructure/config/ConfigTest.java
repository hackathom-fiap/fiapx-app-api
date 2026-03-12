package com.fiapx.video.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void awsConfig_shouldCreateS3Client() {
        AwsConfig awsConfig = new AwsConfig();
        S3Client s3Client = awsConfig.s3Client();
        assertNotNull(s3Client);
    }

    @Test
    void rabbitMqConfig_shouldCreateBeans() {
        RabbitMqConfig config = new RabbitMqConfig();
        
        Queue queue = config.queue();
        assertNotNull(queue);
        assertEquals(RabbitMqConfig.QUEUE_NAME, queue.getName());
        assertTrue(queue.isDurable());
        
        DirectExchange exchange = config.exchange();
        assertNotNull(exchange);
        assertEquals(RabbitMqConfig.EXCHANGE_NAME, exchange.getName());
        
        Binding binding = config.binding(queue, exchange);
        assertNotNull(binding);
        assertEquals(RabbitMqConfig.ROUTING_KEY, binding.getRoutingKey());
        
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}
