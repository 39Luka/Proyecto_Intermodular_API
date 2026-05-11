package com.bakery.bakeryapi.shared;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Helpers for normalizing pagination requests.
 */
public final class PageableUtils {

    private PageableUtils() {
    }

    /**
     * Clamps page number and page size to safe values while preserving sorting.
     *
     * @param pageable requested pagination
     * @param maxPageSize maximum accepted page size
     * @return sanitized pagination request
     */
    public static Pageable safe(Pageable pageable, int maxPageSize) {
        return PageRequest.of(
                Math.clamp(pageable.getPageNumber(), 0, Integer.MAX_VALUE),
                Math.clamp(pageable.getPageSize(), 1, maxPageSize),
                pageable.getSort()
        );
    }
}

