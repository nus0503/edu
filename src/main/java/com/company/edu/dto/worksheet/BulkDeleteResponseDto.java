package com.company.edu.dto.worksheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkDeleteResponseDto {
    private List<Long> successIds;
    private List<Long> failedIds;
}
