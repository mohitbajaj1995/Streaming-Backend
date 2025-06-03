package com.easyliveline.streamingbackend.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Endpoint(id = "applog")
public class AppLogEndpoint {

    @ReadOperation
    public Resource getAppLog() {
        File file = new File("./logs/app.log");
        if (file.exists() && file.isFile()) {
            return new FileSystemResource(file);
        }
        return null;
    }
}