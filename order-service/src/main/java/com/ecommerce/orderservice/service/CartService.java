package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CartResponse;
import com.ecommerce.orderservice.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CartService {

    @Value("${service.cart-url}")
    private String cartServiceUrl;

    private final RestTemplate restTemplate;

    public CartService() {
        this.restTemplate = new RestTemplate();
    }

    public CartResponse getCart(String token) {
        log.info("Fetching cart from Cart Service");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CartResponse> response = restTemplate.exchange(
                    cartServiceUrl,
                    HttpMethod.GET,
                    entity,
                    CartResponse.class
            );
            
            CartResponse cart = response.getBody();
            
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new OrderException("Cart is empty");
            }
            
            log.info("Successfully fetched cart with {} items", cart.getItems().size());
            return cart;
            
        } catch (Exception e) {
            log.error("Error fetching cart: {}", e.getMessage());
            throw new OrderException("Unable to fetch cart. Cart Service may be unavailable.");
        }
    }

    public void clearCart(String token) {
        log.info("Clearing cart in Cart Service");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                    cartServiceUrl,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            
            log.info("Cart cleared successfully");
            
        } catch (Exception e) {
            log.warn("Failed to clear cart: {}", e.getMessage());
            // Don't throw exception here - order is already created
        }
    }
}
