package com.alzionlabs.dto;

import jakarta.validation.constraints.NotBlank;

public class FileDownloadRequest {
    @NotBlank(message = "Passcode is required")
    private String passcode;

    public FileDownloadRequest() {
    }

    public FileDownloadRequest(String passcode) {
        this.passcode = passcode;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }
}
