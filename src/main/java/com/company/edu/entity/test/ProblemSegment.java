package com.company.edu.entity.test;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ProblemSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private Integer orderNum;
    private String contentType;
    private String content;
    private String alignment;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private MediaAsset media;
    // Getters and Setters

    @Transient // DB에 저장되지 않는 임시 필드
    private String renderedContent;

    public String getRenderedContent() {
        return renderedContent;
    }

    public void setRenderedContent(String renderedContent) {
        this.renderedContent = renderedContent;
    }
}