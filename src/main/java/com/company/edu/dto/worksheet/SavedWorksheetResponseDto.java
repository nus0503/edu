package com.company.edu.dto.worksheet;

import lombok.Data;

import java.util.List;
@Data
public class SavedWorksheetResponseDto {


    private List<ProblemDTO> problems;
    private WorksheetStatistics statistics;
    private WorksheetSettingResponseDto setting;
    private Long authorId;
    private String tester;
    private String tag;
    private String title;
    @Data
    public static class WorksheetStatistics {
        private int totalProblems;
        private int multipleChoice; //객관식 갯수
        private int subjective; //주관식 갯수
        private int shortAnswer; //선다식 갯수
        private double nationalAverageCorrectRate; //통계
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
