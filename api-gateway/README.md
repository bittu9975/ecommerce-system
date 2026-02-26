# API Gateway

## Overview
**Single Entry Point** for all E-Commerce microservices using Spring Cloud Gateway.

## Features
- ✅ **Single entry point** (Port 8080)
- ✅ **Routing** to all 6 microservices
- ✅ Path-based routing
- ✅ CORS configuration
- ✅ Fallback handling
- ✅ Health checks
- ✅ Request/Response logging

## Architecture

```
                    Client
                      ↓
            ┌─────────────────┐
            │   API Gateway   │
            │   (Port 8080)   │
            └─────────┬───────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ↓             ↓             ↓
   ┌────────┐   ┌─────────┐   ┌──────┐
   │  Auth  │   │ Product │   │ Cart │
   │ (8081) │   │ (8082)  │   │(8083)│
   └────────┘   └─────────┘   └──────┘
        │             │             │
        ↓             ↓             ↓
   ┌────────┐   ┌─────────┐   ┌──────────┐
   │ Order  │   │ Payment │   │ Notify   │
   │ (8084) │   │ (8085)  │   │  (8086)  │
   └────────┘   └─────────┘   └──────────┘
```

## Routing Configuration

### All Routes Go Through Port 8080:

| Path | Routes To | Service Port |
|------|-----------|--------------|
| `/api/auth/**` | Auth Service | 8081 |
| `/api/products/**` | Product Service | 8082 |
| `/api/categories/**` | Product Service | 8082 |
| `/api/cart/**` | Cart Service | 8083 |
| `/api/orders/**` | Order Service | 8084 |
| `/api/payments/**` | Payment Service | 8085 |
| `/api/notifications/**` | Notification Service | 8086 |

## Prerequisites
- Java 21
- Maven
- **All 6 microservices running**

## Setup

### 1. Start All Services First

```bash
# Terminal 1: Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 2: Product Service
cd product-service && mvn spring-boot:run

# Terminal 3: Cart Service
cd cart-service && mvn spring-boot:run

# Terminal 4: Order Service
cd order-service && mvn spring-boot:run

# Terminal 5: Payment Service
cd payment-service && mvn spring-boot:run

# Terminal 6: Notification Service
cd notification-service && mvn spring-boot:run
```

### 2. Start API Gateway

```bash
# Terminal 7: API Gateway
cd api-gateway
mvn spring-boot:run
```

Gateway runs on **http://localhost:8080**

## Usage

### Before API Gateway:
```bash
# Had to remember different ports
curl http://localhost:8081/api/auth/login
curl http://localhost:8082/api/products
curl http://localhost:8083/api/cart
curl http://localhost:8084/api/orders
```

### With API Gateway:
```bash
# Everything through port 8080!
curl http://localhost:8080/api/auth/login
curl http://localhost:8080/api/products
curl http://localhost:8080/api/cart
curl http://localhost:8080/api/orders
```

## API Endpoints

### Gateway Info
```http
GET http://localhost:8080/
```

**Response:**
```json
{
  "service": "E-Commerce API Gateway",
  "version": "1.0.0",
  "status": "running",
  "message": "Welcome to E-Commerce Microservices API",
  "endpoints": {
    "auth": "http://localhost:8080/api/auth",
    "products": "http://localhost:8080/api/products",
    "categories": "http://localhost:8080/api/categories",
    "cart": "http://localhost:8080/api/cart",
    "orders": "http://localhost:8080/api/orders",
    "payments": "http://localhost:8080/api/payments",
    "notifications": "http://localhost:8080/api/notifications"
  }
}
```

### Health Check
```http
GET http://localhost:8080/health
```

### Gateway Actuator
```http
GET http://localhost:8080/actuator/gateway/routes
```

Shows all configured routes.

## Complete Workflow Through Gateway

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@test.com","password":"test123","role":"USER"}'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'
```

### 3. Browse Products
```bash
curl http://localhost:8080/api/products
```

### 4. Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
```

### 5. Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"123 Main St"}'
```

### 6. Check Payment
```bash
curl http://localhost:8080/api/payments/order/1
```

### 7. View Notifications
```bash
curl http://localhost:8080/api/notifications/user/test@test.com
```

**All through one port: 8080!** 🎯

## Benefits of API Gateway

### 1. **Single Entry Point**
- Clients only need to know one URL
- Simplifies client configuration
- Easy to remember

### 2. **Service Abstraction**
- Internal service ports hidden
- Can change service locations
- Microservices remain independent

### 3. **CORS Handling**
- Centralized CORS configuration
- No CORS issues for frontend

### 4. **Load Balancing** (future)
- Can add multiple instances
- Distribute traffic

### 5. **Security** (future)
- Centralized authentication
- Rate limiting
- Request validation

## Configuration

### Routes (application.yml):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/auth/**
```

### Add New Service:
```yaml
- id: new-service
  uri: http://localhost:8087
  predicates:
    - Path=/api/new/**
```

## Troubleshooting

### Service Not Found (404)
```bash
# Check if target service is running
curl http://localhost:8081/api/auth/health  # Direct

# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

### Connection Refused
- Make sure all services are running
- Check service ports match configuration
- Verify firewall/network settings

### Gateway Not Starting
```bash
# Check for port conflicts
netstat -ano | findstr :8080

# Check logs
mvn spring-boot:run
```

## Project Structure

```
api-gateway/
├── src/main/
│   ├── java/com/ecommerce/apigateway/
│   │   ├── ApiGatewayApplication.java
│   │   ├── config/
│   │   │   └── GatewayConfig.java
│   │   └── controller/
│   │       ├── FallbackController.java
│   │       └── GatewayController.java
│   └── resources/
│       └── application.yml
└── pom.xml
```

## Resume Points

- ✅ Implemented **API Gateway** using Spring Cloud Gateway
- ✅ **Single entry point** for microservices architecture
- ✅ Path-based routing to 6 microservices
- ✅ CORS configuration and fallback handling
- ✅ **Centralized request routing**

## Production Enhancements

For production, consider adding:
- [ ] Service discovery (Eureka/Consul)
- [ ] Circuit breakers (Resilience4j)
- [ ] Rate limiting
- [ ] Request logging
- [ ] Authentication at gateway level
- [ ] API versioning
- [ ] Response caching

---

**API Gateway Complete!** 🎉

**ENTIRE E-COMMERCE SYSTEM COMPLETE!** 🚀
