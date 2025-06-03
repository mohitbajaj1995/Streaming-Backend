package com.easyliveline.streamingbackend.dto;

public record ZoomWithMeetingSize(
        Long id,
        Integer createdAt,
        Integer updatedAt,
        String email,
        String password,
        String sdkKey,
        String sdkSecret,
        String apiKey,
        String apiSecret,
        String accountId,
        int meetingSize
) {
}
