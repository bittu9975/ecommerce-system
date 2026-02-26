package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dto.ProductDTO;
import com.ecommerce.cartservice.exception.CartException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
public class ProductService {

    @Value("${product-service.url}")
    private String productServiceUrl;

    private final RestTemplate restTemplate;

    public ProductService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get product details from Product Service.
     * Forwards the JWT token from the current request so the product service
     * can authenticate the inter-service call.
     */
    public ProductDTO getProductById(Long productId) {
        log.info("Fetching product details for ID: {} from Product Service", productId);

        try {
            String url = productServiceUrl + "/" + productId;

            // Forward the JWT token from the incoming request
            HttpHeaders headers = new HttpHeaders();
            String token = extractTokenFromCurrentRequest();
            if (token != null) {
                headers.set("Authorization", "Bearer " + token);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ProductDTO.class
            );

            ProductDTO product = response.getBody();

            if (product == null) {
                throw new CartException("Product not found with ID: " + productId);
            }

            log.info("Successfully fetched product: {}", product.getName());
            return product;

        } catch (CartException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product from Product Service: {}", e.getMessage());
            throw new CartException("Unable to fetch product details. Product Service may be unavailable.");
        }
    }

    /**
     * Extracts the JWT token from the current HTTP request context.
     */
    private String extractTokenFromCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return null;

            HttpServletRequest request = attributes.getRequest();
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
            log.warn("Could not extract token from request context: {}", e.getMessage());
        }
        return null;
    }
}