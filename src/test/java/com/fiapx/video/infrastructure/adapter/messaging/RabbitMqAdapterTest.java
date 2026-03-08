package com.fiapx.video.infrastructure.adapter.messaging;

import com.fiapx.video.domain.entity.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMqAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @InjectMocks
    private RabbitMqAdapter rabbitMqAdapter;
    
    private Video video;
    private UUID videoId;
    
    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        
        video = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath("/storage/test.mp4")
                .status("PENDING")
                .username("testuser")
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void sendToProcessQueue_success() {
        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        
        rabbitMqAdapter.sendToProcessQueue(video);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("video-process-exchange"),
            eq("video.upload.event"),
            messageCaptor.capture()
        );
        
        Map<String, String> sentMessage = messageCaptor.getValue();
        assertEquals(videoId.toString(), sentMessage.get("id"));
        assertEquals("/storage/test.mp4", sentMessage.get("storagePath"));
        assertEquals("test@example.com", sentMessage.get("userEmail"));
        assertEquals("video/mp4", sentMessage.get("contentType"));
    }
    
    @Test
    void sendToProcessQueue_withNullFields() {
        Video videoWithNulls = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath(null)
                .status("PENDING")
                .username("testuser")
                .userEmail(null)
                .contentType(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        
        rabbitMqAdapter.sendToProcessQueue(videoWithNulls);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("video-process-exchange"),
            eq("video.upload.event"),
            messageCaptor.capture()
        );
        
        Map<String, String> sentMessage = messageCaptor.getValue();
        assertEquals(videoId.toString(), sentMessage.get("id"));
        assertEquals("", sentMessage.get("storagePath"));
        assertEquals("", sentMessage.get("userEmail"));
        assertEquals("", sentMessage.get("contentType"));
    }
    
    @Test
    void sendToProcessQueue_withEmptyFields() {
        Video videoWithEmptyFields = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath("")
                .status("PENDING")
                .username("testuser")
                .userEmail("")
                .contentType("")
                .createdAt(LocalDateTime.now())
                .build();
        
        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        
        rabbitMqAdapter.sendToProcessQueue(videoWithEmptyFields);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("video-process-exchange"),
            eq("video.upload.event"),
            messageCaptor.capture()
        );
        
        Map<String, String> sentMessage = messageCaptor.getValue();
        assertEquals(videoId.toString(), sentMessage.get("id"));
        assertEquals("", sentMessage.get("storagePath"));
        assertEquals("", sentMessage.get("userEmail"));
        assertEquals("", sentMessage.get("contentType"));
    }
    
    @Test
    void sendToProcessQueue_verifyExchangeAndRoutingKey() {
        rabbitMqAdapter.sendToProcessQueue(video);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("video-process-exchange"),
            eq("video.upload.event"),
            any(Map.class)
        );
    }
    
    @Test
    void sendToProcessQueue_multipleCalls() {
        Video video2 = Video.builder()
                .id(UUID.randomUUID())
                .title("Test Video 2")
                .originalFileName("test2.mp4")
                .storagePath("/storage/test2.mp4")
                .status("PENDING")
                .username("testuser")
                .userEmail("test2@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
        
        rabbitMqAdapter.sendToProcessQueue(video);
        rabbitMqAdapter.sendToProcessQueue(video2);
        
        verify(rabbitTemplate, times(2)).convertAndSend(
            eq("video-process-exchange"),
            eq("video.upload.event"),
            any(Map.class)
        );
    }
    
    @Test
    void sendToProcessQueue_messageStructure() {
        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        
        rabbitMqAdapter.sendToProcessQueue(video);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("video-process-exchange"),
                eq("video.upload.event"),
                messageCaptor.capture()
        );
        
        Map<String, String> sentMessage = messageCaptor.getValue();
        
        assertNotNull(sentMessage);
        assertEquals(4, sentMessage.size());
        assertTrue(sentMessage.containsKey("id"));
        assertTrue(sentMessage.containsKey("storagePath"));
        assertTrue(sentMessage.containsKey("userEmail"));
        assertTrue(sentMessage.containsKey("contentType"));
    }
}
