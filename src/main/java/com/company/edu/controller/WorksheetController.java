package com.company.edu.controller;

import com.company.edu.common.code.error.UserErrorCode;
import com.company.edu.common.customException.RestApiException;
import com.company.edu.dto.WorksheetRequest;
import com.company.edu.dto.WorksheetResponse;
import com.company.edu.service.WorksheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/worksheet")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorksheetController {

    private final WorksheetService worksheetService;

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


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Worksheet service is running");
    }
}