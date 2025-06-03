package com.easyliveline.streamingbackend.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class PlatformUtils {

    public static String detectPlatform() {
        // Get current request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "Unknown"; // No request context available (e.g., background thread)
        }

        HttpServletRequest request = attributes.getRequest();
        String userAgent = request.getHeader("User-Agent");

        // Ensure userAgent is not null
        if (userAgent == null) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase(); // Convert to lowercase for case-insensitive matching
        System.out.println("User-Agent: " + userAgent); // Debugging Output

        // 1. Detect Android and differentiate WebView
        if (userAgent.contains("android") || userAgent.contains("ktor-client")) {
            if (userAgent.contains("wv") || userAgent.contains("webview")) {
                return "Android WebView";
            }
            return "Android";
        }

        // 2. Detect iOS (iPhone/iPad) and differentiate WebView
        if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            if (userAgent.contains("safari")) {
                return "iOS Safari Browser";
            }
            return "iOS App/WebView";
        }

        // 3. Detect common web browsers first (before OS detection)
        if (userAgent.contains("chrome") || userAgent.contains("firefox") || userAgent.contains("safari") || userAgent.contains("edge") || userAgent.contains("opera")) {
            return "Web Browser";
        }

        // 4. Detect Windows/macOS/Linux only if it's not a web browser
        if (userAgent.contains("windows")) {
            return "Windows OS";
        }
        if (userAgent.contains("macintosh") || userAgent.contains("mac os x")) {
            return "macOS";
        }
        if (userAgent.contains("linux")) {
            return "Linux OS";
        }

        // 5. Default case
        return "Unknown Device";
    }
}
