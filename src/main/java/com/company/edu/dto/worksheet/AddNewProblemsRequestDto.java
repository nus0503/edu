package com.company.edu.dto.worksheet;

import lombok.Data;

import java.util.List;

@Data
public class AddNewProblemsRequestDto {

    private List<String> selectedPaths;
}
