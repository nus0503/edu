package com.company.edu.entity.test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT p FROM Problem p JOIN FETCH p.segments WHERE p.id = :id")
    Problem findByIdWithSegments(@Param("id") Long id);
}
