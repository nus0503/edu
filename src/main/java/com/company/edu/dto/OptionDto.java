package com.company.edu.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OptionDto {
    private List<SegmentDto> content;
    private Integer optionNumber;
    private boolean isCorrect;
    // Getters & Setters
}