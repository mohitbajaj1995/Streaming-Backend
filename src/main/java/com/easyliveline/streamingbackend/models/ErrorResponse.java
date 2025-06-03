package com.easyliveline.streamingbackend.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String errorDetails;
    private LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String errorMessage, String errorDetails) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
        this.timestamp = LocalDateTime.now();
    }
}
