package com.alzionlabs.repository;

import com.alzionlabs.model.FileMetadata;
import java.time.LocalDateTime;
import java.util.List;

public interface FileMetadataRepository {
    // Find files uploaded before a certain date
    List<FileMetadata> findByUploadTimeBefore(LocalDateTime dateTime);

    // Find a file metadata by download URL
    FileMetadata findByDownloadUrl(String downloadUrl);

    // Method to save metadata to the in-memory store
    void save(FileMetadata fileMetadata);

    // Method to delete metadata from the in-memory store
    void delete(String fileId);
}
