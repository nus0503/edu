package com.company.edu.entity.worksheet;

import com.company.edu.dto.worksheet.WorksheetRequest;
import com.company.edu.entity.user.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "worksheet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worksheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worksheet_id")
    private Long worksheetId;

    @Column(name = "tester", nullable = false)
    private String tester;

    @Column(name = "grade", nullable = false, length = 20)
    private String grade;

    @Column(name = "tag", nullable = false, length = 50)
    private String tag;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "problem_count")
    private Integer problemCount;


    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "problem_type")
    private String problemType;

    @Column(name = "mock_exam_included")
    private String mockExamIncluded;

    @Column(name = "auto_grading")
    private Boolean autoGrading;

    @Column(name = "content_range", length = 200)
    private String contentRange;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "is_recommended")
    private Boolean isRecommended;

    @Column(name = "is_wrong_answer")
    private Boolean isWrongAnswer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "view_permission")
    private ViewPermission viewPermission;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Author 정보를 위한 조인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member authorId;

    // 학습지-문제 연결 정보
    @OneToMany(mappedBy = "worksheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("problemOrder ASC")
    private List<WorksheetProblem> worksheetProblems = new ArrayList<>();

    // 학습지 파일 정보
    @OneToMany(mappedBy = "worksheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorksheetFile> worksheetFiles = new ArrayList<>();




    public enum Difficulty {
        하, 중하, 중, 상, 최상;
    }


    public enum Status {
        ACTIVE, DELETED, DRAFT;
    }


    public enum ViewPermission {
        PUBLIC, PRIVATE, RESTRICTED;
    }
    private Worksheet(String tester, String grade, String tag, String title, String description, Integer problemCount, String difficulty, String problemType, String mockExamIncluded, Boolean autoGrading, String contentRange, Boolean isNew, Boolean isRecommended, Boolean isWrongAnswer, Status status, ViewPermission viewPermission, Member author) {
        this.tester = tester;
        this.grade = grade;
        this.tag = tag;
        this.title = title;
        this.description = description;
        this.problemCount = problemCount;
        this.difficulty = difficulty;
        this.problemType = problemType;
        this.mockExamIncluded = mockExamIncluded;
        this.autoGrading = autoGrading;
        this.contentRange = contentRange;
        this.isNew = isNew;
        this.isRecommended = isRecommended;
        this.isWrongAnswer = isWrongAnswer;
        this.status = status;
        this.viewPermission = viewPermission;
        this.authorId = author;
    }

    public static Worksheet generateEntity(WorksheetRequest.WorksheetCreateRequest dto, Member member) {
        String description = dto.getProblemCount() + "문제 | " + dto.getSelectedDifficulty() + " | " + dto.getContentRange();
        return new Worksheet(dto.getTester(),
                dto.getGrade(),
                dto.getTag(),
                dto.getTitle(),
                description,
                dto.getProblemCount(),
                dto.getSelectedDifficulty(),
                dto.getSettings().getProblemType(),
                dto.getSettings().getMockExamIncluded(),
                dto.getSettings().isAutoGrading(),
                dto.getContentRange(),
                false,
                false,
                false,
                Status.ACTIVE,
                ViewPermission.PUBLIC, member);
    }

    public void updateProblemCountAndDescription(Integer problemCount, String title, String tester, String tag) {
        this.problemCount = problemCount;
        this.description = problemCount + "문제 | " + this.difficulty + " | " + this.contentRange;
        this.title = title;
        this.tester = tester;
        this.tag = tag;

    }
}