package com.company.edu.entity.test;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_level_id")
    private SchoolLevel schoolLevel;

    private String name;
    // Getters and Setters
}