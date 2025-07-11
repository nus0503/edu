package com.company.edu.entity;

import com.company.edu.dto.WorksheetRequest;
import com.company.edu.entity.user.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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



    public enum Difficulty {
        하, 중하, 중, 상, 최상
    }

    public enum Status {
        ACTIVE, DELETED, DRAFT
    }

    public enum ViewPermission {
        PUBLIC, PRIVATE, RESTRICTED
    }

    private Worksheet(String tester, String grade, String tag, String title, String description, Integer problemCount, String difficulty, String contentRange, Boolean isNew, Boolean isRecommended, Boolean isWrongAnswer, Status status, ViewPermission viewPermission, Member author) {
        this.tester = tester;
        this.grade = grade;
        this.tag = tag;
        this.title = title;
        this.description = description;
        this.problemCount = problemCount;
        this.difficulty = difficulty;
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
        return new Worksheet(dto.getTester(), dto.getGrade(), dto.getTag(), dto.getTitle(), description, dto.getProblemCount(), dto.getSelectedDifficulty(), dto.getContentRange(), false, false, false, Status.ACTIVE, ViewPermission.PUBLIC, member);
    }
}