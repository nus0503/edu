package com.company.edu.repository;

import com.company.edu.entity.MinorUnit;
import com.company.edu.entity.Semesters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinorUnitRepository extends JpaRepository<MinorUnit, Integer> {

    @Query("SELECT mu FROM MinorUnit mu " +
            "JOIN mu.middleUnit mdu " +
            "JOIN mdu.majorUnit maju " +
            "JOIN maju.semesters s " +
            "WHERE s.gradeLevel = :gradeLevel AND s.semesterName = :semesterName")
    List<MinorUnit> findBySemester(@Param("gradeLevel") Semesters.GradeLevel gradeLevel,
                                   @Param("semesterName") String semesterName);

    @Query("SELECT mu FROM MinorUnit mu " +
            "JOIN mu.middleUnit mdu " +
            "JOIN mdu.majorUnit maju " +
            "WHERE maju.name = :majorUnitName")
    List<MinorUnit> findByMajorUnitName(@Param("majorUnitName") String majorUnitName);

    @Query("SELECT mu FROM MinorUnit mu " +
            "JOIN mu.middleUnit mdu " +
            "WHERE mdu.name = :middleUnitName")
    List<MinorUnit> findByMiddleUnitName(@Param("middleUnitName") String middleUnitName);

    List<MinorUnit> findByName(String name);
}
