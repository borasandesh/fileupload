package com.alzionlabs.service;

import com.alzionlabs.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String passcode);
    Resource retrieveFile(String fileId, String passcode, String expected);
    void deleteExpiredFiles();

    FileMetadata getFileMetadata(String fileId);
}
