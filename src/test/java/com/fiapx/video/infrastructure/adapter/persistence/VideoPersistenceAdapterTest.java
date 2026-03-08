package com.fiapx.video.infrastructure.adapter.persistence;

import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.infrastructure.adapter.persistence.entity.VideoEntity;
import com.fiapx.video.infrastructure.adapter.persistence.repository.JpaVideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoPersistenceAdapterTest {

    @Mock
    private JpaVideoRepository jpaRepository;
    
    @InjectMocks
    private VideoPersistenceAdapter videoPersistenceAdapter;
    
    private Video video;
    private VideoEntity videoEntity;
    private UUID videoId;
    private String username;
    
    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        username = "testuser";
        
        video = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath("/storage/test.mp4")
                .status("PENDING")
                .username(username)
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
                
        videoEntity = VideoEntity.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath("/storage/test.mp4")
                .status("PENDING")
                .username(username)
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void save_success() {
        when(jpaRepository.save(any(VideoEntity.class))).thenReturn(videoEntity);
        
        Video result = videoPersistenceAdapter.save(video);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals(video.getTitle(), result.getTitle());
        assertEquals(video.getOriginalFileName(), result.getOriginalFileName());
        assertEquals(video.getStoragePath(), result.getStoragePath());
        assertEquals(video.getStatus(), result.getStatus());
        assertEquals(video.getUsername(), result.getUsername());
        assertEquals(video.getUserEmail(), result.getUserEmail());
        assertEquals(video.getContentType(), result.getContentType());
        assertEquals(video.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS), result.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        
        verify(jpaRepository, times(1)).save(argThat(entity -> 
            entity.getId().equals(videoId) &&
            entity.getTitle().equals(video.getTitle()) &&
            entity.getOriginalFileName().equals(video.getOriginalFileName()) &&
            entity.getStoragePath().equals(video.getStoragePath()) &&
            entity.getStatus().equals(video.getStatus()) &&
            entity.getUsername().equals(video.getUsername()) &&
            entity.getUserEmail().equals(video.getUserEmail()) &&
            entity.getContentType().equals(video.getContentType()) &&
            entity.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).equals(video.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.SECONDS))
        ));
    }
    
    @Test
    void save_success_withNullFields() {
        Video videoWithNulls = Video.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath(null)
                .status("PENDING")
                .username(username)
                .userEmail(null)
                .contentType(null)
                .createdAt(LocalDateTime.now())
                .build();
                
        VideoEntity entityWithNulls = VideoEntity.builder()
                .id(videoId)
                .title("Test Video")
                .originalFileName("test.mp4")
                .storagePath(null)
                .status("PENDING")
                .username(username)
                .userEmail(null)
                .contentType(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(jpaRepository.save(any(VideoEntity.class))).thenReturn(entityWithNulls);
        
        Video result = videoPersistenceAdapter.save(videoWithNulls);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertNull(result.getStoragePath());
        assertNull(result.getUserEmail());
        assertNull(result.getContentType());
        
        verify(jpaRepository, times(1)).save(any(VideoEntity.class));
    }
    
    @Test
    void findById_success() {
        when(jpaRepository.findById(videoId)).thenReturn(Optional.of(videoEntity));
        
        Optional<Video> result = videoPersistenceAdapter.findById(videoId);
        
        assertTrue(result.isPresent());
        Video foundVideo = result.get();
        assertEquals(videoId, foundVideo.getId());
        assertEquals(videoEntity.getTitle(), foundVideo.getTitle());
        assertEquals(videoEntity.getOriginalFileName(), foundVideo.getOriginalFileName());
        assertEquals(videoEntity.getStoragePath(), foundVideo.getStoragePath());
        assertEquals(videoEntity.getStatus(), foundVideo.getStatus());
        assertEquals(videoEntity.getUsername(), foundVideo.getUsername());
        assertEquals(videoEntity.getUserEmail(), foundVideo.getUserEmail());
        assertEquals(videoEntity.getContentType(), foundVideo.getContentType());
        assertEquals(videoEntity.getCreatedAt(), foundVideo.getCreatedAt());
        
        verify(jpaRepository, times(1)).findById(videoId);
    }
    
    @Test
    void findById_notFound() {
        when(jpaRepository.findById(videoId)).thenReturn(Optional.empty());
        
        Optional<Video> result = videoPersistenceAdapter.findById(videoId);
        
        assertFalse(result.isPresent());
        
        verify(jpaRepository, times(1)).findById(videoId);
    }
    
    @Test
    void findByUsername_success() {
        List<VideoEntity> entities = List.of(videoEntity);
        when(jpaRepository.findByUsername(username)).thenReturn(entities);
        
        List<Video> result = videoPersistenceAdapter.findByUsername(username);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        Video foundVideo = result.get(0);
        assertEquals(videoId, foundVideo.getId());
        assertEquals(videoEntity.getTitle(), foundVideo.getTitle());
        assertEquals(videoEntity.getUsername(), foundVideo.getUsername());
        
        verify(jpaRepository, times(1)).findByUsername(username);
    }
    
    @Test
    void findByUsername_emptyList() {
        when(jpaRepository.findByUsername(username)).thenReturn(List.of());
        
        List<Video> result = videoPersistenceAdapter.findByUsername(username);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(jpaRepository, times(1)).findByUsername(username);
    }
    
    @Test
    void findByUsername_multipleVideos() {
        VideoEntity videoEntity2 = VideoEntity.builder()
                .id(UUID.randomUUID())
                .title("Test Video 2")
                .originalFileName("test2.mp4")
                .storagePath("/storage/test2.mp4")
                .status("COMPLETED")
                .username(username)
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
                
        List<VideoEntity> entities = List.of(videoEntity, videoEntity2);
        when(jpaRepository.findByUsername(username)).thenReturn(entities);
        
        List<Video> result = videoPersistenceAdapter.findByUsername(username);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(videoEntity.getId(), result.get(0).getId());
        assertEquals(videoEntity2.getId(), result.get(1).getId());
        
        verify(jpaRepository, times(1)).findByUsername(username);
    }
}
