package com.easyliveline.streamingbackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

public class SignatureUtil {

    public String encodeWithSecret(Map<String, Object> data, String secretKey) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);

            // HMAC-SHA256
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] hash = sha256_HMAC.doFinal(json.getBytes());

            // Combine payload and signature
            String encodedPayload = Base64.getEncoder().encodeToString(json.getBytes());
            String encodedSignature = Base64.getEncoder().encodeToString(hash);

            return encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Error while encoding with secret: " + e.getMessage());
        }
    }
}
