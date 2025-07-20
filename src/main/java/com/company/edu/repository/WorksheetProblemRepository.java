package com.company.edu.repository;

import com.company.edu.entity.worksheet.Worksheet;
import com.company.edu.entity.worksheet.WorksheetProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorksheetProblemRepository extends JpaRepository<WorksheetProblem, Long> {


    /**
     * 학습지의 문제들을 순서대로 조회
     */
    List<WorksheetProblem> findByWorksheetOrderByProblemOrderAsc(Worksheet worksheet);

    /**
     * 학습지별 문제 개수 조회
     */
    long countByWorksheet(Worksheet worksheet);

    /**
     * 특정 순서의 문제 조회
     */
    Optional<WorksheetProblem> findByWorksheetAndProblemOrder(Worksheet worksheet, Integer problemOrder);


    /**
     * FETCH JOIN으로 N+1 문제 해결
     */
    @Query("SELECT wp FROM WorksheetProblem wp " +
            "JOIN FETCH wp.problem p " +
            "WHERE wp.worksheet = :worksheet " +
            "ORDER BY wp.problemOrder ASC")
    List<WorksheetProblem> findByWorksheetWithProblemFetch(@Param("worksheet") Worksheet worksheet);


    List<WorksheetProblem> findAllByWorksheetOrderByProblemOrderAsc(Worksheet worksheet);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WorksheetProblem wp WHERE wp.worksheet.worksheetId = :worksheetId")
    void deleteByWorksheetId(@Param("worksheetId") Long worksheetId);
}
