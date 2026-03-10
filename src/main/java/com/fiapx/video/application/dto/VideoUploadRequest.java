package com.fiapx.video.application.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadRequest {
    private String title;
    private String username;
    private String userEmail;
    private MultipartFile file;
}
