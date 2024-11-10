package com.alzionlabs.scheduler;

import com.alzionlabs.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredFileCleanupScheduler {
    @Autowired
    private FileStorageService fileStorageService;

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupExpiredFiles() {
        fileStorageService.deleteExpiredFiles();
    }
}
