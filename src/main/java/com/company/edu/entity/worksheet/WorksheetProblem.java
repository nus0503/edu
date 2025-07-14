package com.company.edu.entity.worksheet;

import com.company.edu.entity.problem.Problem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// WorksheetProblem Entity (연결 테이블)
@Entity
@Table(name = "worksheet_problem")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorksheetProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worksheet_problem_id")
    private Long worksheetProblemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worksheet_id", nullable = false)
    private Worksheet worksheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "problem_order", nullable = false)
    private Integer problemOrder;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Problem generPro(WorksheetProblem worksheetProblem) {
        return null;
    }
}
