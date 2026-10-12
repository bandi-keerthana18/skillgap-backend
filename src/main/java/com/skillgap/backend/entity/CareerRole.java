package com.skillgap.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "career_roles")
@Getter @Setter @NoArgsConstructor
public class CareerRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(length = 1000)
    private String description;
}
