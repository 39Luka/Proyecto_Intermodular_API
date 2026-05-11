package com.bakery.bakeryapi.purchase;

import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.product.ProductService;
import com.bakery.bakeryapi.promotion.PromotionService;
import com.bakery.bakeryapi.shared.PageableUtils;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.domain.Purchase;
import com.bakery.bakeryapi.domain.PurchaseItem;
import com.bakery.bakeryapi.domain.PurchaseStatus;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.domain.Promotion;
import com.bakery.bakeryapi.purchase.dto.PurchaseItemRequest;
import com.bakery.bakeryapi.purchase.dto.PurchaseRequest;
import com.bakery.bakeryapi.purchase.dto.PurchaseResponse;
import com.bakery.bakeryapi.purchase.exception.InvalidPurchaseException;
import com.bakery.bakeryapi.purchase.exception.PurchaseNotFoundException;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.repository.PurchaseRepository;
import com.bakery.bakeryapi.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
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

    private static final Clock CLOCK_UTC = Clock.systemUTC();

    private final PurchaseRepository repository;
    private final UserService userService;
    private final ProductService productService;
    private final PromotionService promotionService;
    private final PurchaseAccessService purchaseAccessService;
    private final PurchasePricingService purchasePricingService;
    private final PaginationProperties paginationProperties;

    public PurchaseService(
            PurchaseRepository repository,
            UserService userService,
            ProductService productService,
            PromotionService promotionService,
            PurchaseAccessService purchaseAccessService,
            PurchasePricingService purchasePricingService,
            PaginationProperties paginationProperties
    ) {
        this.repository = repository;
        this.userService = userService;
        this.productService = productService;
        this.promotionService = promotionService;
        this.purchaseAccessService = purchaseAccessService;
        this.purchasePricingService = purchasePricingService;
        this.paginationProperties = paginationProperties;
    }

    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidPurchaseException("Purchase must include at least one item");
        }

        User user = purchaseAccessService.resolvePurchaseUser(request.userId());
        Purchase purchase = new Purchase(user, LocalDateTime.now(CLOCK_UTC), PurchaseStatus.CREATED);

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
            BigDecimal subtotal = purchasePricingService.calculateSubtotal(
                    unitPrice,
                    itemRequest.quantity(),
                    discountAmount
            );

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
        purchaseAccessService.enforceAccess(purchase);
        return PurchaseResponse.from(purchase);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getAll(Pageable pageable, Long userId) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
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

        User currentUser = purchaseAccessService.currentUser();
        return repository.findAllDetailedByUserId(currentUser.getId(), safePageable)
                .map(PurchaseResponse::from);
    }

    @Transactional
    public void cancel(Long id) {
        Purchase purchase = repository.findDetailedById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        purchaseAccessService.enforceAccess(purchase);

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
        purchaseAccessService.enforceAccess(purchase);

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

}
