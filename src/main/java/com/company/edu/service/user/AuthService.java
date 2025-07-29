package com.company.edu.service.user;

import com.company.edu.dto.user.LoginRequestDto;
import com.company.edu.dto.user.SearchIdRequestDto;
import com.company.edu.dto.user.SearchPwdRequestDto;
import com.company.edu.dto.user.SignUpRequestDto;

public interface AuthService {

    public String login(LoginRequestDto dto);

    public Long signUp(SignUpRequestDto dto);

    public String searchId(SearchIdRequestDto dto);

    public String searchPwd(SearchPwdRequestDto dto);
}
