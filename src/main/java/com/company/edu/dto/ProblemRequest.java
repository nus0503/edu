package com.company.edu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProblemRequest {
    private String type;
    private List<SegmentDto> content;
    private List<OptionDto> options;
    // Getters & Setters
}
