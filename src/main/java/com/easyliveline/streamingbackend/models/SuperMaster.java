package com.easyliveline.streamingbackend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "supermasters",
        indexes = {
                @Index(name = "idx_supermasters_owner_id", columnList = "owner_id"),
                @Index(name = "idx_supermasters_id", columnList = "id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SuperMaster extends User{

    private int points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Master> masters = new ArrayList<>();

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Subscriber> subscribers = new ArrayList<>();
}
