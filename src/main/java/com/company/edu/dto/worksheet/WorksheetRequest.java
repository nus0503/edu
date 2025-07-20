package com.company.edu.dto.worksheet;

import lombok.Data;
import java.util.List;

@Data
public class WorksheetRequest {
    private List<Long> minorUnitIds;
    private WorksheetSettings settings;

    @Data
    public static class WorksheetSettings {
        private int problemCount;
        private String difficulties;  // 단일 선택으로 변경
        private List<Integer> levelWeight;  // 난이도별 가중치 배열 [하, 중하, 중, 상, 최상]
        private String problemType; // "전체", "객관식", "주관식"
        private boolean autoGrading;
        private String mockExamIncluded; // "모의고사 포함", "모의고사 제외", "모의고사만"
    }
    @Data
    public static class WorksheetCreateRequest {

        private Long authorId;

        private String tester;

        private String grade;

        private String semester;

        private String tag;

        private String title;

        private int problemCount;

        private String selectedDifficulty;

        private String contentRange;

        private WorksheetSettings settings;

        private List<ProblemOrder> problemOrders;

        @Data
        public static class ProblemOrder {
            private Long problemId;
            private Integer order;
        }
    }

    @Data
    public static class AddNewProblemsRequestDto {
        private List<Long> minorUnitIds;
        private WorksheetSettings settings;
        private List<Long> excludeProblemIds;
        private int problemCount;
        private int page;
        private int size;
    }
}


