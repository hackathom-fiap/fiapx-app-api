package com.fiapx.video.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadRequest {
    private String title;
    private String username;
    private String userEmail;
    private MultipartFile file;
}
