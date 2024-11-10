package com.alzionlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private String fileId;
    private String filePath;
    private File file;
    private String originalFileName;
    private LocalDateTime uploadTimestamp;
    private Boolean isExpired;
}
