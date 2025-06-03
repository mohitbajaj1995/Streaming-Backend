package com.easyliveline.streamingbackend.projections;

import com.easyliveline.streamingbackend.enums.RoleType;

public interface UserLoginView {
    Long getId();                // from BaseModel
    String getUsername();        // from User
    String getPassword();        // from User
    RoleType getRole();          // from User
    boolean isEnabled();         // from User
}
