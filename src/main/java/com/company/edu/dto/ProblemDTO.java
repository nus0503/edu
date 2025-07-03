// ProblemDTO.java - 수정된 버전
package com.company.edu.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProblemDTO {
    private Integer id;
    private String problemType;
    private String difficulty;
    private String unitName;
    private String problemImagePath;
    private String solution;
    private String hint;
    private Double correctRate;
    private String trend;

    // BigDecimal correctRate를 받는 생성자
    public ProblemDTO(Integer id, String problemType, String difficulty,
                      String unitName, String imagePath, String solution,
                      String hint, BigDecimal correctRate, String trend) {
        this.id = id;
        this.problemType = problemType;
        this.difficulty = difficulty;
        this.unitName = unitName;
        this.problemImagePath = convertToWebPath(imagePath);
        this.solution = solution;
        this.hint = hint;
        this.correctRate = correctRate != null ? correctRate.doubleValue() : 0.0;
        this.trend = trend;
    }

    // Double correctRate를 받는 생성자
    public ProblemDTO(Integer id, String problemType, String difficulty,
                      String unitName, String imagePath, String solution,
                      String hint, Double correctRate, String trend) {
        this.id = id;
        this.problemType = problemType;
        this.difficulty = difficulty;
        this.unitName = unitName;
        this.problemImagePath = convertToWebPath(imagePath);
        this.solution = solution;
        this.hint = hint;
        this.correctRate = correctRate != null ? correctRate : 0.0;
        this.trend = trend;
    }

    // Integer correctRate를 받는 생성자 (JPQL에서 int로 인식하는 경우)
    public ProblemDTO(Integer id, String problemType, String difficulty,
                      String unitName, String imagePath, String solution,
                      String hint, Integer correctRate, String trend) {
        this.id = id;
        this.problemType = problemType;
        this.difficulty = difficulty;
        this.unitName = unitName;
        this.problemImagePath = convertToWebPath(imagePath);
        this.solution = solution;
        this.hint = hint;
        this.correctRate = correctRate != null ? correctRate.doubleValue() : 0.0;
        this.trend = trend;
    }

    // 파일 시스템 경로를 웹 URL로 변환
    private String convertToWebPath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // 이미 웹 경로 형태인 경우
        if (imagePath.startsWith("/uploads/") || imagePath.startsWith("http")) {
            return imagePath;
        }

        // 파일명만 있는 경우 (problem1.png)
        if (!imagePath.contains("/") && !imagePath.contains("\\")) {
            return "/uploads/problems/" + imagePath;
        }

        // 전체 경로가 있는 경우
        if (imagePath.contains("uploads")) {
            String fileName = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
            if (fileName.equals(imagePath)) {
                fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            }
            return "/uploads/problems/" + fileName;
        }

        return "/uploads/problems/" + imagePath;
    }
}