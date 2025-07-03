package com.company.edu.controller;

import com.company.edu.dto.WorksheetRequest;
import com.company.edu.dto.WorksheetResponse;
import com.company.edu.service.WorksheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Worksheet service is running");
    }
}