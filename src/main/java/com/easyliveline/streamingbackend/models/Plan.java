package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "plans")
public class Plan extends BaseModel{

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int durationInMonths;

    @Column(nullable = false)
    private int durationInDays;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanType type; // "FREE" or "PAID"

    @Column(nullable = false)
    private int requiredPoints;

    @Column(nullable = false)
    private boolean isActive;
}
