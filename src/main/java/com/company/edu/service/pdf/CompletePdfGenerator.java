package com.company.edu.service.pdf;

import com.company.edu.entity.problem.Problem;
import com.company.edu.entity.worksheet.Worksheet;
import com.company.edu.entity.worksheet.WorksheetProblem;
import com.company.edu.service.pdf.ImagePathService;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompletePdfGenerator {

    private final ImagePathService imagePathService;

    // 폰트 캐시 (성능 향상)
    private static PdfFont cachedRegularFont;
    private static PdfFont cachedBoldFont;

    // PDF 레이아웃 상수
    private static final float PAGE_WIDTH = PageSize.A4.getWidth();
    private static final float PAGE_HEIGHT = PageSize.A4.getHeight();
    private static final float MARGIN = 40f;
    private static final float COLUMN_GAP = 20f;
    private static final float COLUMN_WIDTH = (PAGE_WIDTH - 2 * MARGIN - COLUMN_GAP) / 2;
    private static final float HEADER_HEIGHT = 120f;
    private static final float FOOTER_HEIGHT = 30f;
    private static final float USABLE_HEIGHT = PAGE_HEIGHT - MARGIN * 2 - HEADER_HEIGHT - FOOTER_HEIGHT;
    private static final float PROBLEM_NUMBER_HEIGHT = 25f;
    private static final float ANSWER_HEIGHT = 20f;
    private static final float PROBLEM_SPACING = 15f;

    /**
     * 문제지 PDF 생성 (문제 + 정답)
     */
    @Transactional
    public byte[] generateProblemWithAnswerPdf(Worksheet worksheet, List<WorksheetProblem> problems) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

            PdfFont regularFont = createKoreanFont();
            PdfFont boldFont = createKoreanBoldFont();

            // 동적 레이아웃 매니저 생성
            ImageLayoutManager layoutManager = new ImageLayoutManager(
                    document, worksheet, regularFont, boldFont, imagePathService
            );

            // 문제들을 동적으로 배치
            layoutManager.layoutProblemsWithAnswers(problems);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF 생성 실패", e);
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    /**
     * 이미지 기반 동적 레이아웃 매니저
     */
    private static class ImageLayoutManager {
        private final Document document;
        private final Worksheet worksheet;
        private final PdfFont regularFont;
        private final PdfFont boldFont;
        private final ImagePathService imagePathService;

        // 현재 페이지 상태
        private int currentPage = 1;
        private float leftColumnY = USABLE_HEIGHT;
        private float rightColumnY = USABLE_HEIGHT;
        private boolean isFirstPage = true;

        public ImageLayoutManager(Document document, Worksheet worksheet,
                                  PdfFont regularFont, PdfFont boldFont,
                                  ImagePathService imagePathService) {
            this.document = document;
            this.worksheet = worksheet;
            this.regularFont = regularFont;
            this.boldFont = boldFont;
            this.imagePathService = imagePathService;

            // 첫 페이지는 헤더 공간을 고려하여 시작 위치를 낮게 설정
            // 헤더가 대략 150px 정도 차지한다고 가정
            this.leftColumnY = USABLE_HEIGHT - 150;
            this.rightColumnY = USABLE_HEIGHT - 150;

        }

        /**
         * 문제와 정답을 함께 배치
         */
        public void layoutProblemsWithAnswers(List<WorksheetProblem> problems) {
            // 첫 페이지 헤더 생성
            createPageHeader();

            for (WorksheetProblem worksheetProblem : problems) {
                Problem problem = worksheetProblem.getProblem();

                // 1. 문제 이미지 정보 가져오기
                ProblemImageInfo problemImageInfo = getProblemImageInfo(problem);

                // 2. 전체 문제 블록에 필요한 높이 계산
                float requiredHeight = calculateTotalRequiredHeight(worksheetProblem, problemImageInfo);

                // 3. 배치 위치 결정 (스마트 레이아웃)
                ColumnPosition position = determineOptimalPosition(requiredHeight);

                // 4. 필요시 새 페이지 생성
                if (position == ColumnPosition.NEW_PAGE) {
                    createNewPage();
                    position = ColumnPosition.LEFT; // 새 페이지에서는 왼쪽부터
                }

                // 5. 문제 블록 전체 배치
                placeProblemBlock(worksheetProblem, problemImageInfo, position);
            }

            // 마지막 페이지 번호 추가
            addPageNumber();
        }

        /**
         * 문제 이미지 정보 수집
         */
        private ProblemImageInfo getProblemImageInfo(Problem problem) {
            // 문제 이미지 정보
            ImagePathService.ImageInfo questionImage = imagePathService.getImageInfo(problem.getImageUrl());

            return new ProblemImageInfo(questionImage);
        }

        /**
         * 문제 블록 전체 높이 계산
         */
        private float calculateTotalRequiredHeight(WorksheetProblem worksheetProblem,
                                                   ProblemImageInfo imageInfo) {
            float totalHeight = 0;

            // 1. 문제 번호 영역
            totalHeight += PROBLEM_NUMBER_HEIGHT;

            // 2. 문제 이미지 높이
            totalHeight += imageInfo.questionImage.height;

            // 3. 문제와 정답 사이 간격
            totalHeight += 10f;

            // 4. 정답 영역 높이
            totalHeight += ANSWER_HEIGHT;

            // 5. 문제 간 간격
            totalHeight += PROBLEM_SPACING;

            return totalHeight;
        }

        /**
         * 최적 배치 위치 결정 (스마트 레이아웃)
         */
        private ColumnPosition determineOptimalPosition(float requiredHeight) {
            // 1. 왼쪽 컬럼에 충분한 공간이 있는지 확인
            if (leftColumnY >= requiredHeight) {
                return ColumnPosition.LEFT;
            }

            // 2. 오른쪽 컬럼에 충분한 공간이 있는지 확인
            if (rightColumnY >= requiredHeight) {
                return ColumnPosition.RIGHT;
            }

            // 3. 둘 다 부족하면 새 페이지 필요
            return ColumnPosition.NEW_PAGE;
        }

        /**
         * 문제 블록 전체 배치
         */
        private void placeProblemBlock(WorksheetProblem worksheetProblem,
                                  ProblemImageInfo imageInfo, ColumnPosition position) {

            // 배치 시작 위치 계산
            float xPosition = (position == ColumnPosition.LEFT) ?
                    MARGIN : MARGIN + COLUMN_WIDTH + COLUMN_GAP;

            float startY = (position == ColumnPosition.LEFT) ? leftColumnY : rightColumnY;
            float currentY = startY;

            Problem problem = worksheetProblem.getProblem();

            // 1. 문제 번호 배치
            currentY = placeProblemNumber(worksheetProblem, xPosition, currentY);

            // 2. 문제 이미지 배치
            currentY = placeProblemImage(imageInfo.questionImage, xPosition, currentY);

            // 3. 정답 배치
            currentY = placeAnswer(problem, xPosition, currentY);

            // 4. 사용한 높이만큼 컬럼 Y 위치 업데이트
            float usedHeight = startY - currentY + PROBLEM_SPACING;
            if (position == ColumnPosition.LEFT) {
                leftColumnY = currentY - PROBLEM_SPACING;
            } else {
                rightColumnY = currentY - PROBLEM_SPACING;
            }

            log.debug("문제 {} 배치 완료: 위치={}, 사용높이={}",
                    worksheetProblem.getProblemOrder(), position, usedHeight);
        }

        /**
         * 문제 번호 배치
         */
        private float placeProblemNumber(WorksheetProblem worksheetProblem, float x, float y) {
            // 문제 번호 스타일
            Paragraph problemNumber = new Paragraph(String.format("%02d", worksheetProblem.getProblemOrder()))
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setFontColor(DeviceRgb.RED)
                    .setTextAlignment(TextAlignment.LEFT);

            float numberY = y - PROBLEM_NUMBER_HEIGHT;
            problemNumber.setFixedPosition(x, numberY, COLUMN_WIDTH);
            document.add(problemNumber);

            return numberY - 5; // 5px 간격
        }

        /**
         * 문제 이미지 배치
         */
        private float placeProblemImage(ImagePathService.ImageInfo imageInfo, float x, float y) {
            if (imageInfo.imageData == null) {
                // 이미지가 없는 경우 빈 박스 표시
                return placeEmptyImageBox(x, y);
            }

            try {
                Image image = new Image(imageInfo.imageData);
                image.setWidth(imageInfo.width);
                image.setHeight(imageInfo.height);

                // 이미지를 컬럼 중앙에 정렬
                float imageX = x + (COLUMN_WIDTH - imageInfo.width) / 2;
                float imageY = y - imageInfo.height;

                image.setFixedPosition(imageX, imageY);

                // 이미지 주변에 테두리 박스 그리기
                drawImageBorder(x + 5, imageY - 5, COLUMN_WIDTH - 10, imageInfo.height + 10);

                document.add(image);

                return imageY - 5; // 5px 간격

            } catch (Exception e) {
                log.error("문제 이미지 배치 실패", e);
                return placeEmptyImageBox(x, y);
            }
        }

        /**
         * 빈 이미지 박스 배치 (이미지 로드 실패시)
         */
        private float placeEmptyImageBox(float x, float y) {
            float boxHeight = 60f; // 기본 높이
            float boxY = y - boxHeight;

            // 빈 박스 그리기
            drawImageBorder(x + 5, boxY - 5, COLUMN_WIDTH - 10, boxHeight + 10);

            // "이미지 없음" 텍스트
            Paragraph noImageText = new Paragraph("이미지를 불러올 수 없습니다")
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setFontColor(DeviceRgb.BLUE)
                    .setTextAlignment(TextAlignment.CENTER);

            noImageText.setFixedPosition(x, boxY + boxHeight/2 - 5, COLUMN_WIDTH);
            document.add(noImageText);

            return boxY - 5;
        }

        /**
         * 이미지 테두리 그리기
         */
        private void drawImageBorder(float x, float y, float width, float height) {
            try {
                PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());
                canvas.saveState()
                        .setStrokeColor(DeviceRgb.BLACK)
                        .setLineWidth(0.5f)
                        .rectangle(x, y, width, height)
                        .stroke()
                        .restoreState();
            } catch (Exception e) {
                log.warn("이미지 테두리 그리기 실패", e);
            }
        }

        /**
         * 정답 배치
         */
        private float placeAnswer(Problem problem, float x, float y) {
            // 정답 텍스트 준비
            String answerText = formatAnswer(problem);

            // 정답 스타일
            Paragraph answer = new Paragraph(answerText)
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setFontColor(DeviceRgb.BLUE)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBold();

            float answerY = y - ANSWER_HEIGHT;
            answer.setFixedPosition(x, answerY, COLUMN_WIDTH);
            document.add(answer);

            return answerY - 5; // 5px 간격
        }

        /**
         * 정답 포맷팅
         */
        private String formatAnswer(Problem problem) {
            StringBuilder answer = new StringBuilder("정답: ");

            if (problem.getProblemType() == Problem.ProblemType.객관식) {
                // 객관식: 선택지 번호
                String correctChoice = extractCorrectChoice(problem.getSolution());
                answer.append(correctChoice);
            } else {
                // 주관식/서술형: 답안 텍스트
                String answerText = problem.getSolution();
                if (answerText != null && !answerText.isEmpty()) {
                    // 너무 긴 답안은 줄임
                    if (answerText.length() > 30) {
                        answer.append(answerText.substring(0, 27)).append("...");
                    } else {
                        answer.append(answerText);
                    }
                } else {
                    answer.append("답안 정보 없음");
                }
            }

            return answer.toString();
        }

        /**
         * 객관식 정답 추출
         */
        private String extractCorrectChoice(String answerText) {
            if (answerText == null || answerText.isEmpty()) {
                return "정답 없음";
            }

            // 정답이 "3" 또는 "③" 형태로 저장된 경우 처리
            try {
                // 숫자인 경우
                int choiceNum = Integer.parseInt(answerText.trim());
                return "③ " + choiceNum + "번";
            } catch (NumberFormatException e) {
                // 기호인 경우 그대로 반환
                return answerText.trim();
            }
        }

        /**
         * 새 페이지 생성
         */
        private void createNewPage() {
            // 현재 페이지 번호 추가
            addPageNumber();

            // 새 페이지 시작
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            currentPage++;

            // Y 위치 초기화
            leftColumnY = USABLE_HEIGHT;
            rightColumnY = USABLE_HEIGHT;
            isFirstPage = false;

            // 헤더 생성
            createPageHeader();

            log.debug("새 페이지 생성: {}", currentPage);
        }

        /**
         * 페이지 헤더 생성
         */
        private void createPageHeader() {
            if (isFirstPage) {
                createFullHeader();
            } else {
                createSimpleHeader();
            }

            // --- [수정된 부분] 모든 페이지에 세로 구분선 그리기 ---
            try {
                // 1. 선을 그릴 가로(X) 위치는 동일합니다. (두 열의 정중앙)
                float lineX = MARGIN + COLUMN_WIDTH + (COLUMN_GAP / 2);

                // 2. 선을 그릴 세로(Y) 위치를 페이지 유형에 맞게 동적으로 계산합니다.
                float startY = MARGIN + FOOTER_HEIGHT; // 선의 시작점: 하단 여백 + 푸터 높이
                float endY; // 선의 끝점: 페이지 종류에 따라 달라짐

                if (isFirstPage) {
                    // 첫 페이지일 경우: 큰 헤더의 높이를 반영합니다.
                    // 헤더 구분선이 대략 (PAGE_HEIGHT - MARGIN - HEADER_HEIGHT) 위치에 있으므로 그 아래부터 시작합니다.
                    endY = PAGE_HEIGHT - MARGIN - HEADER_HEIGHT;
                } else {
                    // 두 번째 페이지 이후일 경우: 작은 헤더의 높이를 반영합니다.
                    // simpleHeader의 구분선이 (PAGE_HEIGHT - MARGIN - 35) 위치에 있습니다.
                    endY = PAGE_HEIGHT - MARGIN - 35;
                }

                // 3. 계산된 위치에 따라 선을 그립니다.
                PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());
                canvas.saveState()
                        .setStrokeColor(ColorConstants.LIGHT_GRAY) // 연한 회색
                        .setLineWidth(0.5f)
                        .moveTo(lineX, startY) // 계산된 시작 Y 위치
                        .lineTo(lineX, endY)   // 계산된 끝 Y 위치
                        .stroke()
                        .restoreState();

            } catch (Exception e) {
                log.warn("세로 구분선 그리기 실패", e);
            }
        }

        /**
         * 첫 페이지 전체 헤더
         */
        private void createFullHeader() {
            // 로고 및 정보 영역
//            Table headerTable = new Table(2).useAllAvailableWidth();
//
//            // 왼쪽: 로고
//            Cell logoCell = new Cell().setBorder(Border.NO_BORDER);
//            Paragraph logo = new Paragraph("⚡오멀학원")
//                    .setFont(boldFont)
//                    .setFontSize(14)
//                    .setFontColor(ColorConstants.WHITE)
//                    .setBackgroundColor(DeviceRgb.BLUE)
//                    .setPadding(8)
//                    .setBorderRadius(new BorderRadius(4));
//            logoCell.add(logo);
//
//            // 오른쪽: 학습지 정보
//            Cell infoCell = new Cell()
//                    .setBorder(Border.NO_BORDER)
//                    .setTextAlignment(TextAlignment.RIGHT);
//
//            infoCell.add(new Paragraph("유형별 학습").setFont(boldFont).setFontSize(12));
//            infoCell.add(new Paragraph(worksheet.getTester()).setFont(regularFont).setFontSize(10));
//            infoCell.add(new Paragraph(worksheet.getProblemCount() + "문제").setFont(regularFont).setFontSize(10));
//
//            headerTable.addCell(logoCell);
//            headerTable.addCell(infoCell);
//            headerTable.setFixedPosition(MARGIN, PAGE_HEIGHT - MARGIN - 50, PAGE_WIDTH - 2 * MARGIN);
//            document.add(headerTable);
//
//            // 이름란과 범위 정보
//            Table infoTable = new Table(2).useAllAvailableWidth();
//
//            Cell nameCell = new Cell().setBorder(Border.NO_BORDER)
//                    .add(new Paragraph("이름 _________________").setFont(regularFont).setFontSize(12));
//
//            Cell rangeCell = new Cell().setBorder(Border.NO_BORDER)
//                    .setTextAlignment(TextAlignment.RIGHT);
//            rangeCell.add(new Paragraph(worksheet.getContentRange() != null ?
//                    worksheet.getContentRange() : "범위 정보 없음")
//                    .setFont(boldFont).setFontSize(14));
//            rangeCell.add(new Paragraph(worksheet.getDescription() != null ?
//                    worksheet.getDescription() : "")
//                    .setFont(regularFont).setFontSize(10));
//
//            infoTable.addCell(nameCell);
//            infoTable.addCell(rangeCell);
//            infoTable.setFixedPosition(MARGIN, PAGE_HEIGHT - MARGIN - 90, PAGE_WIDTH - 2 * MARGIN);
//            document.add(infoTable);
//
//            // 구분선
//            PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());
//            canvas.saveState()
//                    .setStrokeColor(DeviceRgb.GREEN)
//                    .setLineWidth(1)
//                    .moveTo(MARGIN, PAGE_HEIGHT - MARGIN - HEADER_HEIGHT)
//                    .lineTo(PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN - HEADER_HEIGHT)
//                    .stroke()
//                    .restoreState();



            try {
                // 1. 상단 타이틀 바 ("유형별 학습") - 이 부분은 잘 구현되어 있습니다.
                Table titleTable = new Table(1).useAllAvailableWidth();
                titleTable.setMarginBottom(10);
                Cell titleCell = new Cell()
                        .setBorder(Border.NO_BORDER)
                        .setBackgroundColor(new DeviceRgb(220, 220, 220)) // 연한 회색
                        .setPadding(8)
                        .setTextAlignment(TextAlignment.CENTER);
                Paragraph titlePara = new Paragraph(worksheet.getTag() != null ? worksheet.getTag() : "유형별 학습")
                        .setFont(boldFont)
                        .setFontSize(16)
                        .setFontColor(ColorConstants.BLACK);
                titleCell.add(titlePara);
                titleTable.addCell(titleCell);
                document.add(titleTable);

                // 2. 메인 헤더 정보 (2x2 테이블로 재구성)
                Table mainHeaderTable = new Table(2).useAllAvailableWidth();
                mainHeaderTable.setMarginBottom(10);

                // --- 첫 번째 행 ---
                // 좌측 셀: 학원명 + 응시자명
                Cell academyTesterCell = new Cell().setBorder(Border.NO_BORDER);
                academyTesterCell.add(new Paragraph("오성학원") // "모성학원"을 "오성학원"으로 수정
                        .setFont(boldFont).setFontSize(12).setMargin(0));
                academyTesterCell.add(new Paragraph(worksheet.getTester() != null ? worksheet.getTester() : "")
                        .setFont(regularFont).setFontSize(12).setMargin(0));
                mainHeaderTable.addCell(academyTesterCell);

                // 우측 셀: 문제 수
                Cell problemCountCell = new Cell().setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.BOTTOM);
                problemCountCell.add(new Paragraph(worksheet.getProblemCount() + "문제")
                        .setFont(regularFont).setFontSize(12));
                mainHeaderTable.addCell(problemCountCell);


                // --- 두 번째 행 ---
                // 좌측 셀: 이름 입력란
                Cell nameCell = new Cell().setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.BOTTOM);
                nameCell.add(new Paragraph("이름 _________________")
                        .setFont(regularFont).setFontSize(12));
                mainHeaderTable.addCell(nameCell);

                // 우측 셀: 학습 범위
                Cell contentRangeCell = new Cell().setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.BOTTOM);
                contentRangeCell.add(new Paragraph(worksheet.getContentRange() != null ? worksheet.getContentRange() : "범위 정보 없음")
                        .setFont(boldFont).setFontSize(14));
                mainHeaderTable.addCell(contentRangeCell);

                document.add(mainHeaderTable);


                // 3. 구분선 추가
                SolidLine line = new SolidLine(1f);
                line.setColor(ColorConstants.BLACK);
                LineSeparator lineSeparator = new LineSeparator(line);
                lineSeparator.setMarginTop(5);
                document.add(lineSeparator);


                // 4. 문제 시작 위치(Y-coordinate) 업데이트
                // 헤더의 높이를 고려하여 실제 문제가 시작될 위치를 조정해야 합니다.
                // 이 값은 실제 생성된 헤더의 높이에 맞게 미세 조정이 필요할 수 있습니다.
                leftColumnY = PAGE_HEIGHT - MARGIN - 150;
                rightColumnY = PAGE_HEIGHT - MARGIN - 150;


            } catch (Exception e) {
                log.error("헤더 생성 실패", e);
                // 예외 처리
            }
        }

        /**
         * 간소화된 헤더 (2페이지 이후)
         */
        private void createSimpleHeader() {
            Paragraph title = new Paragraph(worksheet.getContentRange())
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);

            title.setFixedPosition(MARGIN, PAGE_HEIGHT - MARGIN - 25, PAGE_WIDTH - 2 * MARGIN);
            document.add(title);

            // 구분선
            PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());
            canvas.saveState()
                    .setStrokeColor(DeviceRgb.GREEN)
                    .setLineWidth(0.5f)
                    .moveTo(MARGIN, PAGE_HEIGHT - MARGIN - 35)
                    .lineTo(PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN - 35)
                    .stroke()
                    .restoreState();
        }

        /**
         * 페이지 번호 추가
         */
        private void addPageNumber() {
            Paragraph pageNum = new Paragraph(String.valueOf(currentPage))
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);

            pageNum.setFixedPosition(PAGE_WIDTH / 2 - 10, 15, 20);
            document.add(pageNum);
        }
    }

    /**
     * 문제 이미지 정보 클래스
     */
    private static class ProblemImageInfo {
        final ImagePathService.ImageInfo questionImage;

        public ProblemImageInfo(ImagePathService.ImageInfo questionImage) {
            this.questionImage = questionImage;
        }
    }

    /**
     * 컬럼 위치 enum
     */
    private enum ColumnPosition {
        LEFT, RIGHT, NEW_PAGE
    }

    // 폰트 생성 메소드
//    private PdfFont createKoreanFont() throws IOException {
//        return PdfFontFactory.createFont("fonts/NanumGothic.ttf", PdfEncodings.IDENTITY_H);
//    }
//
//    private PdfFont createKoreanBoldFont() throws IOException {
//        return PdfFontFactory.createFont("fonts/NanumGothicBold.ttf", PdfEncodings.IDENTITY_H);
//    }

    private PdfFont createKoreanFont()  {
        if (cachedRegularFont != null) {
            return cachedRegularFont;
        }
        try {
            log.debug("한글 폰트 로딩 시도...");

            InputStream fontStream = getClass().getResourceAsStream("/fonts/NanumGothic.ttf");
            if (fontStream != null) {
                byte[] fontBytes = fontStream.readAllBytes();
                cachedRegularFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);
                log.info("✅ 리소스 폰트 로드 성공: NanumGothic.ttf");
                return cachedRegularFont;
            }

        } catch (Exception e) {
            log.error("💥 폰트 생성 완전 실패", e);
            throw new RuntimeException("폰트 생성 실패: " + e.getMessage(), e);
        }
        return null;
    }

    private PdfFont createKoreanBoldFont() throws IOException {
        if (cachedBoldFont != null) {
            return cachedBoldFont;
        }

        try {
            // 볼드 폰트 시도
            try {
                InputStream fontStream = getClass().getResourceAsStream("/fonts/NanumGothicBold.ttf");
                if (fontStream != null) {
                    byte[] fontBytes = fontStream.readAllBytes();
                    cachedBoldFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);
                    log.info("✅ 볼드 폰트 로드 성공");
                    return cachedBoldFont;
                }
            } catch (Exception e) {
                log.debug("볼드 폰트 로드 실패: {}", e.getMessage());
            }

            // 볼드 폰트 실패 시 일반 폰트 사용
            log.warn("⚠️ 볼드 폰트 없음, 일반 폰트 사용");
            cachedBoldFont = createKoreanFont();
            return cachedBoldFont;

        } catch (Exception e) {
            log.error("볼드 폰트 생성 실패", e);
            return createKoreanFont(); // 폴백
        }
    }






}