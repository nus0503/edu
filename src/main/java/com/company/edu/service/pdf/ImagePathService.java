package com.company.edu.service.pdf;

import com.company.edu.common.path.FileProperties;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImagePathService {
    private final FileProperties fileProperties;

    // 컬럼 너비 (PDF 생성용)
    private static final float COLUMN_WIDTH = (PageSize.A4.getWidth() - 80 - 20) / 2; // 마진과 갭 고려

    /**
     * 이미지 정보 클래스
     */
    public static class ImageInfo {
        public final float width;
        public final float height;
        public final ImageData imageData;

        public ImageInfo(float width, float height, ImageData imageData) {
            this.width = width;
            this.height = height;
            this.imageData = imageData;
        }
    }

    /**
     * DB 파일명을 전체 경로로 변환
     */
    public String getFullFilePath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        String uploadDir = fileProperties.getUploadDir();
        if (!uploadDir.endsWith("/") && !uploadDir.endsWith("\\")) {
            uploadDir += File.separator;
        }

        return uploadDir + fileName;
    }

    /**
     * 웹 접근 URL 반환 (클라이언트용)
     */
    public String getWebUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        return fileProperties.getBaseUrl() + "/uploads/" + fileName;
    }

    /**
     * 파일 존재 확인
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        String fullPath = getFullFilePath(fileName);
        return Files.exists(Paths.get(fullPath));
    }

    /**
     * PDF 생성용 이미지 정보 가져오기
     */
    public ImageInfo getImageInfo(String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                log.debug("파일명이 비어있음");
                return new ImageInfo(0, 0, null);
            }

            String fullPath = getFullFilePath(fileName);

            // 파일 존재 확인
            if (!fileExists(fileName)) {
                log.warn("이미지 파일 없음: {}", fullPath);
                return new ImageInfo(0, 0, null);
            }

            // iText ImageData 생성
            ImageData imageData = ImageDataFactory.create(fullPath);

            // 원본 크기
            float originalWidth = imageData.getWidth();
            float originalHeight = imageData.getHeight();

            // 컬럼에 맞게 크기 조정
            float maxWidth = COLUMN_WIDTH - 20; // 패딩 고려
            float scaledWidth = Math.min(originalWidth, maxWidth);
            float scaledHeight = (originalHeight * scaledWidth) / originalWidth;

            log.debug("이미지 정보: {}x{} -> {}x{}",
                    originalWidth, originalHeight, scaledWidth, scaledHeight);

            return new ImageInfo(scaledWidth, scaledHeight, imageData);

        } catch (Exception e) {
            log.error("이미지 정보 가져오기 실패: {}", fileName, e);
            return new ImageInfo(0, 0, null);
        }
    }

    /**
     * 이미지 크기 미리 확인 (레이아웃 계산용)
     */
    public float getImageHeight(String fileName) {
        ImageInfo info = getImageInfo(fileName);
        return info.height;
    }

    /**
     * 이미지 파일 유효성 검증
     */
    public boolean validateImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // 확장자 확인
        String extension = getFileExtension(fileName).toLowerCase();
        Set<String> allowedExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp");

        if (!allowedExtensions.contains(extension)) {
            log.warn("지원하지 않는 이미지 형식: {}", fileName);
            return false;
        }

        // 파일 존재 확인
        return fileExists(fileName);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
