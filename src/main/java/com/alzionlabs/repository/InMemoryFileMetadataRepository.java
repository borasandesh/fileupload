package com.alzionlabs.repository;

import com.alzionlabs.model.FileMetadata;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryFileMetadataRepository implements FileMetadataRepository {

    private final ConcurrentHashMap<String, FileMetadata> fileStore = new ConcurrentHashMap<>();

    public InMemoryFileMetadataRepository() {
        loadFilesFromDirectory();
    }

    private void loadFilesFromDirectory() {
        Path directory = Paths.get("uploaded_files").toAbsolutePath().normalize();
        try {
            Files.list(directory).forEach(path -> {
                try {
                    String fileId = path.getFileName().toString();
                    LocalDateTime uploadTime = LocalDateTime.now();
                    String originalFileName = path.getFileName().toString();

                    File file = path.toFile();
                    FileMetadata metadata = FileMetadata.builder()
                            .fileId(fileId)
                            .filePath(path.toString())
                            .file(file)
                            .originalFileName(originalFileName)
                            .uploadTimestamp(uploadTime)
                            .isExpired(false)
                            .build();

                    save(metadata);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<FileMetadata> findByUploadTimeBefore(LocalDateTime dateTime) {
        return fileStore.values().stream()
                .filter(fileMetadata -> fileMetadata.getUploadTimestamp().isBefore(dateTime))
                .collect(Collectors.toList());
    }

    @Override
    public FileMetadata findByDownloadUrl(String downloadUrl) {
        return fileStore.get(downloadUrl);
    }

    @Override
    public void save(FileMetadata fileMetadata) {
        fileStore.put(fileMetadata.getFileId(), fileMetadata);
    }

    @Override
    public void delete(String fileId) {
        fileStore.remove(fileId);
    }

    public List<FileMetadata> findAll() {
        return new ArrayList<>(fileStore.values());
    }
}
