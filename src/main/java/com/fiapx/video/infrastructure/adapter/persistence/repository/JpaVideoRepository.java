package com.fiapx.video.infrastructure.adapter.persistence.repository;

import com.fiapx.video.infrastructure.adapter.persistence.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaVideoRepository extends JpaRepository<VideoEntity, UUID> {
    List<VideoEntity> findByUsername(String username);
}
