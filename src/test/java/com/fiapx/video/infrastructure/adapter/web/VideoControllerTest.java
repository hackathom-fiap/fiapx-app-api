package com.fiapx.video.infrastructure.adapter.web;

import Mocks.MultiPartFileTestMock;
import com.fiapx.video.application.dto.VideoStatusUpdateRequest;
import com.fiapx.video.application.usecase.UpdateVideoStatusUseCase;
import com.fiapx.video.application.usecase.UploadVideoUseCase;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private UploadVideoUseCase uploadVideoUseCase;
    
    @Mock
    private UpdateVideoStatusUseCase updateVideoStatusUseCase;
    
    @Mock
    private VideoRepository videoRepository;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    @InjectMocks
    private VideoController videoController;
    
    private Video mockVideo;
    private MultipartFile[] mockFiles;
    
    @BeforeEach
    void setUp() {
        mockVideo = Video.builder()
                .id(UUID.randomUUID())
                .title("Test Video")
                .originalFileName("test.mp4")
                .status("PENDING")
                .username("testuser")
                .userEmail("test@example.com")
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
                
        mockFiles = new MultipartFile[]{
            MultiPartFileTestMock.createFile("files", "video1.mp4", "video/mp4"),
            MultiPartFileTestMock.createFile("files", "video2.mp4", "video/mp4")
        };
    }

    @Test
    void upload_success_singleFile() {
        String username = "testuser";
        String email = "test@example.com";
        String title = "Test Video";
        MultipartFile[] singleFile = new MultipartFile[]{mockFiles[0]};
        
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(authentication.getDetails()).thenReturn(Map.of("email", email));
            
            when(uploadVideoUseCase.execute(any())).thenReturn(mockVideo);
            
            List<Video> result = videoController.upload(singleFile, title);
            
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(mockVideo, result.get(0));
            verify(uploadVideoUseCase, times(1)).execute(any());
        }
    }
    
    @Test
    void upload_success_multipleFiles() {
        String username = "testuser";
        String email = "test@example.com";
        String title = "Test Video";
        
        Video mockVideo2 = Video.builder()
                .id(UUID.randomUUID())
                .title("Test Video - video2.mp4")
                .originalFileName("video2.mp4")
                .status("PENDING")
                .username(username)
                .userEmail(email)
                .contentType("video/mp4")
                .createdAt(LocalDateTime.now())
                .build();
        
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(authentication.getDetails()).thenReturn(Map.of("email", email));
            
            when(uploadVideoUseCase.execute(any())).thenReturn(mockVideo, mockVideo2);
            
            List<Video> result = videoController.upload(mockFiles, title);
            
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(mockVideo, result.get(0));
            assertEquals(mockVideo2, result.get(1));
            verify(uploadVideoUseCase, times(2)).execute(any());
        }
    }
    
    @Test
    void upload_success_withoutEmailDetails() {
        String username = "testuser";
        String title = "Test Video";
        MultipartFile[] singleFile = new MultipartFile[]{mockFiles[0]};
        
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(authentication.getDetails()).thenReturn(null);
            
            when(uploadVideoUseCase.execute(any())).thenReturn(mockVideo);
            
            List<Video> result = videoController.upload(singleFile, title);
            
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(uploadVideoUseCase, times(1)).execute(any());
        }
    }

    @Test
    void upload_success_withNonMapDetails() {
        String username = "testuser";
        String title = "Test Video";
        MultipartFile[] singleFile = new MultipartFile[]{mockFiles[0]};
        
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(authentication.getDetails()).thenReturn("Some String Details"); // Non-map details
            
            when(uploadVideoUseCase.execute(any())).thenReturn(mockVideo);
            
            List<Video> result = videoController.upload(singleFile, title);
            
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(uploadVideoUseCase, times(1)).execute(any());
        }
    }

    @Test
    void updateStatus_success() {
        UUID videoId = UUID.randomUUID();
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .status("COMPLETED")
                .storagePath("/storage/path/video.mp4")
                .build();
        
        Video updatedVideo = Video.builder()
                .id(videoId)
                .title("Test Video")
                .status("COMPLETED")
                .storagePath("/storage/path/video.mp4")
                .build();
        
        when(updateVideoStatusUseCase.execute(videoId, request)).thenReturn(updatedVideo);
        
        Video result = videoController.updateStatus(videoId, request);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("/storage/path/video.mp4", result.getStoragePath());
        verify(updateVideoStatusUseCase, times(1)).execute(videoId, request);
    }
    
    @Test
    void updateStatus_success_statusOnly() {
        UUID videoId = UUID.randomUUID();
        VideoStatusUpdateRequest request = VideoStatusUpdateRequest.builder()
                .status("PROCESSING")
                .build();
        
        Video updatedVideo = Video.builder()
                .id(videoId)
                .title("Test Video")
                .status("PROCESSING")
                .storagePath("/original/path/video.mp4")
                .build();
        
        when(updateVideoStatusUseCase.execute(videoId, request)).thenReturn(updatedVideo);
        
        Video result = videoController.updateStatus(videoId, request);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("PROCESSING", result.getStatus());
        assertEquals("/original/path/video.mp4", result.getStoragePath());
        verify(updateVideoStatusUseCase, times(1)).execute(videoId, request);
    }

    @Test
    void getStatusList_success() {
        String username = "testuser";
        List<Video> expectedVideos = Arrays.asList(mockVideo);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            
            when(videoRepository.findByUsername(username)).thenReturn(expectedVideos);
            
            List<Video> result = videoController.getStatusList();
            
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(expectedVideos, result);
            verify(videoRepository, times(1)).findByUsername(username);
        }
    }
    
    @Test
    void getStatusList_success_emptyList() {
        String username = "testuser";
        List<Video> emptyList = Arrays.asList();
        
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            
            when(videoRepository.findByUsername(username)).thenReturn(emptyList);
            
            List<Video> result = videoController.getStatusList();
            
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(videoRepository, times(1)).findByUsername(username);
        }
    }
}
