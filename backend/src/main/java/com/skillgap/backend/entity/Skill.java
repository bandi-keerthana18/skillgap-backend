package com.skillgap.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String category;

    @ManyToMany
    @JoinTable(
        name = "skill_prerequisite",
        joinColumns = @JoinColumn(name = "skill_id"),
        inverseJoinColumns = @JoinColumn(name = "requires_skill_id")
    )
    private Set<Skill> prerequisites = new HashSet<>();
}
