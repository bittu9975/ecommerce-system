# Cart Service

## Overview
Shopping Cart Management microservice for the E-Commerce System. Uses **Redis as primary database** for fast, ephemeral cart storage with automatic expiration.

## Features
- ✅ Add items to cart
- ✅ Update item quantity
- ✅ Remove items from cart
- ✅ View cart
- ✅ Clear cart
- ✅ **Redis as primary storage** (fast, in-memory)
- ✅ Cart expiration (7-day TTL)
- ✅ User-specific carts (JWT-based)
- ✅ Integration with Product Service
- ✅ Stock validation
- ✅ Cart limits (max 50 items)
- ✅ Real-time price calculation

## Technology Stack
- Java 21
- Spring Boot 3.2.2
- Spring Security (JWT)
- **Redis (Primary Database)**
- Spring Data Redis
- RestTemplate (Service Integration)
- Lombok
- Maven

## Architecture

```
User (JWT) → Cart Service → Redis (Cart Storage)
                  ↓
           Product Service (Get product details)
```

**Key Design:**
- **No PostgreSQL** - Redis is the only database
- **Ephemeral storage** - Carts expire after 7 days
- **Fast operations** - In-memory data access
- **Session-based** - User-specific carts via JWT

## Prerequisites
- Java 21
- Maven
- **Redis** (running on localhost:6379) - **CRITICAL**
- Product Service (running on localhost:8082)
- Auth Service (running on localhost:8081) - for JWT tokens

## Setup Instructions

### 1. Start Redis (REQUIRED)
```bash
# Windows
redis-server

# Verify Redis is running
redis-cli ping
# Should respond: PONG
```

### 2. Ensure Other Services are Running
```bash
# Auth Service (Terminal 1)
cd auth-service
mvn spring-boot:run

# Product Service (Terminal 2)
cd product-service
mvn spring-boot:run
```

### 3. Build and Run Cart Service
```bash
cd cart-service
mvn clean install
mvn spring-boot:run
```

Service will start on **http://localhost:8083**

## Redis Storage Strategy

### Cart Data Structure:
```
Key: cart:{userId}  (e.g., cart:john@example.com)
Value: Cart object (JSON serialized)
TTL: 604800 seconds (7 days)
```

### Data Model:
```json
{
  "userId": "john@example.com",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "price": 999.99,
      "quantity": 2,
      "imageUrl": "https://...",
      "subtotal": 1999.98
    }
  ],
  "totalItems": 2,
  "itemCount": 1,
  "totalPrice": 1999.98,
  "createdAt": "2024-02-03T10:00:00",
  "updatedAt": "2024-02-03T10:05:00"
}
```

## API Endpoints

### Health Check
```http
GET http://localhost:8083/api/cart/health
```

---

### Get Cart
```http
GET http://localhost:8083/api/cart
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "userId": "john@example.com",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "price": 999.99,
      "quantity": 2,
      "imageUrl": "https://example.com/iphone.jpg",
      "subtotal": 1999.98
    }
  ],
  "totalItems": 2,
  "itemCount": 1,
  "totalPrice": 1999.98,
  "createdAt": "2024-02-03T10:00:00",
  "updatedAt": "2024-02-03T10:05:00"
}
```

---

### Add Item to Cart
```http
POST http://localhost:8083/api/cart/items
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

**What Happens:**
1. Validates JWT token → Gets user ID
2. Calls Product Service → Gets product details & stock
3. Validates stock availability
4. Adds/Updates item in cart
5. Saves to Redis with 7-day TTL
6. Returns updated cart

---

### Update Cart Item Quantity
```http
PUT http://localhost:8083/api/cart/items/1
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "quantity": 5
}
```

---

### Remove Item from Cart
```http
DELETE http://localhost:8083/api/cart/items/1
Authorization: Bearer <JWT_TOKEN>
```

---

### Clear Entire Cart
```http
DELETE http://localhost:8083/api/cart
Authorization: Bearer <JWT_TOKEN>
```

---

## Complete Testing Workflow

### Step 1: Get JWT Token
```bash
# Login as user
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'

# Copy the token from response
```

### Step 2: Create Products (if not exist)
```bash
# Get admin token first
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@test.com","password":"admin123","role":"ADMIN"}'

# Create category
curl -X POST http://localhost:8082/api/categories \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic devices"}'

# Create product
curl -X POST http://localhost:8082/api/products \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","description":"Latest iPhone","price":999.99,"stock":50,"categoryId":1,"brand":"Apple"}'
```

### Step 3: Use Cart Service
```bash
# Add product to cart
curl -X POST http://localhost:8083/api/cart/items \
  -H "Authorization: Bearer USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# View cart
curl http://localhost:8083/api/cart \
  -H "Authorization: Bearer USER_TOKEN"

# Update quantity
curl -X PUT http://localhost:8083/api/cart/items/1 \
  -H "Authorization: Bearer USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity":3}'

# Remove item
curl -X DELETE http://localhost:8083/api/cart/items/1 \
  -H "Authorization: Bearer USER_TOKEN"

# Clear cart
curl -X DELETE http://localhost:8083/api/cart \
  -H "Authorization: Bearer USER_TOKEN"
```

---

## Verify Redis Storage

### View Cart Data in Redis:
```bash
# Connect to Redis CLI
redis-cli

# List all cart keys
KEYS cart:*

# View specific cart
GET "cart:john@example.com"

# Check TTL (time to live)
TTL "cart:john@example.com"

# Delete cart manually
DEL "cart:john@example.com"

# Clear all carts
FLUSHDB

# Exit
exit
```

---

## Business Logic

### Add to Cart Flow:
1. **Authenticate** - Validate JWT token, extract user ID
2. **Fetch Product** - Call Product Service to get details
3. **Validate** - Check if product is active & in stock
4. **Update Cart**:
   - If product exists in cart → Update quantity
   - If new product → Add to cart
   - If cart full (50 items) → Reject
5. **Save to Redis** - With 7-day TTL
6. **Return** - Updated cart with totals

### Stock Validation:
- Checks current stock before adding
- Validates stock when updating quantity
- Throws error if insufficient stock

### Cart Limits:
- Max 50 different items per cart
- No limit on quantity per item (stock permitting)
- Automatic expiration after 7 days of inactivity

---

## Configuration

### Cart Settings (application.yml):
```yaml
cart:
  ttl: 604800        # 7 days in seconds
  max-items: 50      # Maximum items per cart

product-service:
  url: http://localhost:8082/api/products
```

### Adjust Settings:
- **TTL**: Change `cart.ttl` value (in seconds)
- **Max Items**: Change `cart.max-items` value
- **Product Service URL**: Update if running on different port

---

## Error Handling

### Common Errors:

**1. Product not found:**
```json
{
  "timestamp": "2024-02-03T10:00:00",
  "status": 400,
  "error": "Cart Error",
  "message": "Product not found with ID: 999"
}
```

**2. Insufficient stock:**
```json
{
  "message": "Insufficient stock. Available: 5"
}
```

**3. Cart full:**
```json
{
  "message": "Cart is full. Maximum 50 items allowed"
}
```

**4. Product Service unavailable:**
```json
{
  "message": "Unable to fetch product details. Product Service may be unavailable."
}
```

**5. Unauthorized:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

---

## Project Structure
```
cart-service/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/cartservice/
│   │   │   ├── config/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   └── CartController.java
│   │   │   ├── dto/
│   │   │   │   ├── AddToCartRequest.java
│   │   │   │   ├── CartResponse.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   └── UpdateCartItemRequest.java
│   │   │   ├── exception/
│   │   │   │   ├── CartException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   │   ├── Cart.java
│   │   │   │   └── CartItem.java
│   │   │   ├── service/
│   │   │   │   ├── CartService.java
│   │   │   │   └── ProductService.java
│   │   │   ├── util/
│   │   │   │   └── JwtUtil.java
│   │   │   └── CartServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## Troubleshooting

### Redis Connection Failed
```bash
# Check if Redis is running
redis-cli ping

# If not running, start Redis
redis-server

# Check Redis port
netstat -ano | findstr :6379
```

### Product Service Not Reachable
```bash
# Verify Product Service is running
curl http://localhost:8082/api/products/health

# Check application.yml has correct URL
product-service:
  url: http://localhost:8082/api/products
```

### JWT Token Invalid
- Token expires after 24 hours
- Get new token from Auth Service
- Check Authorization header format: `Bearer <token>`

### Cart Not Persisting
- Check Redis is running
- Verify TTL hasn't expired (7 days default)
- Check Redis CLI: `GET "cart:user@email.com"`

---

## Resume Points

- ✅ Implemented shopping cart microservice using **Redis as primary database**
- ✅ Designed ephemeral storage with **automatic expiration (TTL)**
- ✅ Integrated with Product Service using **RestTemplate**
- ✅ Implemented **JWT-based user authentication**
- ✅ Applied **stock validation** and **cart limits**
- ✅ Used **Spring Data Redis** for in-memory data operations
- ✅ Designed **stateless, scalable** cart architecture

---

## Next Steps
- Integrate with Order Service
- Add cart session sharing
- Implement cart recovery
- Add analytics (abandoned carts)
- Implement cart merging (guest → logged-in)

---

## Notes

**Why Redis for Cart?**
- ⚡ **Fast** - In-memory operations (sub-millisecond)
- 🔄 **Ephemeral** - Carts don't need permanent storage
- 📊 **Scalable** - Easy horizontal scaling
- ⏰ **TTL** - Automatic cleanup of old carts
- 💰 **Cost-effective** - No need for disk I/O

**Production Considerations:**
- Add Redis persistence (RDB/AOF) for backup
- Implement cart backup to PostgreSQL
- Add Redis Cluster for high availability
- Monitor cart abandonment rates
- Implement cart recovery features

---

**Cart Service Complete! 🛒**
