package com.company.edu.common;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
public class PageableRequest {

    private static final int DEFAULT_PAGE_SIZE = 30;

    private int page = 1;
    private int size = DEFAULT_PAGE_SIZE;



    public Pageable toPageable() {
        return PageRequest.of(this.page - 1, size, createDateDesc());
    }

    private Sort createDateDesc() {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
