package com.company.edu.entity.test;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
@Entity
@Getter
@Setter
public class OptionSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private ProblemOption option;

    private Integer orderNum;
    private String contentType;
    private String content;
    private String alignment;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private MediaAsset media;
    // Getters and Setters
}