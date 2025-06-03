package com.easyliveline.streamingbackend.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_user_id", columnList = "user_id"),
                @Index(name = "idx_transactions_created_at", columnList = "created_at"),

                // ✅ NEW: Optimize for queries that filter by user + order by created_at DESC
                @Index(name = "idx_transactions_user_created_at", columnList = "user_id, created_at DESC"),

                // ✅ NEW: Optimize for queries that filter by is_credit + created_at
                @Index(name = "idx_transactions_is_credit_created_at", columnList = "is_credit, created_at DESC"),

                // ✅ NEW: Optimize for frequent queries filtering by user_id + is_credit
                @Index(name = "idx_transactions_user_credit", columnList = "user_id, is_credit")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private long points;

    private String description;

    private boolean isCredit;

    private Long createdAt;

    private long now;

    private long after;
}
