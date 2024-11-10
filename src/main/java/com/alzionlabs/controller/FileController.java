package com.alzionlabs.controller;

import com.alzionlabs.dto.FileDownloadRequest;
import com.alzionlabs.dto.FileUploadResponse;
import com.alzionlabs.model.FileMetadata;
import com.alzionlabs.service.EncryptionService;
import com.alzionlabs.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileController {

    private final FileStorageService fileStorageService;
    private final EncryptionService encryptionService;

    @Autowired
    public FileController(FileStorageService fileStorageService, EncryptionService encryptionService) {
        this.fileStorageService = fileStorageService;
        this.encryptionService = encryptionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("passcode") String passcode) {
        // Validate the input parameters
        if (file.isEmpty() || passcode == null || passcode.isEmpty()) {
            return ResponseEntity.badRequest().body(new FileUploadResponse("File or passcode is missing."));
        }

        String fileId = fileStorageService.storeFile(file, passcode);
        return ResponseEntity.ok(new FileUploadResponse(fileId));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileId,
            @RequestParam String passcode,
            @RequestParam(required = false, defaultValue = ".") String restorePath) {

        try {
            Resource fileResource = fileStorageService.retrieveFile(fileId, passcode.split(",")[0], restorePath);

            FileMetadata fileMetadata = fileStorageService.getFileMetadata(fileId);
            String originalFileName = fileMetadata.getOriginalFileName();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
