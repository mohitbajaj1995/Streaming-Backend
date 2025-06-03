package com.easyliveline.streamingbackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "meetings",
        indexes = {
                @Index(name = "idx_meetings_created_at", columnList = "created_at"),
        }
)
public class Meeting extends BaseModel{

    private String name;

    private String email;

    private boolean activated;

    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String meetingNumber;

    @JsonIgnore
    @Column(nullable = false)
    private String meetingPassword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zoom_id", nullable = false) // foreign key
    private Zoom zoom;
}