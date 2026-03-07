package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.VideoStatusUpdateRequest;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateVideoStatusUseCase {

    private final VideoRepository videoRepository;

    public Video execute(UUID id, VideoStatusUpdateRequest request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vídeo não encontrado"));

        video.setStatus(request.getStatus());
        if (request.getStoragePath() != null) {
            video.setStoragePath(request.getStoragePath());
        }

        return videoRepository.save(video);
    }
}
