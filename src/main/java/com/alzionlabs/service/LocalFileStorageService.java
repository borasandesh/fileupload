package com.alzionlabs.service;

import com.alzionlabs.model.FileMetadata;
import com.alzionlabs.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path fileStorageLocation;
    private final FileMetadataRepository fileMetadataRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public LocalFileStorageService(FileMetadataRepository fileMetadataRepository, EncryptionService encryptionService) throws IOException {
        this.fileMetadataRepository = fileMetadataRepository;
        this.encryptionService = encryptionService;
        this.fileStorageLocation = Paths.get("uploaded_files").toAbsolutePath().normalize();
        Files.createDirectories(fileStorageLocation);
    }

    @Override
    public String storeFile(MultipartFile file, String passcode) {

        String fileId = UUID.randomUUID().toString();
        Path targetLocation = fileStorageLocation.resolve(fileId);

        try {
            byte[] encryptedContent = encryptFileContent(file.getBytes(), passcode);
            Files.write(targetLocation, encryptedContent);

            FileMetadata fileMetadata = FileMetadata.builder()
                    .fileId(fileId)
                    .filePath(targetLocation.toString())
                    .originalFileName(file.getOriginalFilename())
                    .uploadTimestamp(LocalDateTime.now())
                    .isExpired(false)
                    .build();

            fileMetadataRepository.save(fileMetadata);
            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    @Override
    public Resource retrieveFile(String fileId, String passcode, String restorePath) {
        FileMetadata metadata = fileMetadataRepository.findByDownloadUrl(fileId);
        if (metadata == null || metadata.getIsExpired()) {
            throw new RuntimeException("File not found or expired.");
        }

        File encryptedFile = new File(metadata.getFilePath());
        if (encryptedFile.exists()) {
            try {
                byte[] decryptedContent = decryptFileContent(Files.readAllBytes(encryptedFile.toPath()), passcode);

                Path restoredFilePath = Paths.get(restorePath, metadata.getOriginalFileName());
                Files.write(restoredFilePath, decryptedContent);

                return new ByteArrayResource(decryptedContent) {
                    @Override
                    public String getFilename() {
                        return metadata.getOriginalFileName();
                    }
                };
            } catch (IOException | BadPaddingException | IllegalBlockSizeException e) {
                throw new RuntimeException("Could not retrieve file. Please try again!", e);
            }
        } else {
            throw new RuntimeException("File does not exist.");
        }
    }

    @Override
    public void deleteExpiredFiles() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(48);
        List<FileMetadata> expiredFiles = fileMetadataRepository.findByUploadTimeBefore(expirationThreshold);

        for (FileMetadata fileMetadata : expiredFiles) {
            try {
                File file = new File(fileMetadata.getFilePath());
                if (file.exists()) {
                    Files.deleteIfExists(file.toPath());
                }
                fileMetadata.setIsExpired(true);
                fileMetadataRepository.save(fileMetadata);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete expired file: " + fileMetadata.getFileId(), e);
            }
        }
    }
    public FileMetadata getFileMetadata(String fileId) {
        return fileMetadataRepository.findByDownloadUrl(fileId);
    }

    private byte[] encryptFileContent(byte[] content, String passcode) {
        try {
            return encryptionService.encrypt(content, passcode);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    private byte[] decryptFileContent(byte[] content, String passcode) throws BadPaddingException, IllegalBlockSizeException {
        try {
            return encryptionService.decrypt(content, passcode);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
}
