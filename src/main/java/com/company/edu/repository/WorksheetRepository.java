package com.company.edu.repository;

import com.company.edu.entity.worksheet.Worksheet;
import com.company.edu.entity.user.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorksheetRepository extends JpaRepository<Worksheet, Long> {
    Page<Worksheet> findByStatusAndAuthorId(Worksheet.Status status, Member memberId, Pageable pageable);
}
