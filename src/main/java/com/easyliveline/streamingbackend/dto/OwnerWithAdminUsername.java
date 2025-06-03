package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.models.Owner;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnerWithAdminUsername {

    private Long id;
    private String username;
    private String name;
    private String role;
    private boolean enabled;
    private int points;
    private long createdAt;
    private long updatedAt;
    private long lastSeen;
    private String admin;

    public OwnerWithAdminUsername(Owner owner, String admin) {
        this.id = owner.getId();
        this.username = owner.getUsername();
        this.name = owner.getName();
        this.role = owner.getRole().toString();
        this.enabled = owner.isEnabled();
        this.points = owner.getPoints();
        this.createdAt = owner.getCreatedAt();
        this.updatedAt = owner.getUpdatedAt();
        this.lastSeen = owner.getLastSeen();
        this.admin = admin;
    }
}
