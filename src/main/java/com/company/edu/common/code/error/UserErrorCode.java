package com.company.edu.common.code.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    NOT_ACCESS_AUTHORITY(HttpStatus.FORBIDDEN, "접근할 수 없습니다."),
    NOT_FOUND_INFORMATION(HttpStatus.NOT_FOUND, "등록된 정보가 없습니다."),
    NOT_SIGNUP_USER(HttpStatus.BAD_REQUEST, "해당 이메일은 사용할 수 없습니다."),
    NOT_ACCESS_USER(HttpStatus.BAD_REQUEST, "등록되지 않은 아이디이거나 아이디 또는 비밀번호를 잘못 입력했습니다."),
    NOT_FOUND_USER(HttpStatus.FORBIDDEN, "등록된 유저가 아닙니다."),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성에 실패했습니다."),
    MAX_TOKEN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "사용자당 최대 토큰 개수를 초과했습니다."),
    TOKEN_REVOCATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 무효화에 실패했습니다.");


    private final HttpStatus httpStatus;

    private final String message;
}
