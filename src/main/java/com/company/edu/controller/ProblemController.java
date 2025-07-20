package com.company.edu.controller;


import com.company.edu.dto.worksheet.ProblemDTO;
import com.company.edu.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ProblemController {


    private final ProblemService problemService;

    @GetMapping("/{problemId}")
    public ResponseEntity<ProblemDTO> getProblem(@PathVariable Long problemId) {
        try {
            ProblemDTO problemDetail = problemService.getProblemDetail(problemId);
            return ResponseEntity.ok(problemDetail);
        } catch (Exception e) {
            log.error("문제 상세 정보 조회 실패: problemId={}", problemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
