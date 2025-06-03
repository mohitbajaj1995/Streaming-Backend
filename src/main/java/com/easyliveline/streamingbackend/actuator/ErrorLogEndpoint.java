package com.easyliveline.streamingbackend.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Endpoint(id = "errorlog")
public class ErrorLogEndpoint {

    @ReadOperation
    public Resource getErrorLog() {
        File file = new File("./logs/error.log");
        if (file.exists() && file.isFile()) {
            return new FileSystemResource(file);
        }
        return null;
    }
}