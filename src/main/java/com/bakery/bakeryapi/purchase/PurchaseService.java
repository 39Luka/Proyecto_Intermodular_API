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
 * Servicio de aplicación de compra.
 *
 * Reglas clave:
 * - Los usuarios solo pueden crear/acceder a sus propias compras.
 * - Los administradores pueden crear/acceder a compras para cualquier usuario.
 * - El stock y el uso de promoción se actualizan dentro de una única transacción para que los fallos se reviertan limpiamente.
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

            // Disminuir stock primero, luego aplicar promoción. Si algo falla después, la transacción se revierte.
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
    public Page<PurchaseResponse> getAll(Pageable pageable, Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        Authentication auth = SecurityUtils.requireAuthentication();

        // Si no hay filtro de fechas, usar los métodos existentes
        boolean hasDateFilter = startDate != null || endDate != null;
        
        if (SecurityUtils.isAdmin(auth)) {
            if (userId != null) {
                userService.getEntityById(userId);
            }
            
            if (!hasDateFilter) {
                if (userId != null) {
                    return repository.findAllDetailedByUserId(userId, safePageable)
                            .map(PurchaseResponse::from);
                }
                return repository.findAllDetailed(safePageable)
                        .map(PurchaseResponse::from);
            }

            // Con filtro de fechas
            LocalDateTime from = startDate != null ? startDate : LocalDateTime.of(1900, 1, 1, 0, 0, 0);
            LocalDateTime to = endDate != null ? endDate : LocalDateTime.now(CLOCK_UTC).plusYears(100);

            if (userId != null) {
                return repository.findAllDetailedByUserIdBetweenDates(userId, from, to, safePageable)
                        .map(PurchaseResponse::from);
            }
            return repository.findAllDetailedBetweenDates(from, to, safePageable)
                    .map(PurchaseResponse::from);
        }

        // No-admin users: solo ven sus propias compras
        User currentUser = purchaseAccessService.currentUser();
        
        if (!hasDateFilter) {
            return repository.findAllDetailedByUserId(currentUser.getId(), safePageable)
                    .map(PurchaseResponse::from);
        }

        LocalDateTime from = startDate != null ? startDate : LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        LocalDateTime to = endDate != null ? endDate : LocalDateTime.now(CLOCK_UTC).plusYears(100);

        return repository.findAllDetailedByUserIdBetweenDates(currentUser.getId(), from, to, safePageable)
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

        // Liberar uso de promoción primero, luego restaurar stock. Si algo falla, la transacción se revierte.
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
