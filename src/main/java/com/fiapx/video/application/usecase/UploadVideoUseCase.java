package com.fiapx.video.application.usecase;

import com.fiapx.video.application.dto.VideoUploadRequest;
import com.fiapx.video.application.dto.VideoUploadRequest;
import com.fiapx.video.application.service.S3UploaderService;
import com.fiapx.video.domain.entity.Video;
import com.fiapx.video.domain.repository.VideoRepository;
import com.fiapx.video.domain.service.MessageBrokerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UploadVideoUseCase {

    private final VideoRepository videoRepository;
    private final MessageBrokerPort messageBroker;
    private final S3UploaderService s3Uploader;

    @Value("${s3.bucket.name}")
    private String s3BucketName;

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

        // 3. Fazer upload do arquivo original para o S3
        String extension = "";
        String fileName = request.getFile().getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        String s3Key = "uploads/" + savedVideo.getId() + extension;

        try {
            s3Uploader.uploadFile(s3BucketName, s3Key, request.getFile());
            // O caminho de armazenamento agora é o caminho S3
            savedVideo.setStoragePath(String.format("s3://%s/%s", s3BucketName, s3Key));
        } catch (IOException e) {
            // Lide com a falha de upload, talvez atualizando o status para ERROR
            throw new RuntimeException("Erro ao fazer upload do vídeo para o S3", e);
        }

        // 4. Enviar para Fila com o caminho do S3
        messageBroker.sendToProcessQueue(savedVideo);

        return videoRepository.save(savedVideo); // Salva o caminho do S3 no banco
    }
}
