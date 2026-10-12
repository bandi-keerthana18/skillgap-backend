package com.skillgap.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor
public class RoleSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private CareerRole careerRole;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    private int requiredLevel;

    private double weight;
}
