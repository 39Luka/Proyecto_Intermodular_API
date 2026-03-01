package org.example.bakeryapi.purchase;

import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.common.pagination.PageableUtils;
import org.example.bakeryapi.common.security.SecurityUtils;
import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductService;
import org.example.bakeryapi.promotion.PromotionService;
import org.example.bakeryapi.promotion.domain.Promotion;
import org.example.bakeryapi.purchase.domain.Purchase;
import org.example.bakeryapi.purchase.domain.PurchaseItem;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.example.bakeryapi.purchase.dto.request.PurchaseItemRequest;
import org.example.bakeryapi.purchase.dto.request.PurchaseRequest;
import org.example.bakeryapi.purchase.dto.response.PurchaseResponse;
import org.example.bakeryapi.purchase.exception.InvalidPurchaseException;
import org.example.bakeryapi.purchase.exception.PurchaseNotFoundException;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Purchase application service.
 *
 * Key rules:
 * - Users can only create/access their own purchases.
 * - Admins can create/access purchases for any user.
 * - Stock and promotion usage are updated inside a single transaction so failures roll back cleanly.
 */
@Service
public class PurchaseService {

    private final PurchaseRepository repository;
    private final UserService userService;
    private final ProductService productService;
    private final PromotionService promotionService;

    public PurchaseService(
            PurchaseRepository repository,
            UserService userService,
            ProductService productService,
            PromotionService promotionService
    ) {
        this.repository = repository;
        this.userService = userService;
        this.productService = productService;
        this.promotionService = promotionService;
    }

    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidPurchaseException("Purchase must include at least one item");
        }

        User user = resolvePurchaseUser(request.userId());
        Purchase purchase = new Purchase(user, LocalDateTime.now(), PurchaseStatus.CREATED);

        for (PurchaseItemRequest itemRequest : request.items()) {
            if (itemRequest.quantity() <= 0) {
                throw new InvalidPurchaseException("Item quantity must be greater than zero");
            }

            Product product = productService.getActiveEntityById(itemRequest.productId());

            // Decrease stock first, then apply promotion. If anything fails afterwards, the transaction rolls back.
            product.decreaseStock(itemRequest.quantity());

            Promotion promotion = null;
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (itemRequest.promotionId() != null) {
                promotion = promotionService.getEntityById(itemRequest.promotionId());
                discountAmount = promotionService.applyPromotion(promotion, product, itemRequest.quantity(), user);
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = calculateSubtotal(unitPrice, itemRequest.quantity(), discountAmount);

            PurchaseItem item = new PurchaseItem(
                    product,
                    promotion,
                    itemRequest.quantity(),
                    unitPrice,
                    discountAmount,
                    subtotal
            );

            purchase.addItem(item);
        }

        return PurchaseResponse.from(repository.save(purchase));
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getById(Long id) {
        Purchase purchase = repository.findDetailedById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        enforceAccess(purchase);
        return PurchaseResponse.from(purchase);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getAll(Pageable pageable, Long userId) {
        Pageable safePageable = PageableUtils.safe(pageable);
        Authentication auth = SecurityUtils.requireAuthentication();

        if (SecurityUtils.isAdmin(auth)) {
            if (userId != null) {
                userService.getEntityById(userId);
                return repository.findAllDetailedByUserId(userId, safePageable)
                        .map(PurchaseResponse::from);
            }
            return repository.findAllDetailed(safePageable)
                    .map(PurchaseResponse::from);
        }

        User currentUser = userService.getEntityByEmail(auth.getName());
        return repository.findAllDetailedByUserId(currentUser.getId(), safePageable)
                .map(PurchaseResponse::from);
    }

    @Transactional
    public void cancel(Long id) {
        Purchase purchase = repository.findDetailedById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        enforceAccess(purchase);

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new InvalidPurchaseException("Purchase is already cancelled");
        }
        if (purchase.getStatus() != PurchaseStatus.CREATED) {
            throw new InvalidPurchaseException("Only pending purchases can be cancelled");
        }

        // Release promotion usage first, then restore stock. If something fails, the transaction rolls back.
        for (PurchaseItem item : purchase.getItems()) {
            if (item.getPromotion() != null) {
                promotionService.releaseUsage(item.getPromotion(), purchase.getUser());
            }
        }

        for (PurchaseItem item : purchase.getItems()) {
            item.getProduct().increaseStock(item.getQuantity());
        }

        purchase.cancel();
        repository.save(purchase);
    }

    @Transactional
    public void pay(Long id) {
        Purchase purchase = repository.findDetailedById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        enforceAccess(purchase);

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new InvalidPurchaseException("Cancelled purchases cannot be paid");
        }
        if (purchase.getStatus() == PurchaseStatus.PAID) {
            throw new InvalidPurchaseException("Purchase is already paid");
        }
        if (purchase.getStatus() != PurchaseStatus.CREATED) {
            throw new InvalidPurchaseException("Purchase cannot be marked as paid");
        }

        purchase.pay();
        repository.save(purchase);
    }

    private User resolvePurchaseUser(Long requestedUserId) {
        Authentication auth = SecurityUtils.requireAuthentication();
        if (SecurityUtils.isAdmin(auth)) {
            return userService.getEntityById(requestedUserId);
        }

        User currentUser = userService.getEntityByEmail(auth.getName());
        if (!currentUser.getId().equals(requestedUserId)) {
            throw new ForbiddenOperationException("Cannot create a purchase for another user");
        }
        return currentUser;
    }

    private BigDecimal calculateSubtotal(BigDecimal unitPrice, int quantity, BigDecimal discountAmount) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal subtotal = gross.subtract(discountAmount);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    private void enforceAccess(Purchase purchase) {
        Authentication auth = SecurityUtils.requireAuthentication();
        if (SecurityUtils.isAdmin(auth)) {
            return;
        }
        User currentUser = userService.getEntityByEmail(auth.getName());
        if (!purchase.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Cannot access purchases from another user");
        }
    }
}
