package com.company.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "middle_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MiddleUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_unit_id", nullable = false)
    private MajorUnit majorUnit;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "middleUnit", cascade = CascadeType.ALL)
    private List<MinorUnit> minorUnits = new ArrayList<>();

    @OneToMany(mappedBy = "middleUnit")
    private List<Problem> problems = new ArrayList<>();
}
