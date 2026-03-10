package com.fiapx.video.application.dto;

import Mocks.MultiPartFileTestMock;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class VideoUploadRequestTest {

    @Test
    void builder_success() {
        MultipartFile mockFile = MultiPartFileTestMock.createFile("files", "test.mp4", "video/mp4");
        
        VideoUploadRequest request = VideoUploadRequest.builder()
                .title("Test Video")
                .username("testuser")
                .userEmail("test@example.com")
                .file(mockFile)
                .build();
        
        assertNotNull(request);
        assertEquals("Test Video", request.getTitle());
        assertEquals("testuser", request.getUsername());
        assertEquals("test@example.com", request.getUserEmail());
        assertEquals(mockFile, request.getFile());
    }
    
    @Test
    void builder_withNullValues() {
        VideoUploadRequest request = VideoUploadRequest.builder()
                .title(null)
                .username(null)
                .userEmail(null)
                .file(null)
                .build();
        
        assertNotNull(request);
        assertNull(request.getTitle());
        assertNull(request.getUsername());
        assertNull(request.getUserEmail());
        assertNull(request.getFile());
    }
    
    @Test
    void builder_withPartialValues() {
        VideoUploadRequest request = VideoUploadRequest.builder()
                .title("Test Video")
                .username("testuser")
                .build();
        
        assertNotNull(request);
        assertEquals("Test Video", request.getTitle());
        assertEquals("testuser", request.getUsername());
        assertNull(request.getUserEmail());
        assertNull(request.getFile());
    }
    
    @Test
    void settersAndGetters_success() {
        MultipartFile mockFile = MultiPartFileTestMock.createFile("files", "test.mp4", "video/mp4");
        VideoUploadRequest request = new VideoUploadRequest();
        
        request.setTitle("Updated Video");
        request.setUsername("updateduser");
        request.setUserEmail("updated@example.com");
        request.setFile(mockFile);
        
        assertEquals("Updated Video", request.getTitle());
        assertEquals("updateduser", request.getUsername());
        assertEquals("updated@example.com", request.getUserEmail());
        assertEquals(mockFile, request.getFile());
    }
    
    @Test
    void allArgsConstructor_success() {
        MultipartFile mockFile = MultiPartFileTestMock.createFile("files", "test.mp4", "video/mp4");
        
        VideoUploadRequest request = new VideoUploadRequest(
            "Test Video",
            "testuser",
            "test@example.com",
            mockFile
        );
        
        assertNotNull(request);
        assertEquals("Test Video", request.getTitle());
        assertEquals("testuser", request.getUsername());
        assertEquals("test@example.com", request.getUserEmail());
        assertEquals(mockFile, request.getFile());
    }
    
    @Test
    void noArgsConstructor_success() {
        VideoUploadRequest request = new VideoUploadRequest();
        
        assertNotNull(request);
        assertNull(request.getTitle());
        assertNull(request.getUsername());
        assertNull(request.getUserEmail());
        assertNull(request.getFile());
    }
}
