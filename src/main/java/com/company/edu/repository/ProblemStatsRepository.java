package com.company.edu.repository;

import com.company.edu.entity.problem.ProblemStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemStatsRepository extends JpaRepository<ProblemStats, Integer> {

    // 특정 문제의 통계 조회
    Optional<ProblemStats> findByProblemId(Integer problemId);

    // 정답률 업데이트
    @Query("UPDATE ProblemStats ps SET ps.correctRate = :correctRate, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.problemId = :problemId")
    void updateCorrectRate(@Param("problemId") Integer problemId, @Param("correctRate") Double correctRate);

    // 시도 횟수 증가
    @Query("UPDATE ProblemStats ps SET ps.totalAttempts = ps.totalAttempts + 1, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.problemId = :problemId")
    void incrementTotalAttempts(@Param("problemId") Integer problemId);

    // 정답 횟수 증가
    @Query("UPDATE ProblemStats ps SET ps.correctAttempts = ps.correctAttempts + 1, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.problemId = :problemId")
    void incrementCorrectAttempts(@Param("problemId") Integer problemId);
}