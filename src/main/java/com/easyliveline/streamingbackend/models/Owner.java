package com.easyliveline.streamingbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "owners",
        indexes = {
                @Index(name = "idx_owners_id", columnList = "id"),
                @Index(name = "idx_owners_admin_id", columnList = "admin_id"),
                @Index(name = "idx_owners_points", columnList = "points")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Owner extends User {

    private int points;

//    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @JsonIgnore
    private Admin admin;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<SuperMaster> superMasters = new ArrayList<>();

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Master> masters = new ArrayList<>();

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Subscriber> subscribers = new ArrayList<>();
}
