package com.easyliveline.streamingbackend.util;

import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.models.ErrorResponse;

public class ApiResponseBuilder {

    // Success response builder method
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);  // For success, no errorData
    }

    // Failure response builder method
    public static ApiResponse<ErrorResponse> failure(String message, ErrorResponse errorData) {
        return new ApiResponse<>(false, message, errorData);  // For failure, no data
    }

    public static String extractShortMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) return "Unknown error";

        // Strip known prefixes
        if (message.contains(":")) {
            message = message.substring(message.indexOf(":") + 1).trim();
        }

        // Optionally strip fully qualified class names
        message = message.replaceAll("com\\.easyliveline\\.assistant\\.DTO\\.", "");

        // Optional: limit message length
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }

}
