package com.fiapx.video.infrastructure.adapter.persistence;

import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import com.fiapx.video.infrastructure.adapter.persistence.entity.VideoEntity;
import com.fiapx.video.infrastructure.adapter.persistence.repository.JpaVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VideoPersistenceAdapter implements VideoRepository {

    private final JpaVideoRepository jpaRepository;

    @Override
    public Video save(Video video) {
        VideoEntity entity = VideoEntity.builder()
                .id(video.getId())
                .title(video.getTitle())
                .originalFileName(video.getOriginalFileName())
                .storagePath(video.getStoragePath())
                .status(video.getStatus())
                .username(video.getUsername())
                .userEmail(video.getUserEmail())
                .contentType(video.getContentType())
                .createdAt(video.getCreatedAt())
                .build();
        VideoEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Video> findByUsername(String username) {
        return jpaRepository.findByUsername(username).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Video mapToDomain(VideoEntity entity) {
        return Video.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .originalFileName(entity.getOriginalFileName())
                .storagePath(entity.getStoragePath())
                .status(entity.getStatus())
                .username(entity.getUsername())
                .userEmail(entity.getUserEmail())
                .contentType(entity.getContentType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
