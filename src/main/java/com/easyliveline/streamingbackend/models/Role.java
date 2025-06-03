package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RoleType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Role {
    private RoleType type;
    private List<String> permissions;
}
