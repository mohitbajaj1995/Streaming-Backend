package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.models.Master;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterWithParentUsername {

    private Long id;
    private String username;
    private String name;
    private String role;
    private boolean enabled;
    private int subscribersCount;
    private int points;
    private long createdAt;
    private long updatedAt;
    private long lastSeen;
    private long lastRecharge;

    public MasterWithParentUsername(Master master, int subscribersCount) {
        this.id = master.getId();
        this.username = master.getUsername();
        this.name = master.getName();
        this.role = master.getRole().toString();
        this.enabled = master.isEnabled();
        this.points = master.getPoints();
        this.createdAt = master.getCreatedAt();
        this.updatedAt = master.getUpdatedAt();
        this.lastSeen = master.getLastSeen();
        this.subscribersCount = subscribersCount;
        this.lastRecharge = master.getLastRecharge();
    }
}
