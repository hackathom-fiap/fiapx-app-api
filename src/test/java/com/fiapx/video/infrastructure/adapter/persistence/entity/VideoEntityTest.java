package com.fiapx.video.infrastructure.adapter.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VideoEntityTest {

    @Test
    void builder_success() {
        UUID videoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        VideoEntity videoEntity = VideoEntity.builder()
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
        
        assertNotNull(videoEntity);
        assertEquals(videoId, videoEntity.getId());
        assertEquals("Test Video", videoEntity.getTitle());
        assertEquals("test.mp4", videoEntity.getOriginalFileName());
        assertEquals("/storage/test.mp4", videoEntity.getStoragePath());
        assertEquals("PENDING", videoEntity.getStatus());
        assertEquals("testuser", videoEntity.getUsername());
        assertEquals("test@example.com", videoEntity.getUserEmail());
        assertEquals("video/mp4", videoEntity.getContentType());
        assertEquals(createdAt, videoEntity.getCreatedAt());
    }
    
    @Test
    void builder_withNullValues() {
        VideoEntity videoEntity = VideoEntity.builder()
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
        
        assertNotNull(videoEntity);
        assertNull(videoEntity.getId());
        assertNull(videoEntity.getTitle());
        assertNull(videoEntity.getOriginalFileName());
        assertNull(videoEntity.getStoragePath());
        assertNull(videoEntity.getStatus());
        assertNull(videoEntity.getUsername());
        assertNull(videoEntity.getUserEmail());
        assertNull(videoEntity.getContentType());
        assertNull(videoEntity.getCreatedAt());
    }
    
    @Test
    void builder_withPartialValues() {
        UUID videoId = UUID.randomUUID();
        
        VideoEntity videoEntity = VideoEntity.builder()
                .id(videoId)
                .title("Test Video")
                .status("PENDING")
                .build();
        
        assertNotNull(videoEntity);
        assertEquals(videoId, videoEntity.getId());
        assertEquals("Test Video", videoEntity.getTitle());
        assertEquals("PENDING", videoEntity.getStatus());
        assertNull(videoEntity.getOriginalFileName());
        assertNull(videoEntity.getStoragePath());
        assertNull(videoEntity.getUsername());
        assertNull(videoEntity.getUserEmail());
        assertNull(videoEntity.getContentType());
        assertNull(videoEntity.getCreatedAt());
    }
    
    @Test
    void settersAndGetters_success() {
        VideoEntity videoEntity = new VideoEntity();
        UUID videoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        videoEntity.setId(videoId);
        videoEntity.setTitle("Updated Video");
        videoEntity.setOriginalFileName("updated.mp4");
        videoEntity.setStoragePath("/updated/path.mp4");
        videoEntity.setStatus("COMPLETED");
        videoEntity.setUsername("updateduser");
        videoEntity.setUserEmail("updated@example.com");
        videoEntity.setContentType("video/mp4");
        videoEntity.setCreatedAt(createdAt);
        
        assertEquals(videoId, videoEntity.getId());
        assertEquals("Updated Video", videoEntity.getTitle());
        assertEquals("updated.mp4", videoEntity.getOriginalFileName());
        assertEquals("/updated/path.mp4", videoEntity.getStoragePath());
        assertEquals("COMPLETED", videoEntity.getStatus());
        assertEquals("updateduser", videoEntity.getUsername());
        assertEquals("updated@example.com", videoEntity.getUserEmail());
        assertEquals("video/mp4", videoEntity.getContentType());
        assertEquals(createdAt, videoEntity.getCreatedAt());
    }
    
    @Test
    void allArgsConstructor_success() {
        UUID videoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        VideoEntity videoEntity = new VideoEntity(
            videoId,
            "Test Video",
            "test.mp4",
            "/storage/test.mp4",
            "PENDING",
            "testuser",
            "test@example.com",
            "video/mp4",
            createdAt
        );
        
        assertNotNull(videoEntity);
        assertEquals(videoId, videoEntity.getId());
        assertEquals("Test Video", videoEntity.getTitle());
        assertEquals("test.mp4", videoEntity.getOriginalFileName());
        assertEquals("/storage/test.mp4", videoEntity.getStoragePath());
        assertEquals("PENDING", videoEntity.getStatus());
        assertEquals("testuser", videoEntity.getUsername());
        assertEquals("test@example.com", videoEntity.getUserEmail());
        assertEquals("video/mp4", videoEntity.getContentType());
        assertEquals(createdAt, videoEntity.getCreatedAt());
    }
    
    @Test
    void noArgsConstructor_success() {
        VideoEntity videoEntity = new VideoEntity();
        
        assertNotNull(videoEntity);
        assertNull(videoEntity.getId());
        assertNull(videoEntity.getTitle());
        assertNull(videoEntity.getOriginalFileName());
        assertNull(videoEntity.getStoragePath());
        assertNull(videoEntity.getStatus());
        assertNull(videoEntity.getUsername());
        assertNull(videoEntity.getUserEmail());
        assertNull(videoEntity.getContentType());
        assertNull(videoEntity.getCreatedAt());
    }
    
    @Test
    void toString_containsExpectedFields() {
        UUID videoId = UUID.randomUUID();
        VideoEntity videoEntity = VideoEntity.builder()
                .id(videoId)
                .title("Test Video")
                .status("PENDING")
                .build();
        
        String videoEntityString = videoEntity.toString();
        
        assertNotNull(videoEntityString);
        assertTrue(videoEntityString.contains("Test Video"));
        assertTrue(videoEntityString.contains("PENDING"));
        assertTrue(videoEntityString.contains(videoId.toString()));
    }
    
    @Test
    void builder_withEmptyStringValues() {
        UUID videoId = UUID.randomUUID();
        
        VideoEntity videoEntity = VideoEntity.builder()
                .id(videoId)
                .title("")
                .originalFileName("")
                .storagePath("")
                .status("")
                .username("")
                .userEmail("")
                .contentType("")
                .build();
        
        assertNotNull(videoEntity);
        assertEquals(videoId, videoEntity.getId());
        assertEquals("", videoEntity.getTitle());
        assertEquals("", videoEntity.getOriginalFileName());
        assertEquals("", videoEntity.getStoragePath());
        assertEquals("", videoEntity.getStatus());
        assertEquals("", videoEntity.getUsername());
        assertEquals("", videoEntity.getUserEmail());
        assertEquals("", videoEntity.getContentType());
    }
}
