package com.fiapx.video.infrastructure.adapter.web;

import com.fiapx.video.application.dto.VideoUploadRequest;
import com.fiapx.video.application.usecase.UploadVideoUseCase;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final UploadVideoUseCase uploadVideoUseCase;
    private final com.fiapx.video.application.usecase.UpdateVideoStatusUseCase updateVideoStatusUseCase;
    private final VideoRepository videoRepository;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public java.util.List<Video> upload(@RequestParam("files") MultipartFile[] files, @RequestParam("title") String title) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String email = null;
        
        if (auth.getDetails() instanceof java.util.Map) {
            email = (String) ((java.util.Map<?, ?>) auth.getDetails()).get("email");
        }
        
        System.out.println("--- CONTROLLER UPLOAD - USER: " + username + " FILES RECEIVED: " + files.length);

        java.util.List<Video> savedVideos = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            Video video = uploadVideoUseCase.execute(VideoUploadRequest.builder()
                    .file(file)
                    .title(files.length > 1 ? title + " - " + file.getOriginalFilename() : title)
                    .username(username)
                    .userEmail(email)
                    .build());
            savedVideos.add(video);
        }
        
        return savedVideos;
    }

    @PostMapping("/{id}/status")
    public Video updateStatus(@PathVariable java.util.UUID id, @RequestBody com.fiapx.video.application.dto.VideoStatusUpdateRequest request) {
        return updateVideoStatusUseCase.execute(id, request);
    }

    @GetMapping("/status")
    public List<Video> getStatusList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return videoRepository.findByUsername(username);
    }
}
