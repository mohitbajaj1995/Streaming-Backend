package com.easyliveline.streamingbackend.exceptions;

import org.springframework.security.core.AuthenticationException;

public class MeetingNotAssignedException extends AuthenticationException {
    public MeetingNotAssignedException(String message) {
        super(message);
    }
}