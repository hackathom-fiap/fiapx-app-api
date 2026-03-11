package com.fiapx.video.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3UploaderServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private S3UploaderService s3UploaderService;

    @Test
    void shouldUploadFileSuccessfully() throws IOException {
        String bucketName = "test-bucket";
        String key = "test-key";
        byte[] content = "test content".getBytes();
        
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        s3UploaderService.uploadFile(bucketName, key, file);

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
