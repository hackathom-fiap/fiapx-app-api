package com.fiapx.video.application.usecase;

import Mocks.MultiPartFileTestMock;
import com.fiapx.video.application.dto.VideoUploadRequest;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import com.fiapx.video.domain.service.MessageBrokerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadVideoUseCaseTest {

    @Mock
    private VideoRepository videoRepository;
    
    @Mock
    private MessageBrokerPort messageBroker;
    
    @InjectMocks
    private UploadVideoUseCase uploadVideoUseCase;
    
    private VideoUploadRequest request;
    private Video savedVideo;
    private MultipartFile mockFile;
    
    @BeforeEach
    void setUp() {
        mockFile = MultiPartFileTestMock.createFile("files", "test-video.mp4", "video/mp4");
        
        request = VideoUploadRequest.builder()
                .title("Test Video")
                .username("testuser")
                .userEmail("test@example.com")
                .file(mockFile)
                .build();
                
        savedVideo = Video.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .originalFileName(request.getFile().getOriginalFilename())
                .username(request.getUsername())
                .userEmail(request.getUserEmail())
                .contentType(request.getFile().getContentType())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void execute_success() {
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getUserEmail(), result.getUserEmail());
        assertEquals("PENDING", result.getStatus());
        assertEquals(mockFile.getOriginalFilename(), result.getOriginalFileName());
        assertEquals(mockFile.getContentType(), result.getContentType());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getStoragePath().contains("/tmp/videos/"));
        assertTrue(result.getStoragePath().endsWith(".mp4"));
        
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(messageBroker, times(1)).sendToProcessQueue(any(Video.class));
    }
    
    @Test
    void execute_success_withFileWithoutExtension() {
        MultipartFile fileWithoutExtension = MultiPartFileTestMock.createFile("files", "video", "video/mp4");
        request.setFile(fileWithoutExtension);
        
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.getStoragePath().contains("/tmp/videos/"));
        assertFalse(result.getStoragePath().endsWith("."));
        
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(messageBroker, times(1)).sendToProcessQueue(any(Video.class));
    }
    
    @Test
    void execute_success_withMultipleExtensions() {
        MultipartFile fileWithMultipleExtensions = MultiPartFileTestMock.createFile("files", "video.test.mp4", "video/mp4");
        request.setFile(fileWithMultipleExtensions);
        
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.getStoragePath().endsWith(".mp4"));
        
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(messageBroker, times(1)).sendToProcessQueue(any(Video.class));
    }
    
    @Test
    void execute_verifyVideoCreation() {
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);

        uploadVideoUseCase.execute(request);

        verify(videoRepository).save(argThat(video ->
            video.getTitle().equals(request.getTitle()) &&
            video.getUsername().equals(request.getUsername()) &&
            video.getUserEmail().equals(request.getUserEmail()) &&
            video.getOriginalFileName().equals(mockFile.getOriginalFilename()) &&
            video.getContentType().equals(mockFile.getContentType()) &&
            video.getStatus().equals("PENDING") &&
            video.getCreatedAt() != null
        ));
    }
}
