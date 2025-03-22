package com.company.edu.entity.test;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sub_unit_id")
    private SubUnit subUnit;

    private Integer difficulty;
    private String problemType;

    @OneToMany(mappedBy = "problem")
    private List<ProblemSegment> segments;

    @OneToMany(mappedBy = "problem", fetch = FetchType.EAGER)
    private List<ProblemOption> options;
    // Getters and Setters
}