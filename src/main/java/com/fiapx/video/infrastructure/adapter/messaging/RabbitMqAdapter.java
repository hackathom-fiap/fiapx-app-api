package com.fiapx.video.infrastructure.adapter.messaging;

import java.util.Map;
import com.fiapx.video.domain.service.MessageBrokerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqAdapter implements MessageBrokerPort {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE_NAME = "video-process-exchange";
    private static final String ROUTING_KEY = "video.upload.event";

    @Override
    public void sendToProcessQueue(com.fiapx.video.domain.entity.Video video) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, java.util.Map.of(
                "id", video.getId().toString(),
                "storagePath", video.getStoragePath() == null ? "" : video.getStoragePath(),
                "userEmail", video.getUserEmail() == null ? "" : video.getUserEmail(),
                "contentType", video.getContentType() == null ? "" : video.getContentType()
        ));
    }
}
