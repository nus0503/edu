package com.company.edu.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorksheetRequest {
    private List<String> selectedPaths;
    private WorksheetSettings settings;

    @Data
    public static class WorksheetSettings {
        private int problemCount;
        private List<String> difficulties;
        private String problemType; // "전체", "객관식", "주관식"
        private boolean autoGrading;
        private String mockExamIncluded; // "모의고사 포함", "모의고사 제외", "모의고사만"
    }
}
