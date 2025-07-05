package com.company.edu.dto.user;

import com.company.edu.entity.user.RoleType;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomUserInfoDto {

    private Long memberId;

    private String email;

    private String name;

    private String password;

    private RoleType role;
}
