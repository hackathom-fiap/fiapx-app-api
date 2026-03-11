package com.fiapx.video.infrastructure.adapter.messaging;

import com.fiapx.video.domain.entity.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMqAdapter rabbitMqAdapter;

    @Test
    void shouldSendToProcessQueueSuccessfully() {
        Video video = Video.builder()
                .id(UUID.randomUUID())
                .storagePath("path/to/video.mp4")
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .build();

        rabbitMqAdapter.sendToProcessQueue(video);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("video-process-exchange"),
                eq("video.upload.event"),
                any(Map.class)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendToProcessQueueWithNullFields() {
        Video video = Video.builder()
                .id(UUID.randomUUID())
                .storagePath(null)
                .userEmail(null)
                .contentType(null)
                .build();

        rabbitMqAdapter.sendToProcessQueue(video);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("video-process-exchange"),
                eq("video.upload.event"),
                (Object) argThat(argument -> {
                    Map<String, String> map = (Map<String, String>) argument;
                    return "".equals(map.get("storagePath")) &&
                           "".equals(map.get("userEmail")) &&
                           "".equals(map.get("contentType"));
                })
        );
    }
}
