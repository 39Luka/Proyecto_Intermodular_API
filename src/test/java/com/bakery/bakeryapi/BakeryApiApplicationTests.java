package com.bakery.bakeryapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BakeryApiApplicationTests {

    /**
     * CP-SYS.01: contextLoads
     * Prueba de humo: Verifica que el contexto de Spring Boot se cargue correctamente con todas sus dependencias y configuraciones.
     */
    @Test
    void contextLoads() {
    }

}


