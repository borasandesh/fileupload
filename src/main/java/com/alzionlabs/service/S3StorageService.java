package com.alzionlabs.service;

import com.alzionlabs.model.FileMetadata;
import com.alzionlabs.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
public class S3StorageService implements FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Autowired
    public S3StorageService(FileMetadataRepository fileMetadataRepository,
                            @Value("${aws.accessKeyId}") String accessKeyId,
                            @Value("${aws.secretAccessKey}") String secretAccessKey,
                            @Value("${aws.region}") String region) {
        this.fileMetadataRepository = fileMetadataRepository;
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    @Override
    public String storeFile(MultipartFile file, String passcode) {
        try {
            String fileId = generateFileId(file.getOriginalFilename());
            Path tempFile = Files.createTempFile(fileId, null);
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .build();
            s3Client.putObject(putObjectRequest, tempFile);

            FileMetadata fileMetadata = FileMetadata.builder()
                    .fileId(fileId)
                    .filePath(tempFile.toString())
                    .originalFileName(file.getOriginalFilename())
                    .uploadTimestamp(LocalDateTime.now())
                    .isExpired(false)
                    .build();

            fileMetadataRepository.save(fileMetadata);

            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource retrieveFile(String fileId, String passcode, String expected) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
                Path tempFile = Files.createTempFile(fileId, ".tmp");
                Files.copy(s3Object, tempFile, StandardCopyOption.REPLACE_EXISTING);
                return new UrlResource(tempFile.toUri());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteExpiredFiles() {
        LocalDateTime expirationDate = LocalDateTime.now().minusHours(48);
        fileMetadataRepository.findByUploadTimeBefore(expirationDate).forEach(fileMetadata -> {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileMetadata.getFileId())
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            fileMetadataRepository.delete(String.valueOf(fileMetadata));
        });
    }

    @Override
    public FileMetadata getFileMetadata(String fileId) {
        return fileMetadataRepository.findByDownloadUrl(fileId) ;
    }

    private String generateFileId(String originalFilename) {
        return System.currentTimeMillis() + "_" + originalFilename;
    }
}