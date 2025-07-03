package com.company.edu.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorksheetResponse {
    private List<ProblemDTO> problems;
    private WorksheetStatistics statistics;

    @Data
    public static class WorksheetStatistics {
        private int totalProblems;
        private int multipleChoice;
        private int subjective;
        private int shortAnswer;
        private double nationalAverageCorrectRate;
        private DifficultyDistribution difficultyDistribution;
    }

    @Data
    public static class DifficultyDistribution {
        private int low;
        private int mediumLow;
        private int medium;
        private int high;
        private int veryHigh;
    }
}