package com.company.edu.dto.worksheet;

import lombok.Data;

import java.util.List;

@Data
public class SavedWorksheetProblemResponseDto {

    private List<ProblemDTO> problems;
}
