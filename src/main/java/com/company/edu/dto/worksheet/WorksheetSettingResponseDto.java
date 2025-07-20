package com.company.edu.dto.worksheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorksheetSettingResponseDto {

    private int problemCount;
    private String difficulties;  // 단일 선택으로 변경
    private String problemType; // "전체", "객관식", "주관식"
    private boolean autoGrading;
    private String mockExamIncluded; // "모의고사 포함", "모의고사 제외", "모의고사만"
}
