package com.company.edu.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleType {

    GUEST("ROLE_GUEST", "방문자"),
    USER("ROLE_USER", "가입자"),
    STUDENT("ROLE_STUDENT", "학생"),
    TEACHER("ROLE_TEACHER", "선생님"),
    ADMIN("ROLE_ADMIN", "운영자");

    private final String key;
    private final String title;
}
