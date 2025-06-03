package com.easyliveline.streamingbackend.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestUtil {

    public static HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request context available.");
        }
        return attributes.getRequest();
    }

//    public static Map<String, String> getHeadersFromRequest(List<String> requiredHeaderNames) {
//        HttpServletRequest request = getCurrentHttpRequest();
//        Map<String, String> headers = new HashMap<>();
//
//        // Collect all headers from the request
//        Enumeration<String> headerNamesEnum = request.getHeaderNames();
//        while (headerNamesEnum.hasMoreElements()) {
//            String headerName = headerNamesEnum.nextElement();
//            String headerValue = request.getHeader(headerName);
//            System.out.println(headerName + " = " + headerValue);
//            headers.put(headerName, headerValue);
//        }
//

    /// /         Validate required headers
//        for (String requiredHeader : requiredHeaderNames) {
//            if (!headers.containsKey(requiredHeader)) {
//                throw new IllegalArgumentException("Missing required header: " + requiredHeader);
//            }
//        }
//
//        return headers;
//    }
    public static Map<String, String> getHeadersFromRequest(List<String> requiredHeaderNames) {
        HttpServletRequest request = getCurrentHttpRequest();
        Map<String, String> allHeaders = new HashMap<>();

        // Log and collect all headers
        Enumeration<String> headerNamesEnum = request.getHeaderNames();
        while (headerNamesEnum != null && headerNamesEnum.hasMoreElements()) {
            String headerName = headerNamesEnum.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + " = " + headerValue); // Replace with logger in production
            allHeaders.put(headerName, headerValue);
        }

        // Prepare map of required headers with values (may be null)
        Map<String, String> requestedHeaders = new HashMap<>();
        for (String requiredHeader : requiredHeaderNames) {
            requestedHeaders.put(requiredHeader, allHeaders.getOrDefault(requiredHeader, null));
        }

        return requestedHeaders;
    }
}
