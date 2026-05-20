package com.bakery.bakeryapi.user;

import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.auth.exception.InvalidCredentialsException;
import com.bakery.bakeryapi.user.dto.UserRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import com.bakery.bakeryapi.user.exception.EmailAlreadyExistsException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
import com.bakery.bakeryapi.repository.UserRepository;
import com.bakery.bakeryapi.shared.ImageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * Servicio de aplicación para gestión de usuarios, actualizaciones de imágenes de perfil y cambios de contraseña.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getEntityById(Long id) {
        log.debug("Obteniendo entidad de usuario por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", id);
                    return new UserNotFoundException(id);
                });
    }

    public User getEntityByEmail(String email) {
        log.debug("Obteniendo entidad de usuario por correo electrónico: {}", email);
        return repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", email);
                    return new UserNotFoundException(email);
                });
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(getEntityById(id));
    }

    public UserResponse getByEmail(String email) {
        return UserResponse.from(getEntityByEmail(email));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        log.info("Creando nuevo usuario con el correo: {}", request.email());
        User user = createInternal(request.email(), request.password(), request.role());
        return UserResponse.from(user);
    }

    @Transactional
    public User createInternal(String email, String password, Role role) {
        if (repository.existsByEmail(email)) {
            log.warn("Fallo en la creación del usuario: el correo ya existe: {}", email);
            throw new EmailAlreadyExistsException(email);
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, role);
        User saved = repository.save(user);
        log.info("Usuario creado con éxito con el correo: {}, rol: {}", email, role);
        return saved;
    }

    @Transactional
    public User rotateRefreshToken(User user) {
        user.rotateRefreshToken();
        return repository.save(user);
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        log.info("Estableciendo el estado habilitado del usuario {} a: {}", id, enabled);
        User user = getEntityById(id);
        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }
        repository.save(user);
        log.info("Estado habilitado del usuario {} actualizado", id);
    }

    /**
     * Actualiza la imagen de perfil para el usuario identificado por el correo electrónico.
     *
     * Un contenido de imagen en blanco o {@code null} elimina la imagen de perfil existente. Los valores
     * no vacíos deben ser imágenes Base64 válidas aceptadas por {@link ImageValidator}.
     *
     * @param email correo del usuario a actualizar
     * @param profileImageBase64 imagen codificada como Base64, o {@code null}/en blanco para eliminarla
     * @return respuesta de usuario actualizada
     * @throws UserNotFoundException cuando no existe ningún usuario para el correo proporcionado
     * @throws com.bakery.bakeryapi.shared.exception.InvalidImageException cuando la imagen es inválida
     */
    @Transactional
    public UserResponse updateProfileImage(String email, String profileImageBase64) {
        log.info("Actualizando imagen de perfil para el usuario: {}", email);
        User user = getEntityByEmail(email);
        user.setProfileImage(decodeOptionalImage(profileImageBase64));
        User saved = repository.save(user);
        log.info("Imagen de perfil actualizada para el usuario: {}", email);
        return UserResponse.from(saved);
    }

    /**
     * Cambia la contraseña para el usuario identificado por el correo electrónico.
     *
     * La contraseña actual debe coincidir con la contraseña almacenada. Después de cambiarla, la versión del
     * token de refresco se rota para que los tokens de refresco anteriores dejen de ser válidos.
     *
     * @param email correo del usuario que cambia su contraseña
     * @param currentPassword contraseña actual sin procesar
     * @param newPassword nueva contraseña sin procesar para codificar y persistir
     * @throws UserNotFoundException cuando no existe ningún usuario para el correo proporcionado
     * @throws InvalidCredentialsException cuando la contraseña actual no coincide
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        log.info("Cambiando contraseña para el usuario: {}", email);
        User user = getEntityByEmail(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Fallo en el cambio de contraseña: contraseña actual inválida para el correo: {}", email);
            throw new InvalidCredentialsException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.rotateRefreshToken();
        repository.save(user);
        log.info("Contraseña cambiada para el usuario: {}", email);
    }

    /**
     * Convierte una imagen Base64 opcional en bytes después de la validación.
     *
     * @param imageBase64 imagen codificada como Base64, o {@code null}/en blanco
     * @return bytes de la imagen decodificados, o {@code null} cuando la entrada está vacía
     */
    private byte[] decodeOptionalImage(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }
        ImageValidator.validateImageBase64(imageBase64);
        return Base64.getDecoder().decode(imageBase64);
    }
}



