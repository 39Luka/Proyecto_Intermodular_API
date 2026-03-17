package com.bakery.api.purchase;

import com.bakery.api.auth.exception.ForbiddenOperationException;
import com.bakery.api.common.pagination.PageableUtils;
import com.bakery.api.common.security.SecurityUtils;
import com.bakery.api.product.Product;
import com.bakery.api.product.ProductService;
import com.bakery.api.promotion.PromotionService;
import com.bakery.api.promotion.domain.Promotion;
import com.bakery.api.purchase.domain.Purchase;
import com.bakery.api.purchase.domain.PurchaseItem;
import com.bakery.api.purchase.domain.PurchaseStatus;
import com.bakery.api.purchase.dto.PurchaseMapper;
import com.bakery.api.purchase.dto.request.PurchaseItemRequest;
import com.bakery.api.purchase.dto.request.PurchaseRequest;
import com.bakery.api.purchase.dto.response.PurchaseResponse;
import com.bakery.api.purchase.exception.InvalidPurchaseException;
import com.bakery.api.purchase.exception.PurchaseNotFoundException;
import com.bakery.api.user.UserService;
import com.bakery.api.user.domain.User;
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
    private final PurchaseMapper mapper;

    public PurchaseService(
            PurchaseRepository repository,
            UserService userService,
            ProductService productService,
            PromotionService promotionService,
            PurchaseMapper mapper
    ) {
        this.repository = repository;
        this.userService = userService;
        this.productService = productService;
        this.promotionService = promotionService;
        this.mapper = mapper;
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

        return mapper.toResponse(repository.save(purchase));
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getById(Long id) {
        Purchase purchase = repository.findDetailedById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        enforceAccess(purchase);
        return mapper.toResponse(purchase);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getAll(Pageable pageable, Long userId) {
        Pageable safePageable = PageableUtils.safe(pageable);
        Authentication auth = SecurityUtils.requireAuthentication();

        if (SecurityUtils.isAdmin(auth)) {
                if (userId != null) {
                    userService.getEntityById(userId);
                    return repository.findAllDetailedByUserId(userId, safePageable)
                        .map(mapper::toResponse);
                }
            return repository.findAllDetailed(safePageable)
                    .map(mapper::toResponse);
        }

        User currentUser = userService.getEntityByEmail(auth.getName());
        return repository.findAllDetailedByUserId(currentUser.getId(), safePageable)
                .map(mapper::toResponse);
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
