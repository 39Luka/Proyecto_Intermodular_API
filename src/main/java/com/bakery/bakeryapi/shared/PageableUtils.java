package com.bakery.bakeryapi.shared;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Ayudantes para normalizar solicitudes de paginación.
 */
public final class PageableUtils {

    private PageableUtils() {
    }

    /**
     * Fija el número de página y tamaño de página a valores seguros mientras preserva el orden.
     *
     * @param pageable paginación solicitada
     * @param maxPageSize tamaño máximo de página aceptado
     * @return solicitud de paginación desinfectada
     */
    public static Pageable safe(Pageable pageable, int maxPageSize) {
        return PageRequest.of(
                Math.clamp(pageable.getPageNumber(), 0, Integer.MAX_VALUE),
                Math.clamp(pageable.getPageSize(), 1, maxPageSize),
                pageable.getSort()
        );
    }
}

