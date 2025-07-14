package com.company.edu.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateWorksheetRequestDto {
    private Long worksheetId;

    private Integer problemCount;

    private List<ProblemOrder> problemOrders;


    @Data
    public static class ProblemOrder {
        private Long problemId;
        private Integer order;
    }
}
