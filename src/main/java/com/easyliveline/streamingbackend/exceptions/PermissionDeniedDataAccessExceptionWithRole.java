package com.easyliveline.streamingbackend.exceptions;

public class PermissionDeniedDataAccessExceptionWithRole extends RuntimeException {

    // Constructor with two arguments: message and role
    public PermissionDeniedDataAccessExceptionWithRole(String message) {
        super(message);  // Pass the message to the superclass (RuntimeException)
    }
}
