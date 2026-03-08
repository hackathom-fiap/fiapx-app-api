package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.VideoStatusUpdateRequest;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateVideoStatusUseCaseTest {

    @Mock
    private VideoRepository videoRepository;
    
    @InjectMocks
    private UpdateVideoStatusUseCase updateVideoStatusUseCase;
    
    private UUID videoId;
    private Video existingVideo;
    private VideoStatusUpdateRequest request;
    
    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        
        existingVideo = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .status("PENDING")
                .storagePath("/tmp/videos/test.mp4")
                .username("testuser")
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
                
        request = VideoStatusUpdateRequest.builder()
                .status("COMPLETED")
                .storagePath("/storage/videos/processed/test.mp4")
                .build();
    }
    
    @Test
    void execute_success() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(existingVideo));
        when(videoRepository.save(any(Video.class))).thenReturn(existingVideo);
        
        Video result = updateVideoStatusUseCase.execute(videoId, request);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("/storage/videos/processed/test.mp4", result.getStoragePath());
        
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(existingVideo);
    }
    
    @Test
    void execute_success_statusOnly() {
        VideoStatusUpdateRequest statusOnlyRequest = VideoStatusUpdateRequest.builder()
                .status("PROCESSING")
                .build();
        
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(existingVideo));
        when(videoRepository.save(any(Video.class))).thenReturn(existingVideo);
        
        Video result = updateVideoStatusUseCase.execute(videoId, statusOnlyRequest);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("PROCESSING", result.getStatus());
        assertEquals("/tmp/videos/test.mp4", result.getStoragePath()); // unchanged
        
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(existingVideo);
    }
    
    @Test
    void execute_success_nullStoragePath() {
        VideoStatusUpdateRequest nullStorageRequest = VideoStatusUpdateRequest.builder()
                .status("FAILED")
                .storagePath(null)
                .build();
        
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(existingVideo));
        when(videoRepository.save(any(Video.class))).thenReturn(existingVideo);
        
        Video result = updateVideoStatusUseCase.execute(videoId, nullStorageRequest);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("FAILED", result.getStatus());
        assertEquals("/tmp/videos/test.mp4", result.getStoragePath()); // unchanged
        
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(existingVideo);
    }
    
    @Test
    void execute_videoNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        
        when(videoRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateVideoStatusUseCase.execute(nonExistentId, request);
        });
        
        assertEquals("Vídeo não encontrado", exception.getMessage());
        
        verify(videoRepository, times(1)).findById(nonExistentId);
        verify(videoRepository, never()).save(any(Video.class));
    }
    
    @Test
    void execute_verifySaveCalledWithUpdatedVideo() {
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(existingVideo));
        when(videoRepository.save(any(Video.class))).thenReturn(existingVideo);
        
        updateVideoStatusUseCase.execute(videoId, request);
        
        verify(videoRepository).save(argThat(video -> 
            video.getId().equals(videoId) &&
            video.getStatus().equals("COMPLETED") &&
            video.getStoragePath().equals("/storage/videos/processed/test.mp4")
        ));
    }
    
    @Test
    void execute_emptyStoragePath() {
        VideoStatusUpdateRequest emptyStorageRequest = VideoStatusUpdateRequest.builder()
                .status("COMPLETED")
                .storagePath("")
                .build();
        
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(existingVideo));
        when(videoRepository.save(any(Video.class))).thenReturn(existingVideo);
        
        Video result = updateVideoStatusUseCase.execute(videoId, emptyStorageRequest);
        
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("", result.getStoragePath());
        
        verify(videoRepository, times(1)).findById(videoId);
        verify(videoRepository, times(1)).save(existingVideo);
    }
}
