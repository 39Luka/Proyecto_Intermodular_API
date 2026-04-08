package com.bakery.bakeryapi.shared;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtils {

    private PageableUtils() {
    }

    // Safety: clamp requested page and size to avoid huge queries.
    public static Pageable safe(Pageable pageable, int maxPageSize) {
        return PageRequest.of(
                Math.clamp(pageable.getPageNumber(), 0, Integer.MAX_VALUE),
                Math.clamp(pageable.getPageSize(), 1, maxPageSize),
                pageable.getSort()
        );
    }
}

