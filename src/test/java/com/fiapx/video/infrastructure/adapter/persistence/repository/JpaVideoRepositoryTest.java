package com.fiapx.video.infrastructure.adapter.persistence.repository;

import com.fiapx.video.infrastructure.adapter.persistence.entity.VideoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaVideoRepositoryTest {

    @Mock
    private JpaVideoRepository jpaVideoRepository;
    
    private VideoEntity videoEntity;
    private UUID videoId;
    private String username;
    
    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        username = "testuser";
        
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
        when(jpaVideoRepository.save(any(VideoEntity.class))).thenReturn(videoEntity);
        
        VideoEntity result = jpaVideoRepository.save(videoEntity);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("Test Video", result.getTitle());
        assertEquals("test.mp4", result.getOriginalFileName());
        assertEquals("/storage/test.mp4", result.getStoragePath());
        assertEquals("PENDING", result.getStatus());
        assertEquals(username, result.getUsername());
        assertEquals("test@example.com", result.getUserEmail());
        assertEquals("video/mp4", result.getContentType());
        
        verify(jpaVideoRepository, times(1)).save(videoEntity);
    }

    @Test
    void findById_success() {
        when(jpaVideoRepository.findById(videoId)).thenReturn(Optional.of(videoEntity));
        
        Optional<VideoEntity> result = jpaVideoRepository.findById(videoId);
        
        assertTrue(result.isPresent());
        VideoEntity foundEntity = result.get();
        assertEquals(videoId, foundEntity.getId());
        assertEquals("Test Video", foundEntity.getTitle());
        assertEquals(username, foundEntity.getUsername());
        
        verify(jpaVideoRepository, times(1)).findById(videoId);
    }

    @Test
    void findById_notFound() {
        when(jpaVideoRepository.findById(videoId)).thenReturn(Optional.empty());
        
        Optional<VideoEntity> result = jpaVideoRepository.findById(videoId);
        
        assertFalse(result.isPresent());
        
        verify(jpaVideoRepository, times(1)).findById(videoId);
    }

    @Test
    void findByUsername_success() {
        List<VideoEntity> entities = List.of(videoEntity);
        when(jpaVideoRepository.findByUsername(username)).thenReturn(entities);
        
        List<VideoEntity> result = jpaVideoRepository.findByUsername(username);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        VideoEntity foundEntity = result.get(0);
        assertEquals(videoId, foundEntity.getId());
        assertEquals("Test Video", foundEntity.getTitle());
        assertEquals(username, foundEntity.getUsername());
        
        verify(jpaVideoRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_emptyList() {
        when(jpaVideoRepository.findByUsername(username)).thenReturn(List.of());
        
        List<VideoEntity> result = jpaVideoRepository.findByUsername(username);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(jpaVideoRepository, times(1)).findByUsername(username);
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
        when(jpaVideoRepository.findByUsername(username)).thenReturn(entities);
        
        List<VideoEntity> result = jpaVideoRepository.findByUsername(username);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(videoEntity.getId(), result.get(0).getId());
        assertEquals(videoEntity2.getId(), result.get(1).getId());
        
        verify(jpaVideoRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_differentUsernames() {
        String username2 = "anotheruser";
        VideoEntity videoEntity2 = VideoEntity.builder()
                .id(UUID.randomUUID())
                .title("Another User Video")
                .originalFileName("another.mp4")
                .storagePath("/storage/another.mp4")
                .status("PROCESSING")
                .username(username2)
                .userEmail("another@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(jpaVideoRepository.findByUsername(username)).thenReturn(List.of(videoEntity));
        when(jpaVideoRepository.findByUsername(username2)).thenReturn(List.of(videoEntity2));
        
        List<VideoEntity> result1 = jpaVideoRepository.findByUsername(username);
        List<VideoEntity> result2 = jpaVideoRepository.findByUsername(username2);
        
        assertEquals(1, result1.size());
        assertEquals(username, result1.get(0).getUsername());
        
        assertEquals(1, result2.size());
        assertEquals(username2, result2.get(0).getUsername());
        
        verify(jpaVideoRepository, times(1)).findByUsername(username);
        verify(jpaVideoRepository, times(1)).findByUsername(username2);
    }

    @Test
    void findAll_success() {
        VideoEntity videoEntity2 = VideoEntity.builder()
                .id(UUID.randomUUID())
                .title("Test Video 2")
                .originalFileName("test2.mp4")
                .storagePath("/storage/test2.mp4")
                .status("COMPLETED")
                .username("anotheruser")
                .userEmail("another@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
                
        List<VideoEntity> entities = List.of(videoEntity, videoEntity2);
        when(jpaVideoRepository.findAll()).thenReturn(entities);
        
        List<VideoEntity> result = jpaVideoRepository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(jpaVideoRepository, times(1)).findAll();
    }

    @Test
    void deleteById_success() {
        doNothing().when(jpaVideoRepository).deleteById(videoId);
        
        jpaVideoRepository.deleteById(videoId);
        
        verify(jpaVideoRepository, times(1)).deleteById(videoId);
    }

    @Test
    void existsById_success() {
        when(jpaVideoRepository.existsById(videoId)).thenReturn(true);
        
        boolean result = jpaVideoRepository.existsById(videoId);
        
        assertTrue(result);
        verify(jpaVideoRepository, times(1)).existsById(videoId);
    }

    @Test
    void existsById_notFound() {
        when(jpaVideoRepository.existsById(videoId)).thenReturn(false);
        
        boolean result = jpaVideoRepository.existsById(videoId);
        
        assertFalse(result);
        verify(jpaVideoRepository, times(1)).existsById(videoId);
    }

    @Test
    void count_success() {
        when(jpaVideoRepository.count()).thenReturn(5L);
        
        long result = jpaVideoRepository.count();
        
        assertEquals(5L, result);
        verify(jpaVideoRepository, times(1)).count();
    }

    @Test
    void repositoryExtendsJpaRepository() {
        assertTrue(jpaVideoRepository instanceof JpaRepository);
    }

    @Test
    void save_withNullEntity() {
        when(jpaVideoRepository.save(null)).thenThrow(new IllegalArgumentException("Entity cannot be null"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            jpaVideoRepository.save(null);
        });
        
        verify(jpaVideoRepository, times(1)).save(null);
    }

    @Test
    void findByUsername_withNullUsername() {
        when(jpaVideoRepository.findByUsername(null)).thenReturn(List.of());
        
        List<VideoEntity> result = jpaVideoRepository.findByUsername(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(jpaVideoRepository, times(1)).findByUsername(null);
    }

    @Test
    void findByUsername_withEmptyUsername() {
        when(jpaVideoRepository.findByUsername("")).thenReturn(List.of());
        
        List<VideoEntity> result = jpaVideoRepository.findByUsername("");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(jpaVideoRepository, times(1)).findByUsername("");
    }
}
