package com.company.edu.dto;

import lombok.Data;

import java.util.List;

@Data
public class SavedWorksheetProblemResponseDto {

    private List<ProblemDTO> problems;
}
