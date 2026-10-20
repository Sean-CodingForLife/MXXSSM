package com.mxxssm.common.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pageables {

    public static final int MAX_PAGE_SIZE = 100;

    private Pageables() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage - 1, safeSize, sort);
    }
}
