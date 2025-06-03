package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.models.Zoom;

public record MeetingWithZoomDto(Long meetingId ,String meetingNumber, String meetingPassword, String email, Zoom zoom) {
}
