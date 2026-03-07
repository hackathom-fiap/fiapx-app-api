package com.fiapx.video.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    private UUID id;
    private String title;
    private String originalFileName;
    private String storagePath;
    private String status;
    private String username;
    private String userEmail;
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
