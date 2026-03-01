package org.example.bakeryapi.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtils {

    // Hard limit to protect the service from unbounded "size" requests.
    private static final int MAX_PAGE_SIZE = 100;

    private PageableUtils() {
    }

    public static Pageable safe(Pageable pageable) {
        return PageRequest.of(
                Math.max(0, pageable.getPageNumber()),
                Math.min(MAX_PAGE_SIZE, Math.max(1, pageable.getPageSize())),
                pageable.getSort()
        );
    }
}
