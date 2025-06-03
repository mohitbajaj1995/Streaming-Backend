package com.easyliveline.streamingbackend.models;

import com.easyliveline.streamingbackend.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        name = "refunds",
        indexes = {
                @Index(name = "idx_refunds_user_id", columnList = "userId"),
                @Index(name = "idx_refunds_parent_id", columnList = "parentId"),
                @Index(name = "idx_refunds_status", columnList = "status"),
                @Index(name = "idx_refunds_created_at", columnList = "created_at"),
                @Index(name = "idx_refunds_user_created_at", columnList = "userId, created_at DESC")
        }
)
public class Refund extends BaseModel {

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private long parentId;

    @Column(nullable = false)
    private Long requester;

    @Column(nullable = false)
    private String subscriptionName;

    @Column(nullable = false)
    private long subscriptionStartedAt;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private int refundingMonths;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;
}