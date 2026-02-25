package org.example.bakeryapi.promotion;

import jakarta.validation.Valid;
import org.example.bakeryapi.promotion.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<PromotionResponse>> getActiveByProduct(
            @RequestParam Long productId,
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getActiveByProduct(productId, userId, pageable));
    }

    @PostMapping("/percentage")
    public ResponseEntity<PercentagePromotionResponse> createPercentage(@Valid @RequestBody PercentagePromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPercentage(request));
    }

    @PostMapping("/buy-x-pay-y")
    public ResponseEntity<BuyXPayYPromotionResponse> createBuyXPayY(@Valid @RequestBody BuyXPayYPromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createBuyXPayY(request));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        service.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        service.enable(id);
        return ResponseEntity.noContent().build();
    }
}
