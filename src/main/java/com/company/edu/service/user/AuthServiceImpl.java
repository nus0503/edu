package com.company.edu.service.user;

import com.company.edu.common.code.error.UserErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.config.user.CustomUserDetails;
import com.company.edu.dto.user.CustomUserInfoDto;
import com.company.edu.dto.user.LoginRequestDto;
import com.company.edu.dto.user.SignUpRequestDto;
import com.company.edu.entity.user.Member;
import com.company.edu.entity.user.RoleType;
import com.company.edu.repository.user.MemberRepository;
import com.company.edu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService{

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public String login(LoginRequestDto dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        Member member = memberRepository.findMemberByEmail(email);

        if (member == null) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_USER);
        }

        if (!encoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        CustomUserInfoDto info = modelMapper.map(member, CustomUserInfoDto.class);

        String accessToken = jwtUtil.createAccessToken(info);
        return accessToken;
    }

    @Override
    @Transactional
    public Long signUp(SignUpRequestDto dto) {

        memberRepository.findByEmail(dto.getEmail())
                .ifPresent(member -> {
                    throw new RestApiException(UserErrorCode.NOT_SIGNUP_USER);
                });

        String password = dto.getPassword();
        String encodedPassword = encoder.encode(password);
        dto.setPassword(encodedPassword);
        Member member = modelMapper.map(dto, Member.class);
        member.setRole(RoleType.USER);
        return memberRepository.save(member).getMemberId();
    }

}
