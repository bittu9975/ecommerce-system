package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.cartservice.dto.ProductDTO;
import com.ecommerce.cartservice.exception.CartException;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductService productService;

    @Value("${cart.ttl}")
    private long cartTTL;  // Time to live in seconds

    @Value("${cart.max-items}")
    private int maxItems;

    private static final String CART_KEY_PREFIX = "cart:";

    /**
     * Get cart for a user
     */
    public CartResponse getCart(String userId) {
        log.info("Fetching cart for user: {}", userId);
        
        Cart cart = (Cart) redisTemplate.opsForValue().get(getCartKey(userId));
        
        if (cart == null) {
            log.info("No cart found for user: {}, creating empty cart", userId);
            cart = Cart.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        return convertToResponse(cart);
    }

    /**
     * Add item to cart
     */
    public CartResponse addToCart(String userId, Long productId, Integer quantity) {
        log.info("Adding product {} to cart for user: {} with quantity: {}", productId, userId, quantity);
        
        // Fetch product details from Product Service
        ProductDTO product = productService.getProductById(productId);
        
        if (!product.getActive()) {
            throw new CartException("Product is not available");
        }
        
        if (product.getStock() < quantity) {
            throw new CartException("Insufficient stock. Available: " + product.getStock());
        }
        
        // Get or create cart
        Cart cart = (Cart) redisTemplate.opsForValue().get(getCartKey(userId));
        
        if (cart == null) {
            cart = Cart.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        
        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            if (product.getStock() < newQuantity) {
                throw new CartException("Insufficient stock. Available: " + product.getStock());
            }
            
            item.setQuantity(newQuantity);
            log.info("Updated quantity for product {} to {}", productId, newQuantity);
        } else {
            // Add new item
            if (cart.getItems().size() >= maxItems) {
                throw new CartException("Cart is full. Maximum " + maxItems + " items allowed");
            }
            
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .imageUrl(product.getImageUrl())
                    .build();
            
            cart.getItems().add(newItem);
            log.info("Added new item to cart: {}", newItem);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        
        // Save to Redis with TTL
        saveCart(userId, cart);
        
        return convertToResponse(cart);
    }

    /**
     * Update cart item quantity
     */
    public CartResponse updateCartItem(String userId, Long productId, Integer quantity) {
        log.info("Updating cart item {} for user: {} to quantity: {}", productId, userId, quantity);
        
        Cart cart = (Cart) redisTemplate.opsForValue().get(getCartKey(userId));
        
        if (cart == null) {
            throw new CartException("Cart is empty");
        }
        
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartException("Product not found in cart"));
        
        // Validate stock
        ProductDTO product = productService.getProductById(productId);
        if (product.getStock() < quantity) {
            throw new CartException("Insufficient stock. Available: " + product.getStock());
        }
        
        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        
        saveCart(userId, cart);
        
        return convertToResponse(cart);
    }

    /**
     * Remove item from cart
     */
    public CartResponse removeFromCart(String userId, Long productId) {
        log.info("Removing product {} from cart for user: {}", productId, userId);
        
        Cart cart = (Cart) redisTemplate.opsForValue().get(getCartKey(userId));
        
        if (cart == null) {
            throw new CartException("Cart is empty");
        }
        
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        
        if (!removed) {
            throw new CartException("Product not found in cart");
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        
        saveCart(userId, cart);
        
        log.info("Removed product {} from cart", productId);
        return convertToResponse(cart);
    }

    /**
     * Clear entire cart
     */
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Boolean deleted = redisTemplate.delete(getCartKey(userId));
        
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Cart cleared successfully for user: {}", userId);
        } else {
            log.warn("No cart found to clear for user: {}", userId);
        }
    }

    /**
     * Helper method to save cart to Redis with TTL
     */
    private void saveCart(String userId, Cart cart) {
        String key = getCartKey(userId);
        redisTemplate.opsForValue().set(key, cart, cartTTL, TimeUnit.SECONDS);
        log.debug("Cart saved to Redis with TTL: {} seconds", cartTTL);
    }

    /**
     * Helper method to generate cart key
     */
    private String getCartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * Convert Cart model to CartResponse DTO
     */
    private CartResponse convertToResponse(Cart cart) {
        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(cart.getItems())
                .totalItems(cart.getTotalItems())
                .itemCount(cart.getItemCount())
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
