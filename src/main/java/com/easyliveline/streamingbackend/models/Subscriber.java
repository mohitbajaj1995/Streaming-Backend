package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        name = "subscribers",
        indexes = {
                @Index(name = "idx_subscribers_parent_id", columnList = "parent_id"),
                @Index(name = "idx_subscribers_plan_id", columnList = "plan_id"),
                @Index(name = "idx_subscribers_id", columnList = "id"),
        }
)
public class Subscriber extends User {

    private long startAt;
    private long endAt;
    private long lastRecharge;
    private boolean canRefund;
    private int refundableMonths;

    @Enumerated(EnumType.STRING)
    private RoleType parentType; // Values: "MASTER" or "OWNER"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)
    private User parent;  // Can be either a Master or an Owner

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // optional = false means plan is required
    @JoinColumn(name = "plan_id", nullable = false) // creates plan_id column
    private Plan plan;
}
