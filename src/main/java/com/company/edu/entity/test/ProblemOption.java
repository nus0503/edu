package com.company.edu.entity.test;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class ProblemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private Integer optionNumber;
    private Boolean isCorrect;

    @OneToMany(mappedBy = "option", fetch = FetchType.EAGER)
    private List<OptionSegment> segments;
    // Getters and Setters
}