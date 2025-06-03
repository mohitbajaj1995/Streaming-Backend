package com.easyliveline.streamingbackend.exceptions;

public class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}