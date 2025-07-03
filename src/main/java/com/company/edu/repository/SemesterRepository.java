package com.company.edu.repository;

import com.company.edu.entity.Semesters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemesterRepository extends JpaRepository<Semesters, Integer> {

    @Query("SELECT s FROM Semesters s WHERE s.gradeLevel = :gradeLevel AND s.semesterName = :semesterName")
    Semesters findByGradeLevelAndSemesterName(@Param("gradeLevel") Semesters.GradeLevel gradeLevel,
                                              @Param("semesterName") String semesterName);

    List<Semesters> findByGradeLevel(Semesters.GradeLevel gradeLevel);
}
