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
 * Resolves and enforces purchase ownership rules.
 */
@Service
public class PurchaseAccessService {

    private final UserService userService;

    public PurchaseAccessService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Resolves the user that owns a new purchase.
     *
     * Admins must provide a user id. Regular users can only create purchases for themselves.
     *
     * @param requestedUserId user id requested by the client
     * @return owner user for the purchase
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
     * Ensures the current user can access the purchase.
     *
     * Admins can access every purchase. Regular users can only access their own.
     *
     * @param purchase purchase to check
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
     * Returns the current authenticated user entity.
     *
     * @return current user
     */
    public User currentUser() {
        Authentication auth = SecurityUtils.requireAuthentication();
        return userService.getEntityByEmail(auth.getName());
    }
}
