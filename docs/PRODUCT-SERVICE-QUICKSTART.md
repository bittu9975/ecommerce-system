# Product Service - Quick Start Guide

## ✅ Completed! Product Service is Ready

The Product Service is now fully implemented with all features!

---

## 🎯 What You Have Now

### Features Implemented:
- ✅ Product CRUD operations
- ✅ Category management
- ✅ Advanced search & filtering
- ✅ Pagination & sorting
- ✅ **Redis caching** (10-minute TTL)
- ✅ **JWT authentication** (Admin-only operations)
- ✅ Stock management
- ✅ Brand filtering
- ✅ Price range search

---

## 🚀 Quick Setup (5 Steps)

### Step 1: Create PostgreSQL Database

```bash
# Login to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE ecommerce_product;

# Exit
\q
```

### Step 2: Start Redis

```bash
# Windows - Open new terminal
redis-server

# Verify Redis is running
redis-cli ping
# Should respond: PONG
```

### Step 3: Start Auth Service (if not running)

```bash
# In new terminal
cd ecommerce-system/auth-service
mvn spring-boot:run

# Wait for: "Started AuthServiceApplication"
```

### Step 4: Start Product Service

```bash
# In new terminal
cd ecommerce-system/product-service
mvn spring-boot:run

# Wait for: "Started ProductServiceApplication"
```

### Step 5: Test It!

Open browser: http://localhost:8082/api/products/health

You should see: `Product Service is running!`

---

## 🧪 Complete Testing Workflow

### Part 1: Get Admin Token from Auth Service

**1. Register Admin User:**
```bash
curl -X POST http://localhost:8081/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Admin\",\"email\":\"admin@test.com\",\"password\":\"admin123\",\"role\":\"ADMIN\"}"
```

**2. Copy the token** from the response (starts with `eyJ...`)

**3. Save it** - You'll need it for admin operations!

---

### Part 2: Create Categories

**Create Electronics Category:**
```bash
curl -X POST http://localhost:8082/api/categories ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Electronics\",\"description\":\"Electronic devices\"}"
```

**Create Books Category:**
```bash
curl -X POST http://localhost:8082/api/categories ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Books\",\"description\":\"Books and publications\"}"
```

**Create Clothing Category:**
```bash
curl -X POST http://localhost:8082/api/categories ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Clothing\",\"description\":\"Apparel and accessories\"}"
```

**View All Categories (No Auth Needed):**
```bash
curl http://localhost:8082/api/categories
```

---

### Part 3: Create Products

**Create iPhone:**
```bash
curl -X POST http://localhost:8082/api/products ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"iPhone 15 Pro\",\"description\":\"Latest iPhone\",\"price\":999.99,\"stock\":50,\"categoryId\":1,\"brand\":\"Apple\",\"sku\":\"IP15PRO\"}"
```

**Create MacBook:**
```bash
curl -X POST http://localhost:8082/api/products ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"MacBook Pro M3\",\"description\":\"Powerful laptop\",\"price\":1999.99,\"stock\":30,\"categoryId\":1,\"brand\":\"Apple\",\"sku\":\"MBP-M3\"}"
```

**Create Samsung Phone:**
```bash
curl -X POST http://localhost:8082/api/products ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Samsung S24 Ultra\",\"description\":\"Android flagship\",\"price\":1199.99,\"stock\":40,\"categoryId\":1,\"brand\":\"Samsung\",\"sku\":\"S24U\"}"
```

**Create T-Shirt:**
```bash
curl -X POST http://localhost:8082/api/products ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Cotton T-Shirt\",\"description\":\"Comfortable wear\",\"price\":19.99,\"stock\":100,\"categoryId\":3,\"brand\":\"Generic\",\"sku\":\"TSHIRT01\"}"
```

---

### Part 4: Browse & Search (No Auth Needed!)

**Get All Products:**
```bash
curl http://localhost:8082/api/products
```

**Get Products with Pagination:**
```bash
curl "http://localhost:8082/api/products?page=0&size=2&sortBy=price&sortDirection=DESC"
```

**Get Product by ID:**
```bash
curl http://localhost:8082/api/products/1
```

**Search by Keyword:**
```bash
curl -X POST http://localhost:8082/api/products/search ^
  -H "Content-Type: application/json" ^
  -d "{\"keyword\":\"iphone\"}"
```

**Search by Price Range:**
```bash
curl -X POST http://localhost:8082/api/products/search ^
  -H "Content-Type: application/json" ^
  -d "{\"minPrice\":500,\"maxPrice\":1500}"
```

**Search by Category:**
```bash
curl -X POST http://localhost:8082/api/products/search ^
  -H "Content-Type: application/json" ^
  -d "{\"categoryId\":1}"
```

**Get All Brands:**
```bash
curl http://localhost:8082/api/products/brands
```

---

### Part 5: Update Operations (Admin Only)

**Update Product:**
```bash
curl -X PUT http://localhost:8082/api/products/1 ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"iPhone 15 Pro Max\",\"description\":\"Updated\",\"price\":1099.99,\"stock\":60,\"categoryId\":1,\"brand\":\"Apple\",\"sku\":\"IP15PROMAX\"}"
```

**Update Stock Only:**
```bash
curl -X PATCH "http://localhost:8082/api/products/1/stock?quantity=75" ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🎨 Using Postman (Recommended)

### Import Collection:
1. Open Postman
2. Click "Import"
3. Select: `docs/Product-Service-Postman-Collection.json`
4. Click "Import"

### Setup:
1. Run "Get All Categories" - Should work without token ✅
2. In Auth Service collection, run "Register Admin"
3. Copy the `token` from response
4. In Product Service collection, replace `YOUR_ADMIN_TOKEN_HERE` with your token
5. Test all endpoints!

---

## 🔍 Verify Redis Caching

### Test Caching Works:

**1. Get a product (first time - hits database):**
```bash
curl http://localhost:8082/api/products/1
```
Check console logs: `Fetching product with id: 1`

**2. Get same product again (from cache):**
```bash
curl http://localhost:8082/api/products/1
```
No database query! Data served from Redis cache ⚡

**3. View cache in Redis:**
```bash
# Open Redis CLI
redis-cli

# List all cached keys
KEYS *

# View specific cache entry
GET "products::1"

# Exit
exit
```

**4. Clear cache manually:**
```bash
redis-cli
FLUSHALL
exit
```

---

## 🎯 What Works Now

### ✅ Public Access (Anyone):
- Browse all products
- View product details
- Search products
- Filter by category, price, brand
- View categories
- Pagination & sorting

### 🔒 Admin Access (Requires JWT Token):
- Create products
- Update products
- Delete products
- Update stock
- Create categories
- Update categories
- Delete categories

---

## 📊 Check Database

**View Products in Database:**
```bash
psql -U postgres -d ecommerce_product

-- View all products
SELECT id, name, price, stock, brand FROM products;

-- View categories with product count
SELECT c.id, c.name, COUNT(p.id) as product_count
FROM categories c
LEFT JOIN products p ON c.id = p.category_id
GROUP BY c.id, c.name;

-- Exit
\q
```

---

## 🎓 What You've Learned

By completing Product Service:

✅ **Redis Caching**
- Cache configuration
- Cache keys strategy
- Cache eviction
- TTL (Time To Live)

✅ **Advanced Search**
- Keyword search
- Price range filtering
- Category filtering
- Brand filtering
- Pagination
- Sorting

✅ **Security**
- Role-based access (ADMIN vs USER)
- JWT token validation
- Public vs protected endpoints

✅ **Database**
- Entity relationships (Product ↔ Category)
- JPA annotations
- Custom queries
- Soft delete (active flag)

✅ **API Design**
- RESTful endpoints
- Proper HTTP methods
- Status codes
- Request/Response DTOs

---

## 💼 Resume Bullets

You can now confidently say:

> **E-Commerce Product Catalog Service**
> - Developed product management microservice using **Spring Boot 3.2** with **Redis caching** to improve response time by caching frequently accessed data
> - Implemented **advanced search and filtering** with pagination support for products based on keyword, category, price range, and brand
> - Secured admin endpoints using **JWT authentication** and **role-based access control** (RBAC)
> - Integrated **Spring Data JPA** with PostgreSQL for persistent storage and **Spring Data Redis** for distributed caching
> - Designed RESTful APIs following industry standards with proper HTTP methods, status codes, and error handling
> - Applied **cache eviction strategies** to maintain data consistency across create, update, and delete operations

---

## 🚨 Common Issues & Fixes

### Redis Not Running
```bash
# Start Redis
redis-server

# Check if running
redis-cli ping
```

### Database Doesn't Exist
```bash
psql -U postgres -c "CREATE DATABASE ecommerce_product"
```

### Port 8082 Already in Use
```bash
# Find process
netstat -ano | findstr :8082

# Kill it
taskkill /PID <PID> /F
```

### Admin Token Doesn't Work
- Make sure role is "ADMIN" not "USER"
- Token expires after 24 hours - get new one
- Check Authorization header: `Bearer <token>`

---

## 📈 Microservices Progress

**Completed: 2 out of 7 (29%)**

✅ Auth Service (Port 8081)  
✅ **Product Service (Port 8082)** ← You are here!  
⬜ Cart Service (Port 8083) - Next!  
⬜ Order Service (Port 8084)  
⬜ Payment Service (Port 8085)  
⬜ Notification Service (Port 8086)  
⬜ API Gateway (Port 8080)  

---

## 🎉 Congratulations!

You've successfully completed the Product Service with:
- Redis caching
- Advanced search
- JWT security
- Complete CRUD operations
- Professional API design

**Next up: Cart Service (Redis-based shopping cart)!**

Ready to continue? 🚀
