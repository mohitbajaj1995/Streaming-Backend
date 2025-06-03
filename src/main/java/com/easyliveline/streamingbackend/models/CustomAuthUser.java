package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RoleType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomAuthUser extends User {
    @Getter
    private final RoleType role;
    @Getter
    private final long userId;

    @Getter
    private final String tenant;

    public CustomAuthUser(String username, String password, boolean enabled, boolean accountNonExpired,
                          boolean credentialsNonExpired, boolean accountNonLocked,
                          Collection<? extends GrantedAuthority> authorities, RoleType role, long userId, String tenant) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.role = role;
        this.userId = userId;
        this.tenant = tenant;
    }
}