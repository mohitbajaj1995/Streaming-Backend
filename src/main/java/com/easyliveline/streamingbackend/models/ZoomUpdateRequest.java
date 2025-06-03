package com.easyliveline.streamingbackend.models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ZoomUpdateRequest {
    private String email;
    private String password;
    private String sdkKey;
    private String sdkSecret;
    private String apiKey;
    private String apiSecret;
    private String accountId;
}