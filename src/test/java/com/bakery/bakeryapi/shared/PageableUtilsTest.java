package com.bakery.bakeryapi.shared;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableUtilsTest {

    /**
     * CP-SHR.01: safe_capsPageSizeTo100
     * Verifica que la utilidad de paginación limite cualquier solicitud de tamaño de página superior a 100 por seguridad.
     */
    @Test
    void safe_capsPageSizeTo100() {
        PageRequest request = PageRequest.of(0, 1000);
        PageRequest safe = (PageRequest) PageableUtils.safe(request, 100);
        assertEquals(100, safe.getPageSize());
    }

    /**
     * CP-SHR.02: safe_keepsValidSmallPageSize
     * Valida que los tamaños de página pequeños y válidos se mantengan sin cambios.
     */
    @Test
    void safe_keepsValidSmallPageSize() {
        PageRequest request = PageRequest.of(0, 1);
        PageRequest safe = (PageRequest) PageableUtils.safe(request, 100);
        assertEquals(1, safe.getPageSize());
    }
}

