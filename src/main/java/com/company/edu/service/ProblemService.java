package com.company.edu.service;

import com.company.edu.common.code.error.CommonErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.dto.worksheet.ProblemDTO;
import com.company.edu.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;


    public ProblemDTO getProblemDetail(Long problemId) {
        try {
            ProblemDTO problemDto = problemRepository.findProblemDto(problemId);
            return problemDto;
        } catch (Exception e) {
            throw new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
