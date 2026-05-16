package com.bakery.bakeryapi.repository;

import com.bakery.bakeryapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso de persistencia para cuentas de usuario.
 */
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}


