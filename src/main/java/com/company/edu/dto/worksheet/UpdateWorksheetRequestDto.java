package com.company.edu.dto.worksheet;

import lombok.Data;

import java.util.List;

@Data
public class UpdateWorksheetRequestDto {


    private Integer problemCount;

    private List<ProblemOrder> problemOrders;

    private String title;

    private String tester;

    private String tag;


    @Data
    public static class ProblemOrder {
        private Long problemId;
        private Integer order;
    }
}
