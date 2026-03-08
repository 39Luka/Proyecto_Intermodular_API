package com.bakery.api.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtils {

    // Hard limit to protect the service from unbounded "size" requests.
    private static final int MAX_PAGE_SIZE = 100;

    private PageableUtils() {
    }

    public static Pageable safe(Pageable pageable) {
        return PageRequest.of(
                Math.clamp(pageable.getPageNumber(), 0, Integer.MAX_VALUE),
                Math.clamp(pageable.getPageSize(), 1, MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }
}
