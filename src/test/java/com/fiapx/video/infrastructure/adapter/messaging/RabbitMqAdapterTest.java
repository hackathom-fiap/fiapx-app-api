package com.fiapx.video.infrastructure.adapter.messaging;

import com.fiapx.video.domain.entity.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                any(java.util.Map.class)
        );
    }

    @Test
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
                argThat(map -> 
                    map.get("storagePath").equals("") &&
                    map.get("userEmail").equals("") &&
                    map.get("contentType").equals("")
                )
        );
    }
}
