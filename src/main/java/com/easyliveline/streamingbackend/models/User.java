package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_id", columnList = "id"),
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_created_at_desc", columnList = "created_at DESC"),
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@ToString(exclude = "password")
@NoArgsConstructor
@AllArgsConstructor
public abstract class User extends BaseModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    private long lastSeen;

    private boolean enabled;

    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }
}
