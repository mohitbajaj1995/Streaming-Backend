package com.easyliveline.streamingbackend.models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SuperMasterCreateRequest {
    private String name;
    private String username;
    private String password;
    private int points;
}
