package com.company.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "problem_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemStats {

    @Id
    private Integer problemId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(name = "total_attempts", columnDefinition = "INT DEFAULT 0")
    private Integer totalAttempts = 0;

    @Column(name = "correct_attempts", columnDefinition = "INT DEFAULT 0")
    private Integer correctAttempts = 0;

    @Column(name = "correct_rate", precision = 3, scale = 0)
    private BigDecimal correctRate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
