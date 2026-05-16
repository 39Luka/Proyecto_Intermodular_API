package com.bakery.bakeryapi.purchase;

import com.bakery.bakeryapi.domain.Purchase;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.purchase.exception.InvalidPurchaseException;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.shared.exception.ForbiddenOperationException;
import com.bakery.bakeryapi.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Resuelve y aplica reglas de propiedad de compra.
 */
@Service
public class PurchaseAccessService {

    private final UserService userService;

    public PurchaseAccessService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Resuelve el usuario que posee una nueva compra.
     *
     * Los administradores deben proporcionar una identificación de usuario. Los usuarios normales solo pueden crear compras para sí mismos.
     *
     * @param requestedUserId identificación de usuario solicitada por el cliente
     * @return usuario propietario de la compra
     */
    public User resolvePurchaseUser(Long requestedUserId) {
        Authentication auth = SecurityUtils.requireAuthentication();
        if (SecurityUtils.isAdmin(auth)) {
            if (requestedUserId == null) {
                throw new InvalidPurchaseException("userId is required for admins");
            }
            return userService.getEntityById(requestedUserId);
        }

        User currentUser = userService.getEntityByEmail(auth.getName());
        if (requestedUserId == null) {
            return currentUser;
        }
        if (!currentUser.getId().equals(requestedUserId)) {
            throw new ForbiddenOperationException("Cannot create a purchase for another user");
        }
        return currentUser;
    }

    /**
     * Asegura que el usuario actual pueda acceder a la compra.
     *
     * Los administradores pueden acceder a cualquier compra. Los usuarios normales solo pueden acceder a las suyas.
     *
     * @param purchase compra a verificar
     */
    public void enforceAccess(Purchase purchase) {
        Authentication auth = SecurityUtils.requireAuthentication();
        if (SecurityUtils.isAdmin(auth)) {
            return;
        }
        User currentUser = userService.getEntityByEmail(auth.getName());
        if (!purchase.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Cannot access purchases from another user");
        }
    }

    /**
     * Devuelve la entidad del usuario autenticado actual.
     *
     * @return usuario actual
     */
    public User currentUser() {
        Authentication auth = SecurityUtils.requireAuthentication();
        return userService.getEntityByEmail(auth.getName());
    }
}
