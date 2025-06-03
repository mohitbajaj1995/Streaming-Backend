package com.easyliveline.streamingbackend.util;

import com.easyliveline.streamingbackend.models.ApplicationConfiguration;
import com.easyliveline.streamingbackend.models.WebsiteConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Component
public class TenantUtil {

    private static final String TENANT_ID_HEADER = "X-TENANT-ID"; // Header name
    private static ApplicationConfiguration applicationConfiguration;

    @Autowired
    public void setWebsiteProperties(ApplicationConfiguration applicationConfiguration) {
        TenantUtil.applicationConfiguration = applicationConfiguration;
    }

    public static String getTenantFromHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            String tenantID = request.getHeader(TENANT_ID_HEADER);
            if (tenantID == null || tenantID.trim().isEmpty()) {
                throw new RuntimeException("X-TENANT-ID header is missing or empty.");
            }
            return tenantID;
        } else {
            throw new RuntimeException("No current HTTP request found.");
        }
    }

    public static String getTenantFromSubdomain() {
        String[] domainParts = getDomainPartsFromRequest();

        if (domainParts.length < 2) {
            throw new RuntimeException("Tenant is missing or invalid.");
        }

        // Extract tenant (subdomain) logic
        String tenant = null;
        if (domainParts[domainParts.length - 1].equalsIgnoreCase("localhost")) {
            tenant = domainParts[0];  // Localhost case (e.g., "dummyfake.localhost")
        } else if (domainParts.length >= 3) {
            tenant = domainParts[0];  // Production case (e.g., "tenant.somedomain.com")
        }

        if (tenant == null || tenant.isEmpty()) {
            String hostname = TenantUtil.extractDomain();
            Map<String, WebsiteConfig> website = applicationConfiguration.getWebsite();
            if (website == null || website.isEmpty()) {
                throw new RuntimeException("Website configuration is missing.");
            }

            if(website.containsKey(tenant)) return tenant;

            String matchedKey = website.entrySet().stream()
                    .filter(entry -> entry.getValue().getWebsite().equalsIgnoreCase(hostname))
                    .map(Map.Entry::getKey)
//                    .map(Object::toString)
                    .findFirst()
                    .orElse(null);

//            if (applicationConfiguration.getTenants().containsKey(hostname)) {
//                return applicationConfiguration.getTenants().get(hostname);
//            }
        }

        if (tenant == null || tenant.isEmpty()) {
            throw new RuntimeException("Tenant is missing or invalid.");
        }

        return tenant;
    }

    // 🔹 **Extracted method to return domainParts array**
    private static String[] getDomainPartsFromRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("No current HTTP request found.");
        }

        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String fullHost = request.getHeader("Host"); // e.g., "dummyfake.localhost:8080" or "dummyfake.somedomain.com"

        if (fullHost == null || fullHost.isEmpty()) {
            throw new RuntimeException("Host header is missing.");
        }

        String fullDomain = fullHost.split(":")[0]; // Remove port if present
        return fullDomain.split("\\."); // Split by '.'
    }


    public static String extractDomain() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("No current HTTP request found.");
        }

        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String fullUrl = request.getRequestURL().toString();
        return getDomain(fullUrl);
    }

    private static String getDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost(); // Get the domain
            if (host == null) return null;

            // Remove "www." if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // Extract only the primary domain and TLD
            String[] parts = host.split("\\.");
            int length = parts.length;
            if (length < 2) {
                return host; // Return as-is if it doesn't have a TLD
            }
            return parts[length - 2] + "." + parts[length - 1]; // Get the last two parts (e.g., telecom.ac)
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL format: " + url, e);
        }
    }

//    public static String resolveTenantFromRequest() {
//        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
//        if (!(attributes instanceof ServletRequestAttributes)) {
//            throw new RuntimeException("No current HTTP request found.");
//        }
//
//        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
//        String fullHost = request.getHeader("Host"); // e.g., royal.streaming.com, www.royal.com
//        if (fullHost == null || fullHost.isEmpty()) {
//            throw new RuntimeException("Host header is missing.");
//        }
//
//        // Strip port if exists
//        String host = fullHost.split(":")[0];
//
//        // Get subdomain candidate (e.g., "royal" from royal.streaming.com or www.royal.com)
//        String[] parts = host.split("\\.");
//        String firstPart = parts[0];
//
//        Map<String, WebsiteConfig> websiteMap = applicationConfiguration.getWebsite();
//        if (websiteMap == null || websiteMap.isEmpty()) {
//            throw new RuntimeException("Website configuration is missing.");
//        }
//
//        // ✅ Step 1: Try subdomain match
//        if (websiteMap.containsKey(firstPart)) {
//            return firstPart;
//        }
//
//        // ✅ Step 2: Try full host match against config values (e.g., royaltv.ac or www.royal.com)
//        String normalizedHost = host.startsWith("www.") ? host.substring(4) : host;
//        for (Map.Entry<String, WebsiteConfig> entry : websiteMap.entrySet()) {
//            if (entry.getValue().getWebsite().equalsIgnoreCase(normalizedHost)) {
//                return entry.getKey(); // return the config key (e.g., "royal")
//            }
//        }
//
//        throw new RuntimeException("No matching tenant found for host: " + host);
//    }

    public static String resolveTenantFromRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("No current HTTP request found.");
        }

        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String fullHost = request.getHeader("Host"); // e.g., royal.streaming.com or royal.localhost:8008
        if (fullHost == null || fullHost.isEmpty()) {
            throw new RuntimeException("Host header is missing.");
        }

        // Strip port if exists
        String host = fullHost.split(":")[0]; // e.g., royal.localhost

        // Normalize host: remove "www."
        String normalizedHost = host.startsWith("www.") ? host.substring(4) : host;

        // Split host by `.`
        String[] parts = normalizedHost.split("\\.");

        String subdomain;
        if (parts.length >= 3) {
            // e.g., royal.streaming.com → ["royal", "streaming", "com"]
            subdomain = parts[0];
        } else if (parts.length == 2 && "localhost".equals(parts[1])) {
            // e.g., royal.localhost
            subdomain = parts[0];
        } else {
            throw new RuntimeException("Cannot extract tenant from host: " + host);
        }

        System.out.println("Extracted subdomain: " + subdomain);
        Map<String, WebsiteConfig> websiteMap = applicationConfiguration.getWebsite();
        if (websiteMap == null || websiteMap.isEmpty()) {
            throw new RuntimeException("Website configuration is missing.");
        }

        // ✅ Try direct subdomain match
        if (websiteMap.containsKey(subdomain)) {
            return subdomain;
        }

        // ✅ Fallback: match normalized host to configured full domains (e.g., royaltv.ac)
        for (Map.Entry<String, WebsiteConfig> entry : websiteMap.entrySet()) {
            if (entry.getValue().getWebsite().equalsIgnoreCase(normalizedHost)) {
                return entry.getKey(); // e.g., "royal"
            }
        }
        return null; // No matching tenant found
//        throw new TenantNotFoundException("No matching tenant found for host: " + host);
    }
}