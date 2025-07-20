package com.company.edu.repository;

import com.company.edu.dto.worksheet.ProblemDTO;
import com.company.edu.entity.problem.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT new com.company.edu.dto.worksheet.ProblemDTO(" +
            "p.id, " +
            "CAST(p.problemType AS string), " +
            "p.difficulty, " +
            "p.minorUnit.name, " +
            "p.imagePath, " +
            "p.solution, " +
            "p.hint, " +
            "COALESCE(CAST(ps.correctRate AS integer), 0), " + // BigDecimal을 Integer로 캐스팅
            "CASE " +
            "  WHEN COALESCE(ps.correctRate, 0) >= 80 THEN '기본' " +
            "  WHEN COALESCE(ps.correctRate, 0) <= 60 THEN '신경향' " +
            "  ELSE '표준' " +
            "END) " +
            "FROM Problem p " +
            "LEFT JOIN p.problemStats ps " +
            "WHERE p.minorUnit.id IN :unitIds " +
            "AND (:difficulties IS NULL OR p.difficulty IN :difficulties) " +
            "AND (:problemType IS NULL OR :problemType = '전체' OR CAST(p.problemType AS string) = :problemType) " +
            "ORDER BY p.difficulty, p.id")
    List<ProblemDTO> findProblemsByUnitIdsAndFilters(
            @Param("unitIds") List<Long> minorUnitIds,
            @Param("difficulties") List<String> difficulties,
            @Param("problemType") String problemType
    );

    @Query("SELECT new com.company.edu.dto.worksheet.ProblemDTO(" +
            "p.id," +
            "CAST(p.problemType AS string)," +
            "p.difficulty," +
            "p.minorUnit.name," +
            "p.imagePath," +
            "p.solution," +
            "p.hint," +
            "COALESCE(CAST(ps.correctRate AS integer), 0)," +
            "CASE " +
            "WHEN COALESCE(ps.correctRate, 0) >= 80 THEN '기본'" +
            "WHEN COALESCE(ps.correctRate, 0) <= 60 THEN '신경향'" +
            "ELSE '표준'" +
            "END)" +
            "FROM Problem p " +
            "LEFT JOIN p.problemStats ps " +
            "WHERE p.id = :id")
    ProblemDTO findProblemDto(@Param("id") Long problemId);

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.minorUnit.name IN :unitNames")
    int countProblemsByUnits(@Param("unitNames") List<String> unitNames);

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.minorUnit.name IN :unitNames AND p.problemType = :type")
    int countProblemsByUnitsAndType(@Param("unitNames") List<String> unitNames, @Param("type") Problem.ProblemType type);

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.minorUnit.name IN :unitNames AND p.difficulty = :difficulty")
    int countProblemsByUnitsAndDifficulty(@Param("unitNames") List<String> unitNames, @Param("difficulty") String difficulty);

    @Query("SELECT AVG(ps.correctRate) FROM Problem p LEFT JOIN p.problemStats ps WHERE p.minorUnit.name IN :unitIds")
    Double getAverageCorrectRateByUnits(@Param("unitIds") List<Long> minorUnitIds);


    @Query("SELECT COUNT(p) FROM Problem p " +
            "WHERE p.minorUnit.name IN :unitIds " +
            "AND (:problemType IS NULL OR :problemType = '전체' OR CAST(p.problemType AS string) = :problemType) " +
            "AND (:excludeIds IS NULL OR p.id NOT IN :excludeIds)")
    int countAvailableProblemsExcluding(@Param("unitIds") List<Long> minorUnitIds, @Param("problemType") String problemType, @Param("excludeIds") Set<Long> excludeIds);



    @Query("SELECT new com.company.edu.dto.worksheet.ProblemDTO(" +
            "p.id, " +
            "CAST(p.problemType AS string), " +
            "p.difficulty, " +
            "p.minorUnit.name, " +
            "p.imagePath, " +
            "p.solution, " +
            "p.hint, " +
            "COALESCE(CAST(ps.correctRate AS integer), 0), " +
            "CASE " +
            "  WHEN COALESCE(ps.correctRate, 0) >= 80 THEN '기본' " +
            "  WHEN COALESCE(ps.correctRate, 0) <= 60 THEN '신경향' " +
            "  ELSE '표준' " +
            "END) " +
            "FROM Problem p " +
            "LEFT JOIN p.problemStats ps " +
            "WHERE p.minorUnit.id IN :unitIds " +
            "AND (:difficulties IS NULL OR p.difficulty IN :difficulties) " +
            "AND (:problemType IS NULL OR :problemType = '전체' OR CAST(p.problemType AS string) = :problemType) " +
            "AND (:excludeIds IS NULL OR p.id NOT IN :excludeIds) " +
            "ORDER BY p.difficulty, p.id")
    List<ProblemDTO> findProblemsByUnitsAndFiltersExcludingWithPaging(@Param("unitIds") List<Long> minorUnitIds,
                                                                      @Param("difficulties") List<String> difficulties,
                                                                      @Param("problemType") String problemType,
                                                                      @Param("excludeIds") Set<Long> excludeIds,
                                                                      Pageable pageable);


//    @Query("SELECT Problem FROM Problem p WHERE p.id IN ")
//    List<Problem> findSavedWorksheetProblem(@Param("problemId") Long problemId);
}