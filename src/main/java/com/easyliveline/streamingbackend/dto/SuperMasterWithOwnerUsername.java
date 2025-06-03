package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.models.SuperMaster;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperMasterWithOwnerUsername {

    private Long id;
    private String username;
    private String name;
    private String role;
    private boolean enabled;
    private int points;
    private long createdAt;
    private long updatedAt;
    private long lastSeen;
    private int mastersCount;
    private int subscriberCount;

    public SuperMasterWithOwnerUsername(SuperMaster superMaster, int mastersCount, int subscriberCount) {
        this.id = superMaster.getId();
        this.username = superMaster.getUsername();
        this.name = superMaster.getName();
        this.role = superMaster.getRole().toString();
        this.enabled = superMaster.isEnabled();
        this.points = superMaster.getPoints();
        this.createdAt = superMaster.getCreatedAt();
        this.updatedAt = superMaster.getUpdatedAt();
        this.lastSeen = superMaster.getLastSeen();
        this.mastersCount = mastersCount;
        this.subscriberCount = subscriberCount;
    }
}
