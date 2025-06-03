package com.easyliveline.streamingbackend.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class LogDirInitializer {

    private static final String LOG_DIR_PATH = "./logs";

    @PostConstruct
    public void createLogDir() {
        File logDir = new File(LOG_DIR_PATH);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                log.info("Log directory created at {}", LOG_DIR_PATH);
            } else {
                log.warn("Failed to create log directory at {}", LOG_DIR_PATH);
            }
        } else {
            log.info("Log directory already exists at {}", LOG_DIR_PATH);
        }
    }
}
