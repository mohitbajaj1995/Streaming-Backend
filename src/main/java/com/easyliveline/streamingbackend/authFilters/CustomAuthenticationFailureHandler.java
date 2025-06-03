package com.easyliveline.streamingbackend.authFilters;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        System.out.println("Authentication failed: " + exception.getMessage());
//        String errorMessage = "Invalid username or password.";
        String errorMessage = "Authentication failed: " + exception.getMessage();
//        if (exception instanceof UsernameNotFoundException) {
//            errorMessage = exception.getMessage();
//        } else if (exception instanceof BadCredentialsException) {
//            errorMessage = "Incorrect password provided." + exception.getMessage();
//        } else if (exception instanceof SlotNotFoundException) {
//            errorMessage = "Slot not assigned.";
//        } else if (exception instanceof MeetingNotAssignedException) {
//            errorMessage = "Meeting not assigned to participant slot.";
//        } else {
//            errorMessage = "Authentication failed: " + exception.getMessage();
//        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorBody = Map.of(
                "status", "error",
                "message", errorMessage
        );

        new ObjectMapper().writeValue(response.getWriter(), errorBody);
    }
}
