package org.example.bakeryapi.promotion;

import org.example.bakeryapi.product.Product;
import org.example.bakeryapi.product.ProductService;
import org.example.bakeryapi.promotion.domain.BuyXPayYPromotion;
import org.example.bakeryapi.promotion.domain.PercentagePromotion;
import org.example.bakeryapi.promotion.domain.Promotion;
import org.example.bakeryapi.promotion.domain.PromotionUsage;
import org.example.bakeryapi.promotion.dto.*;
import org.example.bakeryapi.promotion.exception.InvalidPromotionException;
import org.example.bakeryapi.promotion.exception.PromotionNotFoundException;
import org.example.bakeryapi.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
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

    public PromotionService(
            PromotionRepository repository,
            ProductService productService,
            PromotionUsageRepository usageRepository
    ) {
        this.repository = repository;
        this.productService = productService;
        this.usageRepository = usageRepository;
    }

    @Transactional
    public PercentagePromotionResponse createPercentage(PercentagePromotionRequest request) {
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

        return PercentagePromotionResponse.from(repository.save(promotion));
    }

    @Transactional
    public BuyXPayYPromotionResponse createBuyXPayY(BuyXPayYPromotionRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        validateBuyXPayY(request.getBuyQuantity(), request.getPayQuantity());

        Product product = productService.getActiveEntityById(request.getProductId());

        BuyXPayYPromotion promotion = new BuyXPayYPromotion(
                request.getDescription(),
                request.getBuyQuantity(),
                request.getPayQuantity(),
                request.getStartDate(),
                request.getEndDate(),
                product
        );

        return BuyXPayYPromotionResponse.from(repository.save(promotion));
    }

    public Promotion getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    public PromotionResponse getById(Long id) {
        return PromotionResponse.from(getEntityById(id));
    }

    public Page<PromotionResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(PromotionResponse::from);
    }

    public Page<PromotionResponse> getActiveByProduct(Long productId, Long userId, Pageable pageable) {
        productService.getEntityById(productId);
        LocalDate today = LocalDate.now();
        // Si userId es null: todas las promociones activas. Si no: solo las que el usuario no ha usado
        Page<Promotion> promotions = userId == null
                ? repository.findActiveByProductId(productId, today, pageable)
                : repository.findActiveByProductIdAndUserId(productId, userId, today, pageable);
        return promotions.map(PromotionResponse::from);
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

    private void validateBuyXPayY(Integer buyQuantity, Integer payQuantity) {
        if (buyQuantity == null || payQuantity == null) {
            throw new InvalidPromotionException("BUY_X_PAY_Y promotion requires buyQuantity and payQuantity");
        }
        if (buyQuantity <= 0 || payQuantity <= 0) {
            throw new InvalidPromotionException("buyQuantity and payQuantity must be greater than zero");
        }
        if (buyQuantity <= payQuantity) {
            throw new InvalidPromotionException("buyQuantity must be greater than payQuantity");
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

}
