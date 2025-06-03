package com.easyliveline.streamingbackend.util;

import java.util.UUID;

public class SessionUtil {

    public static String generateSession(Long userId){
        return userId + ":" + UUID.randomUUID();
    }

    public static String getUserIdFromSession(String session) {
        if (session == null || !session.contains(":")) {
            throw new IllegalArgumentException("Invalid session malformed session string: " + session);
        }
        return session.split(":")[0];
    }
}
