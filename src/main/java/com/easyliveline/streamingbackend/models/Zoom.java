package com.easyliveline.streamingbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "zooms", // Explicitly defining the table name
        indexes = {
                @Index(name = "idx_zooms_created_at", columnList = "created_at")
//                @Index(name = "idx_${TABLE_NAME}_created_at", columnList = "created_at")
        }
)
public class Zoom extends BaseModel {

    private String email;
    private String password;
    private String sdkKey;
    private String sdkSecret;
    private String apiKey;
    private String apiSecret;
    private String accountId;

    @OneToMany(mappedBy = "zoom") // inverse
    List<Meeting> meetings;
}
