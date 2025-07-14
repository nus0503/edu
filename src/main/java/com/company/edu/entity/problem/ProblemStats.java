package com.company.edu.entity.problem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
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
    private Long problemId;

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


    // 정답률을 Double로 반환하는 헬퍼 메서드
    public Double getCorrectRateAsDouble() {
        return correctRate != null ? correctRate.doubleValue() : 0.0;
    }

    // 정답률을 설정하는 헬퍼 메서드
    public void setCorrectRateFromDouble(Double rate) {
        this.correctRate = rate != null ? BigDecimal.valueOf(rate) : BigDecimal.ZERO;
    }
}
