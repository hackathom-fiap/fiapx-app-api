package com.fiapx.video.application.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatusUpdateRequest {
    private String status;
    private String storagePath;
}
