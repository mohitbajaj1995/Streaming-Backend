package com.easyliveline.streamingbackend.models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberCreateRequest {
    private String name;
    private String username;
    private String password;
    private Long plan;
}
