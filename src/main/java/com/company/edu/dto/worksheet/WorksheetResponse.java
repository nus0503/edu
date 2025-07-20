package com.company.edu.dto.worksheet;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorksheetResponse {
    private List<ProblemDTO> problems;
    private WorksheetStatistics statistics;
    private WorksheetSettingResponseDto setting;

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



    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorksheetListResponse {
        private WorksheetData data;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class WorksheetData {
            private List<WorksheetInfo> content;
            private boolean empty;
            private boolean first;
            private boolean last;
            private int number;
            private int numberOfElements;
            private int size;
            private int totalElements;
            private int totalPages;

            @Getter
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class WorksheetInfo {
                private Long worksheetId;
                private Long authorId;
                private String tester;
                private String grade;
                private String tag;
                private String title;
                private String description;
                private Integer problemCount;
                private String contentRange;
                private LocalDateTime createdAt;
            }
        }
    }
    @Data
    public static class AddNewProblemsResponseDto {
        private List<ProblemDTO> problems;
        private PageInfo pageInfo;

        @Data
        public static class PageInfo {
            private int currentPage;
            private int pageSize;
            private long totalElements;
            private int totalPages;
            private boolean hasNext;
            private boolean hasPrevious;
            private boolean isFirst;
            private boolean isLast;
        }
    }
}