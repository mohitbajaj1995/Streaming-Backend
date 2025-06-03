package com.easyliveline.streamingbackend.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "admins")  // optional, but good practice
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Admin extends User {

    private int points;

    @JsonManagedReference
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Owner> owners;
}
