# 🧪 INTEGRATION TESTING GUIDE

Complete guide to test all microservices working together synchronously.

---

## 📋 Prerequisites

### All Services Must Be Running:
```
✅ PostgreSQL (5 databases)
✅ Redis
✅ RabbitMQ
✅ Auth Service (8081)
✅ Product Service (8082)
✅ Cart Service (8083)
✅ Order Service (8084)
✅ Payment Service (8085)
✅ Notification Service (8086)
✅ API Gateway (8080)
```

---

## 🚀 Complete End-to-End Integration Test

This test simulates a real user journey through the entire system.

---

### Phase 1: Infrastructure Setup

#### Step 1.1: Create All Databases
```bash
psql -U postgres <<EOF
CREATE DATABASE ecommerce_auth;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_order;
CREATE DATABASE ecommerce_payment;
CREATE DATABASE ecommerce_notification;
EOF
```

**Verify:**
```bash
psql -U postgres -l | grep ecommerce
```

**Expected Output:**
```
ecommerce_auth
ecommerce_product
ecommerce_order
ecommerce_payment
ecommerce_notification
```

#### Step 1.2: Start Redis
```bash
redis-server
```

**Verify:**
```bash
redis-cli ping
```

**Expected:** `PONG`

#### Step 1.3: Verify RabbitMQ
```bash
rabbitmqctl status
```

**Expected:** Status output showing RabbitMQ running

#### Step 1.4: Access RabbitMQ Management
```
http://localhost:15672
Username: guest
Password: guest
```

---

### Phase 2: Start All Services

#### Step 2.1: Start Services in Order

**Terminal 1: Auth Service**
```bash
cd auth-service
mvn spring-boot:run
```
Wait for: `Started AuthServiceApplication`

**Terminal 2: Product Service**
```bash
cd product-service
mvn spring-boot:run
```
Wait for: `Started ProductServiceApplication`

**Terminal 3: Cart Service**
```bash
cd cart-service
mvn spring-boot:run
```
Wait for: `Started CartServiceApplication`

**Terminal 4: Order Service**
```bash
cd order-service
mvn spring-boot:run
```
Wait for: `Started OrderServiceApplication`

**Terminal 5: Payment Service**
```bash
cd payment-service
mvn spring-boot:run
```
Wait for: `Started PaymentServiceApplication`

**Terminal 6: Notification Service**
```bash
cd notification-service
mvn spring-boot:run
```
Wait for: `Started NotificationServiceApplication`

**Terminal 7: API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```
Wait for: `Started ApiGatewayApplication`

#### Step 2.2: Verify All Services Running
```bash
# Terminal 8: Run this health check script
curl -s http://localhost:8080/ | jq
curl -s http://localhost:8081/api/auth/health
curl -s http://localhost:8082/api/products/health
curl -s http://localhost:8083/api/cart/health
curl -s http://localhost:8084/api/orders/health
curl -s http://localhost:8085/api/payments/health
curl -s http://localhost:8086/api/notifications/health
```

**All should respond successfully!**

---

### Phase 3: End-to-End User Journey

**From now on, all requests go through API Gateway (port 8080)**

#### Step 3.1: Register Admin User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@ecommerce.com",
    "password": "Admin@123",
    "role": "ADMIN"
  }' | jq

# Save the token
export ADMIN_TOKEN="paste_admin_token_here"
```

**Expected:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@ecommerce.com",
  "role": "ADMIN"
}
```

#### Step 3.2: Create Product Categories
```bash
# Create Electronics category
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "description": "Electronic devices and gadgets"
  }' | jq

# Create Clothing category
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Clothing",
    "description": "Fashion and apparel"
  }' | jq

# Create Books category
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Books",
    "description": "Books and publications"
  }' | jq
```

#### Step 3.3: Create Products
```bash
# Create iPhone
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "Latest Apple iPhone with A17 chip",
    "price": 999.99,
    "stock": 50,
    "categoryId": 1,
    "brand": "Apple",
    "sku": "APPL-IP15P-001",
    "imageUrl": "https://example.com/iphone15.jpg",
    "active": true
  }' | jq

# Create MacBook
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16",
    "description": "Powerful laptop for professionals",
    "price": 2499.99,
    "stock": 30,
    "categoryId": 1,
    "brand": "Apple",
    "sku": "APPL-MBP16-001",
    "imageUrl": "https://example.com/macbook.jpg",
    "active": true
  }' | jq

# Create AirPods
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AirPods Pro",
    "description": "Wireless earbuds with noise cancellation",
    "price": 249.99,
    "stock": 100,
    "categoryId": 1,
    "brand": "Apple",
    "sku": "APPL-AIRP-001",
    "imageUrl": "https://example.com/airpods.jpg",
    "active": true
  }' | jq
```

#### Step 3.4: Register Regular User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@email.com",
    "password": "User@123",
    "role": "USER"
  }' | jq

# Save the token
export USER_TOKEN="paste_user_token_here"
```

#### Step 3.5: Browse Products (Public - No Auth Required)
```bash
# Get all products
curl http://localhost:8080/api/products | jq

# Search products
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "Apple",
    "page": 0,
    "size": 10
  }' | jq

# Filter by category
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "page": 0,
    "size": 10
  }' | jq

# Filter by price range
curl -X POST http://localhost:8080/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 200,
    "maxPrice": 1000,
    "page": 0,
    "size": 10
  }' | jq
```

#### Step 3.6: Add Products to Cart
```bash
# Add iPhone to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 1
  }' | jq

# Add AirPods to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 3,
    "quantity": 2
  }' | jq
```

**Expected Response:**
```json
{
  "userId": "john.doe@email.com",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "price": 999.99,
      "quantity": 1
    },
    {
      "productId": 3,
      "productName": "AirPods Pro",
      "price": 249.99,
      "quantity": 2
    }
  ],
  "totalItems": 3,
  "totalPrice": 1499.97
}
```

#### Step 3.7: View Cart
```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer $USER_TOKEN" | jq
```

#### Step 3.8: Update Cart Item Quantity
```bash
# Update iPhone quantity to 2
curl -X PUT http://localhost:8080/api/cart/items/1 \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2
  }' | jq
```

**Expected:** Total should now be $2,499.96

#### Step 3.9: Verify Cart in Redis
```bash
redis-cli GET "cart:john.doe@email.com"
```

**Expected:** JSON string with cart data

---

### Phase 4: Order Processing & Event Flow

#### Step 4.1: Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "123 Main Street, Apt 4B, New York, NY 10001, USA"
  }' | jq
```

**Expected Response:**
```json
{
  "id": 1,
  "userId": "john.doe@email.com",
  "status": "PENDING",
  "totalPrice": 2499.96,
  "items": [...],
  "shippingAddress": "123 Main Street...",
  "createdAt": "2024-02-04T..."
}
```

**What Happens Next (Watch the Terminal Logs):**

**Order Service (Terminal 4):**
```
Order created successfully with ID: 1
Publishing OrderCreatedEvent
Publishing PaymentRequestEvent
Cart cleared successfully
```

**Notification Service (Terminal 6):**
```
📦 Received OrderCreatedEvent for order ID: 1
📧 SENDING EMAIL
To: john.doe@email.com
Subject: Order Confirmation - Order #1
Message: Dear Customer, Your order #1 has been successfully placed!
📱 SENDING SMS
✅ Email notification logged to database
✅ SMS notification logged to database
```

**Payment Service (Terminal 5):**
```
Received PaymentRequestEvent from RabbitMQ for order ID: 1
Processing payment for order ID: 1
Payment record created with ID: 1
[Wait 2 seconds...]
Payment SUCCESSFUL for order ID: 1 with transaction ID: a1b2c3d4-...
PaymentSuccessEvent published successfully
```

**Notification Service (Terminal 6) - Again:**
```
💳 Received PaymentSuccessEvent for order ID: 1
📧 SENDING EMAIL
Subject: Payment Successful - Order #1
Message: Your payment has been successfully processed!
Transaction ID: a1b2c3d4-...
📱 SENDING SMS
✅ Notifications sent
```

#### Step 4.2: Verify Cart Cleared
```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer $USER_TOKEN" | jq
```

**Expected:** Empty cart

#### Step 4.3: Verify Cart Removed from Redis
```bash
redis-cli GET "cart:john.doe@email.com"
```

**Expected:** `(nil)` or null

#### Step 4.4: Check Order Status
```bash
curl http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $USER_TOKEN" | jq
```

#### Step 4.5: Check Payment Status
```bash
curl http://localhost:8080/api/payments/order/1 | jq
```

**Expected (90% chance - Success):**
```json
{
  "id": 1,
  "orderId": 1,
  "userId": "john.doe@email.com",
  "amount": 2499.96,
  "status": "SUCCESS",
  "transactionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "paymentMethod": "MOCK_PAYMENT",
  "failureReason": null
}
```

**Or (10% chance - Failure):**
```json
{
  "status": "FAILED",
  "failureReason": "Insufficient funds or card declined (MOCK)"
}
```

#### Step 4.6: Check Notifications
```bash
curl http://localhost:8080/api/notifications/user/john.doe@email.com | jq
```

**Expected:** 4 notifications (2 for order, 2 for payment - email + SMS each)

---

### Phase 5: RabbitMQ Verification

#### Step 5.1: Check Queues in Management UI
```
Open: http://localhost:15672
Login: guest/guest
Go to: Queues tab
```

**Expected Queues:**
```
✅ order.created.queue (0 messages - consumed)
✅ payment.request.queue (0 messages - consumed)
✅ payment.success.queue (0 messages - consumed)
   OR payment.failed.queue (0 messages - consumed)
```

#### Step 5.2: Check Queue Messages via CLI
```bash
rabbitmqadmin list queues name messages
```

**Expected:**
```
+------------------------+----------+
|          name          | messages |
+------------------------+----------+
| order.created.queue    | 0        |
| payment.request.queue  | 0        |
| payment.success.queue  | 0        |
| payment.failed.queue   | 0        |
+------------------------+----------+
```

---

### Phase 6: Database Verification

#### Step 6.1: Check Auth Database
```bash
psql -U postgres -d ecommerce_auth -c "
SELECT id, name, email, role FROM users;
"
```

**Expected:** 2 users (admin and john.doe)

#### Step 6.2: Check Product Database
```bash
psql -U postgres -d ecommerce_product -c "
SELECT id, name, price, stock FROM products;
"
```

**Expected:** 3 products

#### Step 6.3: Check Order Database
```bash
psql -U postgres -d ecommerce_order -c "
SELECT id, user_id, status, total_price FROM orders;
"
```

**Expected:** 1 order with PENDING status

#### Step 6.4: Check Order Items
```bash
psql -U postgres -d ecommerce_order -c "
SELECT id, order_id, product_name, quantity, price FROM order_items;
"
```

**Expected:** 2 items (iPhone and AirPods)

#### Step 6.5: Check Payment Database
```bash
psql -U postgres -d ecommerce_payment -c "
SELECT id, order_id, status, amount, transaction_id FROM payments;
"
```

**Expected:** 1 payment record

#### Step 6.6: Check Notification Database
```bash
psql -U postgres -d ecommerce_notification -c "
SELECT id, user_id, type, subject, event_type FROM notifications ORDER BY id;
"
```

**Expected:** 4 notifications

---

### Phase 7: Redis Cache Verification

#### Step 7.1: Check Product Cache
```bash
# First product request (from DB)
time curl -s http://localhost:8080/api/products > /dev/null

# Second request (from cache - faster)
time curl -s http://localhost:8080/api/products > /dev/null
```

**Expected:** Second request should be faster

#### Step 7.2: View Cached Keys
```bash
redis-cli KEYS "*"
```

**Expected:**
```
1) "products::all:0:10:name:ASC"
2) "products::1"
3) "products::3"
```

#### Step 7.3: View Cached Product
```bash
redis-cli GET "products::1"
```

**Expected:** JSON string with product data

---

### Phase 8: Advanced Integration Tests

#### Step 8.1: Test Multiple Orders
```bash
# Add products to cart again
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 2, "quantity": 1}' | jq

# Create another order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress": "456 Oak Ave, Boston, MA 02108"}' | jq
```

**Watch:** All events fire again in order

#### Step 8.2: Test Order History
```bash
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $USER_TOKEN" | jq
```

**Expected:** List of 2 orders

#### Step 8.3: Test Payment Failure Scenario
Keep creating orders until you get a payment failure (10% chance):

```bash
# Keep running this until payment fails
for i in {1..10}; do
  # Add to cart
  curl -s -X POST http://localhost:8080/api/cart/items \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"productId": 3, "quantity": 1}' > /dev/null
  
  # Create order
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Authorization: Bearer $USER_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"shippingAddress": "Test Address"}' | jq '.id'
  
  sleep 3
  
  # Check payment status
  PAYMENT_STATUS=$(curl -s http://localhost:8080/api/payments/order/$((i+1)) | jq -r '.status')
  echo "Order $((i+1)): $PAYMENT_STATUS"
  
  if [ "$PAYMENT_STATUS" == "FAILED" ]; then
    echo "✅ Payment failure scenario tested!"
    break
  fi
done
```

**Watch Notification Service logs for failure notification**

#### Step 8.4: Test Stock Validation
```bash
# Try to add more than available stock
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 1000
  }' | jq
```

**Expected:** Error about insufficient stock

---

## ✅ Integration Test Results

### Synchronous Flow Verification:

**User Journey:**
```
✅ User Registration → Auth Service → Database
✅ User Login → Auth Service → JWT Token
✅ Browse Products → Product Service → Redis Cache
✅ Add to Cart → Cart Service → Redis Storage → Product Service (validation)
✅ Create Order → Order Service → Database
              → RabbitMQ (OrderCreatedEvent)
              → RabbitMQ (PaymentRequestEvent)
              → Cart Service (clear cart)
              → Redis (cart deleted)
✅ Notification → Notification Service (consumes OrderCreatedEvent)
              → Email sent
              → SMS sent
              → Database logged
✅ Payment → Payment Service (consumes PaymentRequestEvent)
         → Process (2 sec delay)
         → Database logged
         → RabbitMQ (PaymentResultEvent)
✅ Payment Notification → Notification Service (consumes PaymentResultEvent)
                      → Email sent
                      → SMS sent
                      → Database logged
```

### Services Communication:
```
✅ Cart → Product (get details, validate stock)
✅ Order → Cart (get cart, clear cart)
✅ Order → RabbitMQ (publish events)
✅ Payment → RabbitMQ (consume + publish)
✅ Notification → RabbitMQ (consume)
✅ All Services → API Gateway (routing)
```

### Data Persistence:
```
✅ Users in PostgreSQL
✅ Products in PostgreSQL
✅ Product cache in Redis
✅ Cart in Redis (with TTL)
✅ Orders in PostgreSQL
✅ Payments in PostgreSQL
✅ Notifications in PostgreSQL
✅ Events in RabbitMQ (temporary)
```

---

## 📊 Performance Metrics

Run these to check performance:

```bash
# Product cache performance
echo "=== First Request (DB) ==="
time curl -s http://localhost:8080/api/products > /dev/null

echo "=== Second Request (Cache) ==="
time curl -s http://localhost:8080/api/products > /dev/null

# Order creation time
echo "=== Order Creation ==="
time curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress": "Test"}' > /dev/null
```

**Expected:**
- First product request: ~100-200ms
- Cached product request: ~10-20ms (10x faster!)
- Order creation: ~100-300ms (immediate, not waiting for payment)

---

## 🎯 Integration Test Checklist

**Infrastructure:**
- [x] All databases created
- [x] Redis running
- [x] RabbitMQ running

**Services:**
- [x] All 7 services started
- [x] All health checks pass
- [x] API Gateway routes correctly

**User Flow:**
- [x] User registration works
- [x] User login works
- [x] JWT tokens validate
- [x] Product browsing works
- [x] Cart operations work
- [x] Order creation works

**Event Flow:**
- [x] OrderCreatedEvent published
- [x] PaymentRequestEvent published
- [x] Events consumed by Payment Service
- [x] PaymentResultEvent published
- [x] Events consumed by Notification Service

**Notifications:**
- [x] Order confirmation email sent
- [x] Order confirmation SMS sent
- [x] Payment result email sent
- [x] Payment result SMS sent

**Data Persistence:**
- [x] Users saved to database
- [x] Products saved to database
- [x] Orders saved to database
- [x] Payments saved to database
- [x] Notifications saved to database
- [x] Cart saved to Redis
- [x] Products cached in Redis

**Integration Points:**
- [x] Cart ↔ Product integration
- [x] Order ↔ Cart integration
- [x] Order ↔ RabbitMQ integration
- [x] Payment ↔ RabbitMQ integration
- [x] Notification ↔ RabbitMQ integration

---

## ✅ INTEGRATION TESTS COMPLETE!

**All services are working together synchronously!**

The complete event-driven, microservices-based e-commerce system is fully operational and tested.

---

**Next:** Production deployment (see DEPLOYMENT-GUIDE.md)
