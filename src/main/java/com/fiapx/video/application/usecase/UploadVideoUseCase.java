package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.VideoUploadRequest;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import com.fiapx.video.domain.service.MessageBrokerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UploadVideoUseCase {

    private final VideoRepository videoRepository;
    private final MessageBrokerPort messageBroker;

    public Video execute(VideoUploadRequest request) {
        // 1. Criar entidade
        Video video = Video.builder()
                .title(request.getTitle())
                .originalFileName(request.getFile().getOriginalFilename())
                .username(request.getUsername())
                .userEmail(request.getUserEmail())
                .contentType(request.getFile().getContentType())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // 2. Salvar no Banco
        Video savedVideo = videoRepository.save(video);

        // 3. Simular salvamento de arquivo (Em produção seria S3/MinIO)
        String extension = "";
        String fileName = request.getFile().getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        savedVideo.setStoragePath("/tmp/videos/" + savedVideo.getId() + extension);

        // 4. Enviar para Fila
        messageBroker.sendToProcessQueue(savedVideo);

        return savedVideo;
    }
}
