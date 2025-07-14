package com.company.edu.controller;

import com.company.edu.common.code.error.UserErrorCode;
import com.company.edu.common.code.error.WorksheetErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.config.user.CustomUserDetails;
import com.company.edu.dto.UpdateWorksheetRequestDto;
import com.company.edu.dto.WorksheetRequest;
import com.company.edu.dto.WorksheetResponse;
import com.company.edu.entity.worksheet.Worksheet;
import com.company.edu.entity.worksheet.WorksheetProblem;
import com.company.edu.service.WorksheetService;
import com.company.edu.service.pdf.CompletePdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worksheet")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class WorksheetController {

    private final WorksheetService worksheetService;
    private final CompletePdfGenerator pdfGenerator;

    @PostMapping("/generate")
    public ResponseEntity<WorksheetResponse> generateWorksheet(@RequestBody WorksheetRequest request) {
        try {
            WorksheetResponse response = worksheetService.generateWorksheet(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveWorksheet(@RequestBody WorksheetRequest.WorksheetCreateRequest request) {

        worksheetService.saveWorksheet(request);
        Map<String, Object> response = new HashMap<>();
        response.put("ok", "ok");
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<WorksheetResponse.WorksheetListResponse> getWorksheet(@RequestParam String page, @RequestParam String size) {
        WorksheetResponse.WorksheetListResponse response = worksheetService.getWorksheet(page, size);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/get/{worksheetId}")
    public ResponseEntity<WorksheetResponse> getSavedWorksheetProblem(@PathVariable Long worksheetId) {
        WorksheetResponse savedWorksheetProblem = worksheetService.getSavedWorksheetProblem(worksheetId);
        return ResponseEntity.ok(savedWorksheetProblem);
    }

    @PutMapping("/update/{worksheetId}")
    public ResponseEntity<?> updateWorksheet(@PathVariable Long worksheetId, UpdateWorksheetRequestDto request) {
        return null;
    }


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Worksheet service is running");
    }


    @Transactional(readOnly = true)
    @GetMapping("/{worksheetId}/preview/problem")
    public ResponseEntity<Resource> getWorksheetPreview(@PathVariable Long worksheetId) {
        try {
            log.info("문제지 PDF 생성 요청: worksheetId={}", worksheetId);

            // 1. 학습지 정보 조회
            Worksheet worksheet = worksheetService.getWorksheetById(worksheetId);

            // 2. 권한 확인
            validateWorksheetAccess(worksheet);

            // 3. 학습지의 문제들 조회 (순서대로)
            List<WorksheetProblem> problems = worksheetService.getWorksheetProblemsOrdered(worksheetId);

            if (problems.isEmpty()) {
                throw new RestApiException(WorksheetErrorCode.NO_PROBLEMS_FOUND);
            }

            // 4. PDF 생성 (문제 + 정답)
            byte[] pdfBytes = pdfGenerator.generateProblemWithAnswerPdf(worksheet, problems);

            // 5. 응답 생성
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            String fileName = String.format("%s_문제지_%s.pdf",
                    sanitizeFileName(worksheet.getTitle()),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));

            log.info("문제지 PDF 생성 완료: worksheetId={}, 파일크기={}KB",
                    worksheetId, pdfBytes.length / 1024);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdfBytes.length))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(resource);

        } catch (RestApiException e) {
            log.error("문제지 PDF 생성 실패 - 비즈니스 오류: worksheetId={}", worksheetId, e);
            throw e;
        } catch (Exception e) {
            log.error("문제지 PDF 생성 실패 - 시스템 오류: worksheetId={}", worksheetId, e);
            throw new RestApiException(WorksheetErrorCode.PDF_GENERATION_FAILED);
        }
    }

    /**
     * 학습지 접근 권한 확인
     */
    private void validateWorksheetAccess(Worksheet worksheet) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RestApiException(UserErrorCode.NOT_ACCESS_AUTHORITY);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getMember().getMemberId();

        // 작성자 본인이거나 공개된 학습지만 접근 가능
        boolean isAuthor = worksheet.getAuthorId().getMemberId().equals(currentUserId);
        boolean isPublic = worksheet.getViewPermission() == Worksheet.ViewPermission.PUBLIC;

        if (!isAuthor && !isPublic) {
            log.warn("학습지 접근 권한 없음: userId={}, worksheetId={}, permission={}",
                    currentUserId, worksheet.getWorksheetId(), worksheet.getViewPermission());
            throw new RestApiException(WorksheetErrorCode.WORKSHEET_ACCESS_DENIED);
        }
    }



    /**
     * 파일명 안전화 (특수문자 제거)
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "학습지";
        }

        // 파일명에 사용할 수 없는 문자들 제거
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .substring(0, Math.min(fileName.length(), 50)); // 길이 제한
    }






}