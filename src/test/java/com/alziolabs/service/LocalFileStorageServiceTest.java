package com.alzionlabs.service;

import com.alzionlabs.model.FileMetadata;
import com.alzionlabs.repository.FileMetadataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalFileStorageServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private LocalFileStorageService fileStorageService;

    private final String passcode = "securePass";
    private final String fileId = "sample-file-id";
    private final String originalFileName = "sample-file.txt";
    private final Path filePath = Paths.get("uploaded_files", fileId);

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        Files.createDirectories(Paths.get("uploaded_files"));
        fileStorageService = new LocalFileStorageService(fileMetadataRepository, encryptionService);
    }

    @Test
    void testStoreFile_Success() throws Exception {
        byte[] fileContent = "sample content".getBytes();
        byte[] encryptedContent = "encrypted content".getBytes();
        when(file.getBytes()).thenReturn(fileContent);
        when(file.getOriginalFilename()).thenReturn(originalFileName);
        when(encryptionService.encrypt(fileContent, passcode)).thenReturn(encryptedContent);

        String returnedFileId = fileStorageService.storeFile(file, passcode);

        assertNotNull(returnedFileId);
        verify(fileMetadataRepository, times(1)).save(any(FileMetadata.class));
    }

    @Test
    void testStoreFile_IOFailure() throws Exception {
        when(file.getBytes()).thenThrow(IOException.class);

        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file, passcode));
        verify(fileMetadataRepository, never()).save(any(FileMetadata.class));
    }

    @Test
    void testRetrieveFile_Success() throws Exception {
        byte[] encryptedContent = "encrypted content".getBytes();
        byte[] decryptedContent = "sample content".getBytes();
        when(fileMetadataRepository.findByDownloadUrl(fileId)).thenReturn(
                FileMetadata.builder()
                        .fileId(fileId)
                        .filePath(filePath.toString())
                        .originalFileName(originalFileName)
                        .isExpired(false)
                        .build()
        );
        when(encryptionService.decrypt(encryptedContent, passcode)).thenReturn(decryptedContent);
        Files.write(filePath, encryptedContent);

    }

    @Test
    void testRetrieveFile_FileExpired() {
        when(fileMetadataRepository.findByDownloadUrl(fileId)).thenReturn(
                FileMetadata.builder().fileId(fileId).isExpired(true).build()
        );

        assertThrows(RuntimeException.class, () -> fileStorageService.retrieveFile(fileId, passcode, "restored_files"));
    }

    @Test
    void testRetrieveFile_FileNotFound() {
        when(fileMetadataRepository.findByDownloadUrl(fileId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> fileStorageService.retrieveFile(fileId, passcode, "restored_files"));
    }

    @Test
    void testRetrieveFile_DecryptionError() throws Exception {
        byte[] encryptedContent = "encrypted content".getBytes();
        when(fileMetadataRepository.findByDownloadUrl(fileId)).thenReturn(
                FileMetadata.builder()
                        .fileId(fileId)
                        .filePath(filePath.toString())
                        .isExpired(false)
                        .build()
        );
        when(encryptionService.decrypt(encryptedContent, passcode)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> fileStorageService.retrieveFile(fileId, passcode, "restored_files"));
    }

    @Test
    void testGetFileMetadata_Success() {
        FileMetadata metadata = FileMetadata.builder()
                .fileId(fileId)
                .originalFileName(originalFileName)
                .uploadTimestamp(LocalDateTime.now())
                .isExpired(false)
                .build();
        when(fileMetadataRepository.findByDownloadUrl(fileId)).thenReturn(metadata);

        FileMetadata returnedMetadata = fileStorageService.getFileMetadata(fileId);

        assertNotNull(returnedMetadata);
        assertEquals(fileId, returnedMetadata.getFileId());
    }

    @Test
    void testGetFileMetadata_NotFound() {
        when(fileMetadataRepository.findByDownloadUrl(fileId)).thenReturn(null);

        assertNull(fileStorageService.getFileMetadata(fileId));
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(filePath);
    }
}
