package com.alzionlabs.repository;

import com.alzionlabs.model.FileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileMetadataRepositoryTest {

    private FileMetadataRepository fileMetadataRepository;

    @BeforeEach
    void setUp() {

        fileMetadataRepository = new InMemoryFileMetadataRepository();
    }

    @Test
    void testSaveAndFindByDownloadUrl() {
        FileMetadata metadata = new FileMetadata(
                "test-file-id",
                "path/to/file",
                null,
                "originalFileName.txt",
                LocalDateTime.now(),
                false
        );

        fileMetadataRepository.save(metadata);
        FileMetadata retrievedMetadata = fileMetadataRepository.findByDownloadUrl("test-file-id");

        assertNotNull(retrievedMetadata);
        assertEquals("test-file-id", retrievedMetadata.getFileId());
        assertEquals("path/to/file", retrievedMetadata.getFilePath());
    }
}
