package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.services.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    public final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/test")
    public String testLogging() {
        // Log messages at different levels using the 'log' instance
        log.trace("This is a TRACE message (not logged due to INFO level)");
        log.debug("This is a DEBUG message (not logged due to INFO level)");
        log.info("This is an INFO message (logged to app.log)");
        log.warn("This is a WARN message (logged to app.log)");
        log.error("This is an ERROR message (logged to both app.log and error.log)");
        return "Logging test completed. Check app.log and error.log.";
    }
}