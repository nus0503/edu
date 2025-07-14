package com.company.edu.common.code.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WorksheetErrorCode implements ErrorCode{

    WORKSHEET_NOT_FOUND(HttpStatus.BAD_REQUEST, "학습지를 찾을 수 없습니다"),
    WORKSHEET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "학습지에 접근할 권한이 없습니다"),
    PDF_GENERATION_FAILED(HttpStatus.BAD_REQUEST, "PDF 생성에 실패했습니다"),
    NO_PROBLEMS_FOUND(HttpStatus.BAD_REQUEST, "학습지에 문제가 없습니다"),
    IMAGE_LOAD_FAILED(HttpStatus.BAD_REQUEST, "이미지 로드에 실패했습니다");

    private final HttpStatus httpStatus;

    private final String message;
}
