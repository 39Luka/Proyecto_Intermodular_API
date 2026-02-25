package org.example.bakeryapi.common.pagination;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableUtilsTest {

    @Test
    void safe_capsPageSizeTo100() {
        PageRequest request = PageRequest.of(0, 1000);
        PageRequest safe = (PageRequest) PageableUtils.safe(request);
        assertEquals(100, safe.getPageSize());
    }

    @Test
    void safe_keepsValidSmallPageSize() {
        PageRequest request = PageRequest.of(0, 1);
        PageRequest safe = (PageRequest) PageableUtils.safe(request);
        assertEquals(1, safe.getPageSize());
    }
}
