package com.easyliveline.streamingbackend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "pVAtRXhMFCyY9NBmGekl3S_eUXYQoMbol6phoDVE31s="; // Use a secure, random key
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final long EXPIRATION_TIME = 86400000;  // 1 Day

//    private static final Date EXPIRATION_TIME = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);

    public static String generateToken(long userId, String role, String username, String tenant) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Add the role to the claims
        claims.put("un", username); // Add the username to the claims
        claims.put("tenant", tenant);

        Header basicHeaders = Jwts.header()
                .add("alg","HS256")
                .type("JWT")
                .build();

        return Jwts.builder()
                .header()
                .add(basicHeaders)
                .and()
                .subject("" + userId)
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .issuedAt(new Date()) // for example, now
                .signWith(SIGNING_KEY)
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) SIGNING_KEY).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

//    public static boolean validateTokenWithKey(SecretKey key,String token) {
//        try {
//            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            System.err.println("Invalid JWT token: " + e.getMessage());
//            return false;
//        }
//    }

    public static String getUserId(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    public static Long getUserIdFromJWT() {
        String token = getTokenFromRequest();
        return Long.parseLong(getUserId(token));
    }


    public static Jws<Claims> getClaims(String token) {
        return Jwts.parser().verifyWith((SecretKey) SIGNING_KEY).build().parseSignedClaims(token);  // get payload
    }

    public static String getClaimWithKey(String token, String claimKey) {
        Claims claims = getClaims(token).getPayload();
        return claims.get(claimKey, String.class);
    }

    public static String getRole(String token) {
        return getClaimWithKey(token,"role");
    }

//    public static List<String> getPermissions(String token) {
//        Claims claims = getClaims(token).getPayload();
//        return claims.get("permissions", List.class); // Retrieve permissions as a List
//    }

//    public static String getRequester() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication.getName();
//    }

    public static String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null; // No request context available
        }

        HttpServletRequest request = attributes.getRequest();

//        System.out.println("==== New Request ====");
//        System.out.println("Method: " + request.getMethod() + " URI: " + request.getRequestURI());
//
//        Enumeration<String> headerNames = request.getHeaderNames();
//        if (headerNames != null) {
//            while (headerNames.hasMoreElements()) {
//                String headerName = headerNames.nextElement();
//                String headerValue = request.getHeader(headerName);
//                System.out.println("Header: " + headerName + " = " + headerValue);
//            }
//        }

        // Check Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Extract token after "Bearer "
        }

        // Fall back to checking cookies
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("token=")) {
                    return cookie.substring("token=".length());
                }
            }
        }

        return null; // Token not found
    }


    public static String getTenantFromJWT() {
        String token = getTokenFromRequest();
        if( token == null) {
            return null; // No token found
        }
        return getClaimWithKey(token, "tenant");
    }

    public static String getRoleFromJWT() {
        String token = getTokenFromRequest();
        return getRole(token);
    }
//
//    public static String getUsernameFromJWT() {
//        String token = getTokenFromRequest();
//        return get(token);
//    }

//    public static String getWebTypeFromJWT() {
//        String token = getTokenFromRequest();
//        return getClaimWithKey(token, "WebType");
//    }
}