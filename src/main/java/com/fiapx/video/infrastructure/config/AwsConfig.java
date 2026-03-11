package com.fiapx.video.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        System.out.println("--- CONFIGURANDO S3 CLIENT NA REGIAO: US_EAST_1");
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
