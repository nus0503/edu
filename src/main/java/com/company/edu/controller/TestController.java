package com.company.edu.controller;

import com.company.edu.entity.test.ProblemRepository;
import com.company.education.dto.ProblemRequest;
import com.company.education.entity.test.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final ProblemRepository problemRepository;
    private final ProblemSegmentRepository segmentRepository;
    private final ProblemOptionRepository optionRepository;
    private final OptionSegmentRepository optionSegmentRepository;

    @GetMapping("/problem/{id}")
    public String getProblem(@PathVariable Long id, Model model) {
        Problem problem = problemRepository.findByIdWithSegments(id);

        problem.getSegments().forEach(segment -> {
            if ("formula".equals(segment.getContentType())) {
                String renderedFormula = "\\(" + segment.getContent() + "\\)";
                segment.setRenderedContent(renderedFormula);
            } else {
                segment.setRenderedContent(segment.getContent());
            }
        });

        model.addAttribute("problem", problem);
        return "problem";
    }
    @GetMapping("/add-problem")
    public String addProblem() {
        return "add_problem";
    }

    @PostMapping("/api/problems")
    public ResponseEntity<?> createProblem(@RequestBody ProblemRequest request) {
        // 1. 문제 기본 정보 저장
        Problem problem = new Problem();
        problem.setProblemType(request.getType());
        Problem saveProblem = problemRepository.save(problem);

        // 2. 문제 내용 세그먼트 저장
        request.getContent().forEach(seg -> {
            ProblemSegment segment = new ProblemSegment();
            segment.setContentType(seg.getType());
            segment.setContent(seg.getContent());
            segment.setAlignment(seg.getAlignment());
            segment.setOrderNum(seg.getOrderNum());
            segment.setProblem(saveProblem);
            segmentRepository.save(segment);
        });

        // 3. 객관식 옵션 처리
        if ("객관식".equals(request.getType())) {
            request.getOptions().forEach(opt -> {
                ProblemOption option = new ProblemOption();
                option.setProblem(saveProblem);
                option.setIsCorrect(opt.isCorrect());
                option.setOptionNumber(opt.getOptionNumber());
                ProblemOption newOption = optionRepository.save(option);

                opt.getContent().forEach(optSeg -> {
                    OptionSegment os = new OptionSegment();
                    os.setContentType(optSeg.getType());
                    os.setOrderNum(optSeg.getOrderNum());
                    os.setContent(optSeg.getContent());
                    os.setAlignment(optSeg.getAlignment());
                    os.setOption(newOption);
                    optionSegmentRepository.save(os);
                });
            });
        }

        return ResponseEntity.ok().build();
    }
}
