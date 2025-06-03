package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "masters",
        indexes = {
                @Index(name = "idx_masters_id", columnList = "id"),
                @Index(name = "idx_masters_parent_id", columnList = "parent_id"),
                @Index(name = "idx_masters_points", columnList = "points"),
                @Index(name = "idx_masters_last_recharge", columnList = "lastRecharge")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Master extends User {

    private int points;

    private long lastRecharge;

    @Enumerated(EnumType.STRING)
    private RoleType parentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)
    private User parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Subscriber> subscribers = new ArrayList<>();

}