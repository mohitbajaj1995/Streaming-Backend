package com.easyliveline.streamingbackend.dto;

public record MasterDeleteSecurityCheck(
        Long parentId,
        int hostsCount
) {
}
