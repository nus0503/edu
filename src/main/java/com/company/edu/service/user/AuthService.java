package com.company.edu.service.user;

import com.company.edu.dto.user.LoginRequestDto;

public interface AuthService {

    public String login(LoginRequestDto dto);
}
