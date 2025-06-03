package com.easyliveline.streamingbackend.exceptions;

import org.springframework.security.core.AuthenticationException;

public class SlotNotFoundException extends AuthenticationException {
    public SlotNotFoundException(String message) {
        super(message);
    }
}
