package com.skillgap.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_history")
@Getter @Setter @NoArgsConstructor
public class AssessmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private CareerRole careerRole;

    private double readiness;

    private double alignment;

    private LocalDateTime takenAt;
}
