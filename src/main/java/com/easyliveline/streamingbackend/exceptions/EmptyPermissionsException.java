package com.easyliveline.streamingbackend.exceptions;

public class EmptyPermissionsException extends RuntimeException {
    public EmptyPermissionsException(String message) {
        super(message);
    }
}