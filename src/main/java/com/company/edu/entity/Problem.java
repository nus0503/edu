package com.company.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_unit_id", nullable = false)
    private MajorUnit majorUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "middle_unit_id", nullable = false)
    private MiddleUnit middleUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minor_unit_id", nullable = false)
    private MinorUnit minorUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detailed_unit_id", nullable = false)
    private DetailedUnit detailedUnit;

    @Column(name = "problem_image_path")
    private String problemImagePath;

    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(columnDefinition = "TEXT")
    private String hint;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false)
    private ProblemType problemType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "problem", cascade = CascadeType.ALL)
    private ProblemStats problemStats;

    @OneToOne(mappedBy = "problem", cascade = CascadeType.ALL)
    private MultipleChoiceAnswer multipleChoiceAnswer;

    @OneToOne(mappedBy = "problem", cascade = CascadeType.ALL)
    private SubjectiveAnswer subjectiveAnswer;

    public enum ProblemType {
        주관식, 객관식, 서술형
    }
}