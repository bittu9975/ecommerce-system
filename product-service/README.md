# Product Service

## Overview
Product Catalog and Management microservice for the E-Commerce System. Handles product CRUD operations, categories, search, filtering, and Redis caching.

## Features
- ✅ Product CRUD Operations
- ✅ Category Management
- ✅ Product Search & Filtering
- ✅ Pagination & Sorting
- ✅ Redis Caching (10-minute TTL)
- ✅ Admin-only operations (JWT protected)
- ✅ Stock Management
- ✅ Brand Filtering
- ✅ Price Range Filtering

## Technology Stack
- Java 21
- Spring Boot 3.2.2
- Spring Security (JWT)
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- Redis (Caching)
- Lombok
- Maven

## Database Schema

### Categories Table
```sql
categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
)
```

### Products Table
```sql
products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    image_url VARCHAR(255),
    category_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    brand VARCHAR(255),
    sku VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
)
```

## Prerequisites
- Java 21
- Maven
- PostgreSQL (running on localhost:5432)
- Redis (running on localhost:6379)
- Auth Service running (for JWT tokens)

## Setup Instructions

### 1. Create Database
```bash
# Open PostgreSQL terminal
psql -U postgres

# Create database
CREATE DATABASE ecommerce_product;
```

### 2. Start Redis
```bash
# Windows
redis-server

# Verify Redis is running
redis-cli ping
# Should respond: PONG
```

### 3. Update Configuration (if needed)
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. Build and Run
```bash
cd product-service
mvn clean install
mvn spring-boot:run
```

Service will start on **http://localhost:8082**

## API Endpoints

### Health Check
```http
GET http://localhost:8082/api/products/health
```

---

### Category Endpoints

#### Get All Categories
```http
GET http://localhost:8082/api/categories
```

**Response:**
```json
[
    {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic items",
        "productCount": 5,
        "createdAt": "2024-02-02T10:30:00",
        "updatedAt": "2024-02-02T10:30:00"
    }
]
```

#### Get Category by ID
```http
GET http://localhost:8082/api/categories/1
```

#### Create Category (ADMIN only)
```http
POST http://localhost:8082/api/categories
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "name": "Electronics",
    "description": "Electronic devices and gadgets"
}
```

#### Update Category (ADMIN only)
```http
PUT http://localhost:8082/api/categories/1
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "name": "Electronics & Gadgets",
    "description": "Updated description"
}
```

#### Delete Category (ADMIN only)
```http
DELETE http://localhost:8082/api/categories/1
Authorization: Bearer <JWT_TOKEN>
```

---

### Product Endpoints

#### Get All Products (with Pagination)
```http
GET http://localhost:8082/api/products?page=0&size=10&sortBy=name&sortDirection=ASC
```

**Response:**
```json
{
    "content": [
        {
            "id": 1,
            "name": "iPhone 15 Pro",
            "description": "Latest iPhone model",
            "price": 999.99,
            "stock": 50,
            "imageUrl": "https://example.com/iphone.jpg",
            "categoryId": 1,
            "categoryName": "Electronics",
            "active": true,
            "brand": "Apple",
            "sku": "IPHONE15PRO",
            "createdAt": "2024-02-02T10:30:00",
            "updatedAt": "2024-02-02T10:30:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10
    },
    "totalPages": 1,
    "totalElements": 1
}
```

#### Get Product by ID
```http
GET http://localhost:8082/api/products/1
```

#### Search Products
```http
POST http://localhost:8082/api/products/search
Content-Type: application/json

{
    "keyword": "iphone",
    "categoryId": 1,
    "minPrice": 500,
    "maxPrice": 2000,
    "brand": "Apple",
    "page": 0,
    "size": 10,
    "sortBy": "price",
    "sortDirection": "ASC"
}
```

#### Get All Brands
```http
GET http://localhost:8082/api/products/brands
```

**Response:**
```json
["Apple", "Samsung", "Sony", "LG"]
```

#### Create Product (ADMIN only)
```http
POST http://localhost:8082/api/products
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone with A17 Pro chip",
    "price": 999.99,
    "stock": 50,
    "imageUrl": "https://example.com/iphone.jpg",
    "categoryId": 1,
    "active": true,
    "brand": "Apple",
    "sku": "IPHONE15PRO"
}
```

#### Update Product (ADMIN only)
```http
PUT http://localhost:8082/api/products/1
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "name": "iPhone 15 Pro Max",
    "description": "Updated description",
    "price": 1099.99,
    "stock": 100,
    "imageUrl": "https://example.com/iphone-new.jpg",
    "categoryId": 1,
    "active": true,
    "brand": "Apple",
    "sku": "IPHONE15PROMAX"
}
```

#### Update Stock (ADMIN only)
```http
PATCH http://localhost:8082/api/products/1/stock?quantity=75
Authorization: Bearer <JWT_TOKEN>
```

#### Delete Product (ADMIN only)
```http
DELETE http://localhost:8082/api/products/1
Authorization: Bearer <JWT_TOKEN>
```

---

## Redis Caching

The service uses Redis for caching with the following strategy:

### Cache Keys:
- **Categories**: `categories::all` and `categories::{id}`
- **Products**: `products::all:{page}:{size}:{sortBy}:{sortDirection}` and `products::{id}`

### Cache Eviction:
- Cache is automatically cleared when:
  - Creating a new product/category
  - Updating a product/category
  - Deleting a product/category
- Cache TTL: 10 minutes (600,000 ms)

### Verify Caching:
```bash
# Connect to Redis CLI
redis-cli

# List all keys
KEYS *

# Get a specific cache entry
GET "products::1"

# Clear all cache
FLUSHALL
```

---

## Security & Authorization

### Public Endpoints (No Auth Required):
- ✅ GET `/api/products/**` - Browse products
- ✅ GET `/api/categories/**` - View categories
- ✅ POST `/api/products/search` - Search products

### Admin-Only Endpoints (Requires ADMIN role):
- 🔒 POST `/api/products` - Create product
- 🔒 PUT `/api/products/{id}` - Update product
- 🔒 PATCH `/api/products/{id}/stock` - Update stock
- 🔒 DELETE `/api/products/{id}` - Delete product
- 🔒 POST `/api/categories` - Create category
- 🔒 PUT `/api/categories/{id}` - Update category
- 🔒 DELETE `/api/categories/{id}` - Delete category

### Getting Admin Token:
```bash
# 1. Register admin user in Auth Service
POST http://localhost:8081/api/auth/register
{
    "name": "Admin User",
    "email": "admin@example.com",
    "password": "admin123",
    "role": "ADMIN"
}

# 2. Copy the token from response
# 3. Use it in Product Service requests
Authorization: Bearer <token>
```

---

## Testing Workflow

### Step 1: Start Services
```bash
# Terminal 1: Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 2: Product Service
cd product-service
mvn spring-boot:run
```

### Step 2: Get Admin Token
```bash
# Register admin
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@test.com","password":"admin123","role":"ADMIN"}'
```

### Step 3: Create Category
```bash
curl -X POST http://localhost:8082/api/categories \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic items"}'
```

### Step 4: Create Product
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","description":"Latest iPhone","price":999.99,"stock":50,"categoryId":1,"brand":"Apple"}'
```

### Step 5: Browse Products (No Auth)
```bash
curl http://localhost:8082/api/products
```

---

## Project Structure
```
product-service/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/productservice/
│   │   │   ├── config/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── CategoryController.java
│   │   │   │   └── ProductController.java
│   │   │   ├── dto/
│   │   │   │   ├── CategoryDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   └── ProductSearchRequest.java
│   │   │   ├── entity/
│   │   │   │   ├── Category.java
│   │   │   │   └── Product.java
│   │   │   ├── exception/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   └── ProductRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CategoryService.java
│   │   │   │   └── ProductService.java
│   │   │   ├── util/
│   │   │   │   └── JwtUtil.java
│   │   │   └── ProductServiceApplication.java
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

### Database Connection Issues
```bash
# Verify database exists
psql -U postgres -l | findstr ecommerce_product

# If not exists, create it
psql -U postgres -c "CREATE DATABASE ecommerce_product"
```

### Cache Not Working
```bash
# Check Redis logs in application
# Look for: "Connected to Redis"

# Verify cache entries
redis-cli
KEYS *
```

### JWT Authentication Issues
- Make sure Auth Service is running on port 8081
- Use ADMIN role token for protected endpoints
- Check token hasn't expired (24-hour validity)

---

## Resume Points

- ✅ Implemented product catalog microservice with **Spring Boot 3.2**
- ✅ Integrated **Redis caching** for improved performance (10-minute TTL)
- ✅ Built **search and filtering** with pagination support
- ✅ Secured admin endpoints using **JWT authentication**
- ✅ Implemented **role-based access control** (USER vs ADMIN)
- ✅ Used **Spring Data JPA** for PostgreSQL integration
- ✅ Applied **cache eviction strategies** for data consistency

---

## Next Steps
- Integrate with Cart Service
- Add image upload functionality
- Implement product reviews
- Add inventory management
- Create product recommendations

