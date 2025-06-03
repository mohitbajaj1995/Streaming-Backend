package com.easyliveline.streamingbackend.actuator;


import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;

@Component
@Endpoint(id = "logfile")
public class LogFileEndpoint {

    private static final String LOG_DIR = "./logs";
    private static final Set<String> allowedLogs = Set.of("app", "error");

    @ReadOperation
    public Resource getLogFile(@Selector String name) {
        if (!allowedLogs.contains(name)) {
            return null; // or throw IllegalArgumentException
        }

        String fileName = switch (name) {
            case "app" -> "app.log";
            case "error" -> "error.log";
            default -> null;
        };

        if (fileName == null) return null;

        File file = new File(LOG_DIR, fileName);
        if (file.exists() && file.isFile()) {
            return new FileSystemResource(file);
        }

        return null;
    }
}