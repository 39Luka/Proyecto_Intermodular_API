package com.bakery.api.promotion;

import com.bakery.api.product.Product;
import com.bakery.api.product.ProductService;
import com.bakery.api.promotion.dto.PromotionMapper;
import com.bakery.api.promotion.PercentagePromotion;
import com.bakery.api.promotion.Promotion;
import com.bakery.api.promotion.PromotionUsage;
import com.bakery.api.promotion.dto.PercentagePromotionRequest;
import com.bakery.api.promotion.dto.PromotionResponse;
import com.bakery.api.promotion.exception.InvalidPromotionException;
import com.bakery.api.promotion.exception.PromotionNotFoundException;
import com.bakery.api.auth.exception.ForbiddenOperationException;
import com.bakery.api.common.pagination.PageableUtils;
import com.bakery.api.common.security.SecurityUtils;
import com.bakery.api.user.UserService;
import com.bakery.api.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository repository;
    private final ProductService productService;
    private final PromotionUsageRepository usageRepository;
    private final UserService userService;
    private final PromotionMapper mapper;

    public PromotionService(
            PromotionRepository repository,
            ProductService productService,
            PromotionUsageRepository usageRepository,
            UserService userService,
            PromotionMapper mapper
    ) {
        this.repository = repository;
        this.productService = productService;
        this.usageRepository = usageRepository;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional
    public PromotionResponse createPercentage(PercentagePromotionRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        validatePercentage(request.getDiscountPercentage());

        Product product = productService.getActiveEntityById(request.getProductId());
        BigDecimal discountPercentage = normalizePercentage(request.getDiscountPercentage());

        PercentagePromotion promotion = new PercentagePromotion(
                request.getDescription(),
                discountPercentage,
                request.getStartDate(),
                request.getEndDate(),
                product
        );

        return mapper.toResponse(repository.save(promotion));
    }

    public Promotion getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    public PromotionResponse getById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    public Page<PromotionResponse> getAll(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable);
        return repository.findAll(safePageable)
                .map(mapper::toResponse);
    }

    public Page<PromotionResponse> getActiveByProduct(Long productId, Long userId, Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable);
        productService.getEntityById(productId);
        LocalDate today = LocalDate.now();

        Authentication auth = SecurityUtils.requireAuthentication();
        Long effectiveUserId = resolveUserIdForPromotionFiltering(auth, userId);

        // If effectiveUserId is null: return all active promotions.
        // Otherwise: return only promotions the user has not used yet.
        Page<Promotion> promotions = effectiveUserId == null
                ? repository.findActiveByProductId(productId, today, safePageable)
                : repository.findActiveByProductIdAndUserId(productId, effectiveUserId, today, safePageable);
        return promotions.map(mapper::toResponse);
    }

    @Transactional
    public void disable(Long id) {
        Promotion promotion = getEntityById(id);
        promotion.disable();
        repository.save(promotion);
    }

    @Transactional
    public void enable(Long id) {
        Promotion promotion = getEntityById(id);
        promotion.enable();
        repository.save(promotion);
    }

    public BigDecimal applyPromotion(Promotion promotion, Product product, int quantity, User user) {
        if (!promotion.getProduct().getId().equals(product.getId())) {
            throw new InvalidPromotionException("Promotion does not apply to this product");
        }

        if (!promotion.isActiveOn(LocalDate.now())) {
            throw new InvalidPromotionException("Promotion is not active");
        }

        BigDecimal discountAmount = promotion.calculateDiscountAmount(product.getPrice(), quantity);
        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPromotionException("Promotion does not apply to the requested quantity");
        }

        try {
            // Unique constraint (promotion_id, user_id) guarantees "use once".
            // saveAndFlush triggers the constraint violation inside this method so we can return a 400-style domain error.
            usageRepository.saveAndFlush(new PromotionUsage(promotion, user, LocalDateTime.now()));
        } catch (DataIntegrityViolationException e) {
            throw new InvalidPromotionException("Promotion has already been used by this user");
        }

        return normalizeAmount(discountAmount);
    }

    public void releaseUsage(Promotion promotion, User user) {
        usageRepository.deleteByPromotionIdAndUserId(promotion.getId(), user.getId());
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidPromotionException("Promotion start date cannot be in the past");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidPromotionException("Promotion end date cannot be before start date");
        }
    }

    private void validatePercentage(BigDecimal discountPercentage) {
        if (discountPercentage == null) {
            throw new InvalidPromotionException("Percentage promotion requires discountPercentage");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidPromotionException("discountPercentage must be between 0 and 100");
        }
    }

    private BigDecimal normalizePercentage(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private Long resolveUserIdForPromotionFiltering(Authentication auth, Long requestedUserId) {
        if (SecurityUtils.isAdmin(auth)) {
            if (requestedUserId != null) {
                userService.getEntityById(requestedUserId);
            }
            return requestedUserId;
        }

        User currentUser = userService.getEntityByEmail(auth.getName());
        if (requestedUserId != null && !currentUser.getId().equals(requestedUserId)) {
            throw new ForbiddenOperationException("Cannot request promotions for another user");
        }
        return currentUser.getId();
    }

}
