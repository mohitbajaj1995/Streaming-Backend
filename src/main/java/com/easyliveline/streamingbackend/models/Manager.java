package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "managers",
        indexes = {
                @Index(name = "idx_managers_id", columnList = "id"),
                @Index(name = "idx_managers_parent_id", columnList = "parent_id"),
                @Index(name = "idx_managers_role_type", columnList = "parentType")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Manager extends User {

    // Either "OWNER" or "SUPERMASTER"
    @Enumerated(EnumType.STRING)
    private RoleType parentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)
    private User parent;
}
