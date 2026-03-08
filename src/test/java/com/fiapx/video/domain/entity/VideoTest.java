package com.fiapx.video.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VideoTest {

    @Test
    void builder_success() {
        UUID videoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        Video video = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath("/storage/test.mp4")
                .status("PENDING")
                .username("testuser")
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(createdAt)
                .build();
        
        assertNotNull(video);
        assertEquals(videoId, video.getId());
        assertEquals("Test Video", video.getTitle());
        assertEquals("test.mp4", video.getOriginalFileName());
        assertEquals("/storage/test.mp4", video.getStoragePath());
        assertEquals("PENDING", video.getStatus());
        assertEquals("testuser", video.getUsername());
        assertEquals("test@example.com", video.getUserEmail());
        assertEquals("video/mp4", video.getContentType());
        assertEquals(createdAt, video.getCreatedAt());
    }
    
    @Test
    void builder_withNullValues() {
        Video video = Video.builder()
                .id(null)
                .title(null)
                .originalFileName(null)
                .storagePath(null)
                .status(null)
                .username(null)
                .userEmail(null)
                .contentType(null)
                .createdAt(null)
                .build();
        
        assertNotNull(video);
        assertNull(video.getId());
        assertNull(video.getTitle());
        assertNull(video.getOriginalFileName());
        assertNull(video.getStoragePath());
        assertNull(video.getStatus());
        assertNull(video.getUsername());
        assertNull(video.getUserEmail());
        assertNull(video.getContentType());
        assertNull(video.getCreatedAt());
    }
    
    @Test
    void builder_withPartialValues() {
        UUID videoId = UUID.randomUUID();
        
        Video video = Video.builder()
                .id(videoId)
                .title("Test Video")
                .status("PENDING")
                .build();
        
        assertNotNull(video);
        assertEquals(videoId, video.getId());
        assertEquals("Test Video", video.getTitle());
        assertEquals("PENDING", video.getStatus());
        assertNull(video.getOriginalFileName());
        assertNull(video.getStoragePath());
        assertNull(video.getUsername());
        assertNull(video.getUserEmail());
        assertNull(video.getContentType());
        assertNull(video.getCreatedAt());
    }
    
    @Test
    void settersAndGetters_success() {
        Video video = new Video();
        UUID videoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        video.setId(videoId);
        video.setTitle("Updated Video");
        video.setOriginalFileName("updated.mp4");
        video.setStoragePath("/updated/path.mp4");
        video.setStatus("COMPLETED");
        video.setUsername("updateduser");
        video.setUserEmail("updated@example.com");
        video.setContentType("video/mp4");
        video.setCreatedAt(createdAt);
        
        assertEquals(videoId, video.getId());
        assertEquals("Updated Video", video.getTitle());
        assertEquals("updated.mp4", video.getOriginalFileName());
        assertEquals("/updated/path.mp4", video.getStoragePath());
        assertEquals("COMPLETED", video.getStatus());
        assertEquals("updateduser", video.getUsername());
        assertEquals("updated@example.com", video.getUserEmail());
        assertEquals("video/mp4", video.getContentType());
        assertEquals(createdAt, video.getCreatedAt());
    }
    
    @Test
    void allArgsConstructor_success() {
        UUID videoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        Video video = new Video(
            videoId,
            "Test Video",
            "test.mp4",
            "/storage/test.mp4",
            "PENDING",
            "testuser",
            "test@example.com",
            "video/mp4",
            createdAt,
            null
        );
        
        assertNotNull(video);
        assertEquals(videoId, video.getId());
        assertEquals("Test Video", video.getTitle());
        assertEquals("test.mp4", video.getOriginalFileName());
        assertEquals("/storage/test.mp4", video.getStoragePath());
        assertEquals("PENDING", video.getStatus());
        assertEquals("testuser", video.getUsername());
        assertEquals("test@example.com", video.getUserEmail());
        assertEquals("video/mp4", video.getContentType());
        assertEquals(createdAt, video.getCreatedAt());
        assertNull(video.getUpdatedAt());
    }
    
    @Test
    void noArgsConstructor_success() {
        Video video = new Video();
        
        assertNotNull(video);
        assertNull(video.getId());
        assertNull(video.getTitle());
        assertNull(video.getOriginalFileName());
        assertNull(video.getStoragePath());
        assertNull(video.getStatus());
        assertNull(video.getUsername());
        assertNull(video.getUserEmail());
        assertNull(video.getContentType());
        assertNull(video.getCreatedAt());
        assertNull(video.getUpdatedAt());
    }
    
    @Test
    void toString_containsExpectedFields() {
        UUID videoId = UUID.randomUUID();
        Video video = Video.builder()
                .id(videoId)
                .title("Test Video")
                .status("PENDING")
                .build();
        
        String videoString = video.toString();
        
        assertNotNull(videoString);
        assertTrue(videoString.contains("Test Video"));
        assertTrue(videoString.contains("PENDING"));
        assertTrue(videoString.contains(videoId.toString()));
    }
}
