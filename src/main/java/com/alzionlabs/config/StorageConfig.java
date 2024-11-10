package com.alzionlabs.config;

import com.alzionlabs.repository.InMemoryFileMetadataRepository;
import com.alzionlabs.service.EncryptionService;
import com.alzionlabs.service.FileStorageService;
import com.alzionlabs.service.LocalFileStorageService;
import com.alzionlabs.service.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

@Configuration
public class StorageConfig {

    @Value("${storage.type}")
    private String storageType;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private final InMemoryFileMetadataRepository fileMetadataRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public StorageConfig(InMemoryFileMetadataRepository fileMetadataRepository, EncryptionService encryptionService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.encryptionService = encryptionService;
    }

    @Bean
    public FileStorageService fileStorageService() throws IOException {
        if ("local".equalsIgnoreCase(storageType)) {
            return new LocalFileStorageService(fileMetadataRepository, encryptionService);
        } else {
            return new S3StorageService(fileMetadataRepository, System.getenv("AWS_ACCESS_KEY_ID"),
                    System.getenv("AWS_SECRET_ACCESS_KEY"), bucketName);
        }
    }
}
