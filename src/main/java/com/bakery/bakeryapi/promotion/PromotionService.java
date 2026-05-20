package com.bakery.bakeryapi.promotion;

import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.product.ProductService;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.domain.PercentagePromotion;
import com.bakery.bakeryapi.domain.Promotion;
import com.bakery.bakeryapi.domain.PromotionUsage;
import com.bakery.bakeryapi.promotion.dto.PercentagePromotionRequest;
import com.bakery.bakeryapi.promotion.dto.PromotionResponse;
import com.bakery.bakeryapi.promotion.exception.InvalidPromotionException;
import com.bakery.bakeryapi.promotion.exception.PromotionNotFoundException;
import com.bakery.bakeryapi.shared.exception.ForbiddenOperationException;
import com.bakery.bakeryapi.shared.PageableUtils;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.repository.PromotionRepository;
import com.bakery.bakeryapi.repository.PromotionUsageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicio de aplicación para promociones.
 *
 * Valida ventanas de promoción, aplica descuentos y refuerza la regla de un uso por usuario
 * a través de registros de uso de promoción.
 */
@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository repository;
    private final ProductService productService;
    private final PromotionUsageRepository usageRepository;
    private final UserService userService;
    private final PaginationProperties paginationProperties;
    private final PromotionRules promotionRules;

    public PromotionService(
            PromotionRepository repository,
            ProductService productService,
            PromotionUsageRepository usageRepository,
            UserService userService,
            PaginationProperties paginationProperties,
            PromotionRules promotionRules
    ) {
        this.repository = repository;
        this.productService = productService;
        this.usageRepository = usageRepository;
        this.userService = userService;
        this.paginationProperties = paginationProperties;
        this.promotionRules = promotionRules;
    }

    @Transactional
    public PromotionResponse createPercentage(PercentagePromotionRequest request) {
        promotionRules.validateDates(request.getStartDate(), request.getEndDate());
        promotionRules.validatePercentage(request.getDiscountPercentage());

        Product product = productService.getActiveEntityById(request.getProductId());
        BigDecimal discountPercentage = promotionRules.normalizeAmount(request.getDiscountPercentage());

        PercentagePromotion promotion = new PercentagePromotion(
                request.getDescription(),
                discountPercentage,
                request.getStartDate(),
                request.getEndDate(),
                product
        );

        return PromotionResponse.from(repository.save(promotion));
    }

    public Promotion getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    public PromotionResponse getById(Long id) {
        return PromotionResponse.from(getEntityById(id));
    }

    public Page<PromotionResponse> getAll(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        return repository.findAll(safePageable)
                .map(PromotionResponse::from);
    }

    public Page<PromotionResponse> getActiveByProduct(Long productId, Long userId, Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        productService.getEntityById(productId);
        LocalDate today = LocalDate.now();

        Authentication auth = SecurityUtils.optionalAuthentication();
        if (auth == null) {
            if (userId != null) {
                // Los usuarios anónimos pueden listar promociones activas, pero no pueden solicitar filtrado por usuario.
                throw new InsufficientAuthenticationException("Autenticación requerida");
            }
        }
        Long effectiveUserId = auth == null ? null : resolveUserIdForPromotionFiltering(auth, userId);

        // Si effectiveUserId es null: devolver todas las promociones activas.
        // Si no: devolver solo promociones que el usuario aún no ha usado.
        Page<Promotion> promotions = effectiveUserId == null
                ? repository.findActiveByProductId(productId, today, safePageable)
                : repository.findActiveByProductIdAndUserId(productId, effectiveUserId, today, safePageable);
        return promotions.map(PromotionResponse::from);
    }

    public Page<PromotionResponse> getAvailablePromotions(Long userId, Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        LocalDate today = LocalDate.now();

        Authentication auth = SecurityUtils.optionalAuthentication();
        if (auth == null) {
            throw new InsufficientAuthenticationException("Autenticación requerida");
        }
        Long effectiveUserId = resolveUserIdForPromotionFiltering(auth, userId);

        return repository.findActiveAndUnusedByUser(effectiveUserId, today, safePageable)
                .map(PromotionResponse::from);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Promotion promotion = getEntityById(id);
        if (active) {
            promotion.enable();
        } else {
            promotion.disable();
        }
        repository.save(promotion);
    }

    public BigDecimal applyPromotion(Promotion promotion, Product product, int quantity, User user) {
        promotionRules.validateApplicable(promotion, product, quantity);
        BigDecimal discountAmount = promotion.calculateDiscountAmount(product.getPrice(), quantity);

        try {
            // La restricción única (promotion_id, user_id) garantiza "usar una sola vez".
            // saveAndFlush dispara la violación de restricción dentro de este método para que podamos devolver un error de dominio de tipo 400.
            usageRepository.saveAndFlush(new PromotionUsage(promotion, user, LocalDateTime.now()));
        } catch (DataIntegrityViolationException e) {
            throw new InvalidPromotionException("La promoción ya ha sido utilizada por este usuario");
        }

        return promotionRules.normalizeAmount(discountAmount);
    }

    public void releaseUsage(Promotion promotion, User user) {
        usageRepository.deleteByPromotionIdAndUserId(promotion.getId(), user.getId());
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
            throw new ForbiddenOperationException("No se pueden solicitar promociones para otro usuario");
        }
        return currentUser.getId();
    }

}
