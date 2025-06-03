package com.easyliveline.streamingbackend.dto;

public record ManagerDTO(Long id, String name, String username, boolean enabled, long lastSeen) {
}
