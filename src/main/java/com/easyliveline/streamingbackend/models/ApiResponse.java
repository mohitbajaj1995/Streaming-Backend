package com.easyliveline.streamingbackend.models;

public class ApiResponse<T> {

    private boolean success;   // Indicates if the request was successful
    private String message;    // Describes the result or error
    private T data;            // Contains response data for successful requests (nullable)

    // Default constructor
    public ApiResponse() {}

    // Constructor for success response without errorData
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
