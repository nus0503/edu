package com.company.edu.dto.worksheet;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkDeleteRequestDto {

    @NotNull
    @NotEmpty
    private List<Long> ids;

    private boolean force = false;
}
