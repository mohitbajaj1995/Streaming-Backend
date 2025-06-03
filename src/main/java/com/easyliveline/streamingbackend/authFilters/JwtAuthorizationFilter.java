package com.easyliveline.streamingbackend.authFilters;

import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.services.RoleService;
import com.easyliveline.streamingbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);
    private final RoleService roleService;

    public JwtAuthorizationFilter(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws IOException, ServletException {

        try {
            // Check if the request is for a WebSocket upgrade
            String upgradeHeader = request.getHeader("Upgrade");
            if ("websocket".equalsIgnoreCase(upgradeHeader)) {
                logger.debug("Skipping JWT filter for WebSocket connection");
                chain.doFilter(request, response);
                return;
            }

            System.out.println("Calling JWT Util:");
            String token = JwtUtil.getTokenFromRequest();

            if (token == null) {
                logger.error("No token found in Authorization header or cookies");
                chain.doFilter(request, response);
                return;
            }

            // Validate the token
            if (!JwtUtil.validateToken(token)) {
                logger.error("Invalid or expired JWT token");
                sendErrorResponse(response, "Invalid or Expired Token");
                return;
            }

            // Authenticate user from token
            authenticateUserFromToken(token);

        } catch (Exception ex) {
            logger.error("JWT validation error: {}", ex.getMessage(), ex);
            sendErrorResponse(response, "JWT validation failed: " + ex.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }

    private void authenticateUserFromToken(String token) {
        // Extract user details from the token
        String userId = JwtUtil.getUserId(token);
        String role = JwtUtil.getRole(token);

        // Create authorities list
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // Add permissions
        Optional.ofNullable(roleService.getPermissionsByRole(RoleType.valueOf(role)))
                .ifPresent(role_permissions -> role_permissions.stream()
                        .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
//                        .peek(authority -> System.out.println("Adding authority: " + authority.getAuthority()))
                        .forEach(authorities::add));

// Also print roles
//        authorities.stream()
//                .map(GrantedAuthority::getAuthority)
//                .forEach(auth -> System.out.println("Final authority: " + auth));

//        Optional.ofNullable(roleService.getPermissionsByRole(RoleType.valueOf(role)))
//                .ifPresent(role_permissions -> role_permissions.stream()
//                        .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
//                        .forEach(authorities::add));

//        Optional.ofNullable(roleService.getPermissionsByRoleName(JwtUtil.getWebTypeFromJWT(), role))
//                .ifPresent(role_permissions -> {
//                    role_permissions.stream()
//                            .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
//                            .forEach(authorities::add);
////                    role_permissions.forEach(permission -> System.out.println("Permission: " + permission));
//                });


//        try {
//            Optional.ofNullable(roleService.getPermissionsByRoleName(JwtUtil.getWebTypeFromJWT(), role))
//                    .orElseThrow(() -> new UserNotFoundException("No permissions found for role: " + role))
//                    .forEach(permission -> {
//                        authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission));
//                        System.out.println("Permission: " + permission);
//                    });
//        } catch (ResourceNotFoundException | IllegalArgumentException e) {
////            logger.error("Error fetching permissions for role: {}", role, e);
//            throw new UserNotFoundException("Error fetching permissions for role: " + role);
//        }


        logger.debug("Authenticated user: {} with authorities: {}", userId, authorities);

        // Set authentication in SecurityContext
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\":\"Unauthorized\", \"message\":\"%s\"}", message));
    }
}
