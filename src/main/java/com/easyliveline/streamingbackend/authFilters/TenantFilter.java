package com.easyliveline.streamingbackend.authFilters;

import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.models.ErrorResponse;
import com.easyliveline.streamingbackend.util.JwtUtil;
import com.easyliveline.streamingbackend.util.TenantContext;
import com.easyliveline.streamingbackend.util.TenantUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = resolveTenantId(request);

        if (isBlank(tenantId)) {
            log.error("No tenant found in X-Tenant-ID header, subdomain, JWT or actuator fallback");
            // Set the response status and content type
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Build your error response
            ErrorResponse errorResponse = new ErrorResponse(
                    "TENANT_NOT_FOUND",
                    "Invalid or missing tenant identifier",
                    "No matching tenant found in header or subdomain"
            );

            ApiResponse<ErrorResponse> apiResponse = new ApiResponse<>(
                    false,
                    "TENANT_NOT_FOUND",
                    errorResponse
            );

            // Convert to JSON and write to response
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // 👈 fix here
            String json = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(json);
            return;
        }

        try {
            TenantContext.setCurrentTenant(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader("X-Tenant-ID");
        System.out.println("isLogin Request: " + isLoginRequest(request));

        if (isBlank(tenantId)) {
            if (isActuatorRequest(request)) {
                return "public";
            }

            tenantId = !isLoginRequest(request)
                    ? getTenantFromJWT()
                    : TenantUtil.resolveTenantFromRequest();
        }

        return tenantId;
    }

    private String getTenantFromJWT() {
        String tenantId = JwtUtil.getTenantFromJWT();
        if (isBlank(tenantId)) {
            log.error("No tenant found in JWT");
        }
        return tenantId;
    }

    private boolean isActuatorRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/actuator");
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/auth/login");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
