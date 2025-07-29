package com.company.edu.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchPwdRequestDto {

    private String name;

    private String phoneNumber;

    private String email;
}
