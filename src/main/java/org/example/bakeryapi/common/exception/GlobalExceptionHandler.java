package org.example.bakeryapi.common.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 *
 * @RestControllerAdvice intercepta excepciones lanzadas en CUALQUIER @RestController
 * y las convierte en respuestas HTTP estructuradas (JSON con código y mensaje).
 *
 * Ventajas:
 * 1. Centraliza el manejo de errores (no repetir try-catch en cada controller)
 * 2. Respuestas consistentes en toda la API
 * 3. Oculta detalles internos al cliente (no se ven stack traces)
 *
 * Códigos HTTP principales:
 * - 400 Bad Request: datos inválidos del cliente
 * - 401 Unauthorized: no está autenticado (falta token o es inválido)
 * - 403 Forbidden: está autenticado pero no tiene permisos
 * - 404 Not Found: recurso no existe
 * - 409 Conflict: operación genera conflicto (ej: email duplicado)
 * - 500 Internal Server Error: error inesperado del servidor
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de negocio personalizadas (ApiException y sus subclases).
     * Ej: UserNotFoundException, EmailAlreadyExistsException, etc.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }

    /**
     * Maneja errores de JWT (token inválido, expirado, mal formado).
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid or expired token"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Maneja errores de autenticación de Spring Security.
     * Se lanza cuando no hay token o el token no es válido.
     * Retorna 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * Maneja errores de autorización de Spring Security.
     * Se lanza cuando el usuario está autenticado pero no tiene permisos para el recurso.
     * Ej: un USER intenta acceder a un endpoint de ADMIN.
     * Retorna 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden"
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /**
     * Maneja errores de validación de datos (Bean Validation).
     * Se lanza cuando un @Valid falla en un @RequestBody.
     * Ej: @NotBlank, @Email, @Min, @Max, etc.
     *
     * Retorna 400 Bad Request con la lista de campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Extrae todos los errores de validación en una lista
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of(
                        "field", err.getField(),              // Campo que falló (ej: "email")
                        "message", err.getDefaultMessage()    // Mensaje de error (ej: "must not be blank")
                ))
                .toList();

        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errors));
    }

    /**
     * Typically unique constraint violations or FK constraint problems triggered by concurrent requests.
     * Return 409 instead of a generic 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict"));
    }
}


