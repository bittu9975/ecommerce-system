package com.ecommerce.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class GatewayController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "E-Commerce API Gateway");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("message", "Welcome to E-Commerce Microservices API");
        
        Map<String, String> services = new HashMap<>();
        services.put("auth", "http://localhost:8080/api/auth");
        services.put("products", "http://localhost:8080/api/products");
        services.put("categories", "http://localhost:8080/api/categories");
        services.put("cart", "http://localhost:8080/api/cart");
        services.put("orders", "http://localhost:8080/api/orders");
        services.put("payments", "http://localhost:8080/api/payments");
        services.put("notifications", "http://localhost:8080/api/notifications");
        
        response.put("endpoints", services);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "API Gateway");
        
        return ResponseEntity.ok(response);
    }
}
