package com.easyliveline.streamingbackend.exceptions;

public class RefundNotFoundException extends RuntimeException {
    public RefundNotFoundException(String message) {
        super(message);
    }
}
