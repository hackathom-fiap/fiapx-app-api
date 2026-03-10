package com.fiapx.video.application.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoStatusUpdateRequestTest {

    @Test
    void builder_success() {
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .status("COMPLETED")
                .storagePath("/storage/processed/video.mp4")
                .build();
        
        assertNotNull(request);
        assertEquals("COMPLETED", request.getStatus());
        assertEquals("/storage/processed/video.mp4", request.getStoragePath());
    }
    
    @Test
    void builder_withNullValues() {
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .status(null)
                .storagePath(null)
                .build();
        
        assertNotNull(request);
        assertNull(request.getStatus());
        assertNull(request.getStoragePath());
    }
    
    @Test
    void builder_withPartialValues() {
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .status("PROCESSING")
                .build();
        
        assertNotNull(request);
        assertEquals("PROCESSING", request.getStatus());
        assertNull(request.getStoragePath());
    }
    
    @Test
    void builder_withStoragePathOnly() {
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .storagePath("/new/path/video.mp4")
                .build();
        
        assertNotNull(request);
        assertNull(request.getStatus());
        assertEquals("/new/path/video.mp4", request.getStoragePath());
    }
    
    @Test
    void settersAndGetters_success() {
        VideoStatusUpdateRequest request = new VideoStatusUpdateRequest();
        
        request.setStatus("FAILED");
        request.setStoragePath("/error/path/video.mp4");
        
        assertEquals("FAILED", request.getStatus());
        assertEquals("/error/path/video.mp4", request.getStoragePath());
    }
    
    @Test
    void allArgsConstructor_success() {
        VideoStatusUpdateRequest request = new VideoStatusUpdateRequest(
            "COMPLETED",
            "/storage/processed/video.mp4"
        );
        
        assertNotNull(request);
        assertEquals("COMPLETED", request.getStatus());
        assertEquals("/storage/processed/video.mp4", request.getStoragePath());
    }
    
    @Test
    void noArgsConstructor_success() {
        VideoStatusUpdateRequest request = new VideoStatusUpdateRequest();
        
        assertNotNull(request);
        assertNull(request.getStatus());
        assertNull(request.getStoragePath());
    }

    @Test
    void equalsAndHashCode_differentStatus() {
        VideoStatusUpdateRequest request1 = VideoStatusUpdateRequest.builder()
                .status("COMPLETED")
                .storagePath("/storage/processed/video.mp4")
                .build();
                
        VideoStatusUpdateRequest request2 = VideoStatusUpdateRequest.builder()
                .status("PROCESSING")
                .storagePath("/storage/processed/video.mp4")
                .build();
        
        assertNotEquals(request1, request2);
    }
    
    @Test
    void builder_withEmptyStringValues() {
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .status("")
                .storagePath("")
                .build();
        
        assertNotNull(request);
        assertEquals("", request.getStatus());
        assertEquals("", request.getStoragePath());
    }
}
