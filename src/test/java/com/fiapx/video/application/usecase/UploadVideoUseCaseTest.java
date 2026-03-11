package com.fiapx.video.application.usecase;

import Mocks.MultiPartFileTestMock;
import com.fiapx.video.application.dto.VideoUploadRequest;
import com.fiapx.video.application.service.S3UploaderService;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import com.fiapx.video.domain.service.MessageBrokerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Mock
    private S3UploaderService s3Uploader;
    
    @InjectMocks
    private UploadVideoUseCase uploadVideoUseCase;
    
    private VideoUploadRequest request;
    private Video savedVideo;
    private MultipartFile mockFile;
    private final String BUCKET_NAME = "test-bucket";
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadVideoUseCase, "s3BucketName", BUCKET_NAME);
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
    void execute_success() throws IOException {
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals("PENDING", result.getStatus());
        assertTrue(result.getStoragePath().startsWith("s3://" + BUCKET_NAME + "/uploads/"));
        assertTrue(result.getStoragePath().endsWith(".mp4"));
        
        verify(s3Uploader, times(1)).uploadFile(eq(BUCKET_NAME), anyString(), eq(mockFile));
        verify(videoRepository, times(2)).save(any(Video.class));
        verify(messageBroker, times(1)).sendToProcessQueue(any(Video.class));
    }
    
    @Test
    void execute_success_withFileWithoutExtension() throws IOException {
        MultipartFile fileWithoutExtension = MultiPartFileTestMock.createFile("files", "video", "video/mp4");
        request.setFile(fileWithoutExtension);
        
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.getStoragePath().startsWith("s3://" + BUCKET_NAME + "/uploads/"));
        
        verify(s3Uploader, times(1)).uploadFile(eq(BUCKET_NAME), anyString(), eq(fileWithoutExtension));
        verify(videoRepository, times(2)).save(any(Video.class));
    }
    
    @Test
    void execute_success_withMultipleExtensions() throws IOException {
        MultipartFile fileWithMultipleExtensions = MultiPartFileTestMock.createFile("files", "video.test.mp4", "video/mp4");
        request.setFile(fileWithMultipleExtensions);
        
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.getStoragePath().endsWith(".mp4"));
        
        verify(s3Uploader, times(1)).uploadFile(eq(BUCKET_NAME), anyString(), eq(fileWithMultipleExtensions));
        verify(videoRepository, times(2)).save(any(Video.class));
    }

    @Test
    void execute_success_withNullFileName() throws IOException {
        MultipartFile fileWithNullName = MultiPartFileTestMock.createFile("files", null, "video/mp4");
        request.setFile(fileWithNullName);
        
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        
        Video result = uploadVideoUseCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.getStoragePath().startsWith("s3://" + BUCKET_NAME + "/uploads/"));
        // Se fileName for null, a extensão deve ser vazia, então o path não deve terminar com "." ou ".mp4"
        assertFalse(result.getStoragePath().endsWith("."));
        
        verify(s3Uploader, times(1)).uploadFile(eq(BUCKET_NAME), anyString(), eq(fileWithNullName));
        verify(videoRepository, times(2)).save(any(Video.class));
    }
    
    @Test
    void execute_verifyVideoCreation() throws IOException {
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);

        uploadVideoUseCase.execute(request);

        verify(videoRepository, atLeastOnce()).save(argThat(video ->
            video.getTitle().equals(request.getTitle()) &&
            video.getUsername().equals(request.getUsername()) &&
            video.getUserEmail().equals(request.getUserEmail()) &&
            video.getOriginalFileName().equals(mockFile.getOriginalFilename()) &&
            video.getContentType().equals(mockFile.getContentType()) &&
            video.getStatus().equals("PENDING")
        ));
        verify(s3Uploader).uploadFile(eq(BUCKET_NAME), anyString(), eq(mockFile));
    }

    @Test
    void execute_uploadFailure_shouldThrowException() throws IOException {
        when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);
        doThrow(new IOException("S3 Error")).when(s3Uploader).uploadFile(anyString(), anyString(), any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            uploadVideoUseCase.execute(request);
        });

        assertEquals("Erro ao fazer upload do vídeo para o S3", exception.getMessage());
        verify(messageBroker, never()).sendToProcessQueue(any());
    }
}
