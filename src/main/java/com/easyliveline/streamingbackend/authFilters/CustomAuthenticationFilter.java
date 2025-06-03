package com.easyliveline.streamingbackend.authFilters;

import com.easyliveline.streamingbackend.models.CustomAuthUser;
import com.easyliveline.streamingbackend.models.LoginRequest;
import com.easyliveline.streamingbackend.services.UserService;
import com.easyliveline.streamingbackend.util.JwtUtil;
import com.easyliveline.streamingbackend.util.SessionUtil;
import com.easyliveline.streamingbackend.util.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginRequest creds = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword());
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new BadCredentialsException("Invalid login request format", e);
        } catch (Exception ex) {
            throw new AuthenticationServiceException("Authentication failed", ex);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
        } catch (IOException ex) {
            logger.error("Failed to write error response", ex);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        try {
            CustomAuthUser userDetails = (CustomAuthUser) authResult.getPrincipal();

            String role = authResult.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(r -> r.startsWith("ROLE_"))
                    .map(r -> r.substring(5))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("User must have a role assigned"));

            String token = JwtUtil.generateToken(userDetails.getUserId(), role, userDetails.getUsername(), userDetails.getTenant());

            String tenantId = TenantContext.getCurrentTenant(); // or however you resolved it earlier
            userService.updateLastSeenAsync(userDetails.getUserId(), System.currentTimeMillis(), tenantId);
            // Set secure cookie with token
//            String cookie = String.format("token=%s; HttpOnly; Secure; SameSite=Strict; Path=/;", token);
//            response.addHeader("Set-Cookie", cookie);
            response.addHeader("Set-Cookie", "token=" + token + "; Path=/;");
            response.addHeader("X-SESSION-ID", SessionUtil.generateSession(userDetails.getUserId()));
//            response.addHeader("Access-Control-Allow-Credentials", "true");

            Map<String, String> responseBody = Map.of(
                    "status", "success",
                    "token", token
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            new ObjectMapper().writeValue(response.getWriter(), responseBody);
        } catch (Exception e) {
//            e.printStackTrace(); // Can be replaced with proper logging
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, String> errorBody = Map.of(
                    "status", "error",
                    "message", "An internal error occurred during authentication"
            );
            new ObjectMapper().writeValue(response.getWriter(), errorBody);
        }
    }
}