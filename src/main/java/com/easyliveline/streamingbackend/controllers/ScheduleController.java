package com.easyliveline.streamingbackend.controllers;


import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.services.RedisService;

import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public ScheduleController(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

//    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLatestSchedule() {
        String json = redisService.getString("schedule:");
        if (json == null) {
            throw new RuntimeException("No schedule found in redis");
        }

        try {
            Map<String, Object> wrapper = objectMapper.readValue(json, new TypeReference<>() {});
            Object data = wrapper.get("data");
//            Object timestamp = wrapper.get("timestamp");

//            Map<String, Object> response = new HashMap<>();
//            response.put("timestamp", timestamp);
//            response.put("data", data);

            return ResponseEntity.ok(ApiResponseBuilder.success("LATEST_SCHEDULE", data));
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing schedule from redis", e);
        }
    }
}

