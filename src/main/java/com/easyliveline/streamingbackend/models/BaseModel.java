package com.easyliveline.streamingbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Integer createdAt;

    @Column(name = "updated_at")
    private Integer updatedAt;

    @PrePersist
    public void prePersist() {
        int today = getTodayAsInt();
        this.createdAt = today;
        this.updatedAt = today;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = getTodayAsInt();
    }

    private Integer getTodayAsInt() {
        return Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
}
