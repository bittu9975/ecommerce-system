package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.AddToCartRequest;
import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.cartservice.dto.UpdateCartItemRequest;
import com.ecommerce.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cart Service is running!");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String userId = getCurrentUserId();
        log.info("GET /api/cart - Fetching cart for user: {}", userId);
        
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String userId = getCurrentUserId();
        log.info("POST /api/cart/items - Adding product {} to cart for user: {}", 
                request.getProductId(), userId);
        
        CartResponse cart = cartService.addToCart(userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String userId = getCurrentUserId();
        log.info("PUT /api/cart/items/{} - Updating quantity to {} for user: {}", 
                productId, request.getQuantity(), userId);
        
        CartResponse cart = cartService.updateCartItem(userId, productId, request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long productId) {
        String userId = getCurrentUserId();
        log.info("DELETE /api/cart/items/{} - Removing from cart for user: {}", productId, userId);
        
        CartResponse cart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        String userId = getCurrentUserId();
        log.info("DELETE /api/cart - Clearing cart for user: {}", userId);
        
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to get current user ID from JWT
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();  // This is the email from JWT
    }
}
