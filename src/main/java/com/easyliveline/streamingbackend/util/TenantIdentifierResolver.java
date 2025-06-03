package com.easyliveline.streamingbackend.util;


import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getCurrentTenant();
        System.out.println("Resolving current tenant identifier: " + tenantId);
        return tenantId != null ? tenantId : DEFAULT_TENANT; // Fallback to default tenant if no specific tenant is set
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}