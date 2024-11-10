package com.alzionlabs.dto;

public class FileUploadResponse {
    private String fileUrl;

    public FileUploadResponse(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
