package com.fiapx.video.domain.repository;

import com.fiapx.video.domain.entity.Video;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository {
    Video save(Video video);
    Optional<Video> findById(UUID id);
    List<Video> findByUsername(String username);
}
