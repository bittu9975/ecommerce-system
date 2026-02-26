# 🔄 COMPLETE SERVICE FLOW DOCUMENTATION

Detailed flow diagrams and explanations for each service and the overall system.

---

## 📋 Table of Contents

1. [Auth Service Flow](#1-auth-service-flow)
2. [Product Service Flow](#2-product-service-flow)
3. [Cart Service Flow](#3-cart-service-flow)
4. [Order Service Flow](#4-order-service-flow)
5. [Payment Service Flow](#5-payment-service-flow)
6. [Notification Service Flow](#6-notification-service-flow)
7. [API Gateway Flow](#7-api-gateway-flow)
8. [Complete System Flow](#8-complete-system-flow)

---

## 1. Auth Service Flow

### Purpose:
User authentication and JWT token generation

### Port: 8081

### Database: ecommerce_auth (PostgreSQL)

### Architecture:
```
User Request
     ↓
Controller (AuthController)
     ↓
Service Layer (AuthService)
     ↓
Password Encryption (BCryptPasswordEncoder)
     ↓
Repository (UserRepository)
     ↓
PostgreSQL Database
     ↓
JWT Token Generation (JwtUtil)
     ↓
Response to User
```

### Detailed Flow:

#### 1.1 User Registration Flow

**Request:**
```
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@email.com",
  "password": "password123",
  "role": "USER"
}
```

**Flow:**
```
Step 1: Request → AuthController.register()
   ↓
Step 2: Controller → AuthService.register(RegisterRequest)
   ↓
Step 3: Check if email already exists
   UserRepository.existsByEmail(email)
   ↓
   If exists → throw "Email already registered"
   If not exists → Continue
   ↓
Step 4: Encrypt password
   BCryptPasswordEncoder.encode(password)
   ↓
Step 5: Create User entity
   User user = new User()
   user.setName(name)
   user.setEmail(email)
   user.setPassword(encryptedPassword)
   user.setRole(role)
   ↓
Step 6: Save to database
   UserRepository.save(user)
   ↓
Step 7: Generate JWT token
   JwtUtil.generateToken(email, role)
   ↓
Step 8: Return response
   AuthResponse { token, email, role }
```

**Database Impact:**
```sql
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('John Doe', 'john@email.com', '$2a$10$...', 'USER', NOW(), NOW());
```

#### 1.2 User Login Flow

**Request:**
```
POST /api/auth/login
{
  "email": "john@email.com",
  "password": "password123"
}
```

**Flow:**
```
Step 1: Request → AuthController.login()
   ↓
Step 2: Controller → AuthService.login(LoginRequest)
   ↓
Step 3: Find user by email
   UserRepository.findByEmail(email)
   ↓
   If not found → throw "Invalid credentials"
   If found → Continue
   ↓
Step 4: Verify password
   BCryptPasswordEncoder.matches(password, user.getPassword())
   ↓
   If doesn't match → throw "Invalid credentials"
   If matches → Continue
   ↓
Step 5: Generate JWT token
   JwtUtil.generateToken(email, role)
   ↓
Step 6: Return response
   AuthResponse { token, email, role }
```

**JWT Token Structure:**
```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: { 
  "sub": "john@email.com",
  "role": "USER",
  "iat": 1707000000,
  "exp": 1707086400
}
Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### Key Components:

**Entities:**
- `User`: id, name, email, password, role, createdAt, updatedAt

**DTOs:**
- `RegisterRequest`: name, email, password, role
- `LoginRequest`: email, password
- `AuthResponse`: token, email, role

**Security:**
- Password encryption with BCrypt
- JWT token generation with HS256
- Token expiration: 24 hours

---

## 2. Product Service Flow

### Purpose:
Product catalog management with caching

### Port: 8082

### Databases: 
- PostgreSQL: ecommerce_product
- Redis: Caching layer

### Architecture:
```
User Request (with JWT)
     ↓
JWT Filter (validate token)
     ↓
Security Check (role-based)
     ↓
Controller (ProductController / CategoryController)
     ↓
Service Layer (ProductService / CategoryService)
     ↓
Cache Check (Redis) → If found, return
     ↓ (if not in cache)
Repository (ProductRepository / CategoryRepository)
     ↓
PostgreSQL Database
     ↓
Cache Update (Redis)
     ↓
Response to User
```

### Detailed Flow:

#### 2.1 Create Product Flow (ADMIN only)

**Request:**
```
POST /api/products
Authorization: Bearer <ADMIN_TOKEN>
{
  "name": "iPhone 15 Pro",
  "price": 999.99,
  "stock": 50,
  "categoryId": 1,
  "brand": "Apple"
}
```

**Flow:**
```
Step 1: Request → JwtAuthenticationFilter
   Extract token from header
   Validate token with JwtUtil
   Extract user email and role
   Set SecurityContext
   ↓
Step 2: Security Check
   @PreAuthorize("hasRole('ADMIN')")
   If not ADMIN → 403 Forbidden
   If ADMIN → Continue
   ↓
Step 3: Request → ProductController.createProduct()
   @Valid ProductDTO validated
   ↓
Step 4: Controller → ProductService.createProduct()
   ↓
Step 5: Verify category exists
   CategoryRepository.findById(categoryId)
   If not found → throw exception
   ↓
Step 6: Create Product entity
   Product product = new Product()
   Set all fields
   ↓
Step 7: Save to database
   ProductRepository.save(product)
   ↓
Step 8: Evict cache
   @CacheEvict clears all product caches
   ↓
Step 9: Convert to DTO and return
   ProductDTO response
```

**Database Impact:**
```sql
-- First, verify category exists
SELECT * FROM categories WHERE id = 1;

-- Insert product
INSERT INTO products (name, description, price, stock, image_url, category_id, active, brand, sku, created_at, updated_at)
VALUES ('iPhone 15 Pro', 'Latest iPhone', 999.99, 50, 'url', 1, true, 'Apple', 'SKU001', NOW(), NOW());
```

**Cache Impact:**
```
DELETE all keys matching "products::*"
```

#### 2.2 Get Products Flow (PUBLIC - no auth)

**Request:**
```
GET /api/products?page=0&size=10&sortBy=name&sortDirection=ASC
```

**Flow:**
```
Step 1: Request → ProductController.getAllProducts()
   No JWT required (public endpoint)
   ↓
Step 2: Controller → ProductService.getAllProducts()
   ↓
Step 3: Check Redis cache
   @Cacheable("products::all:0:10:name:ASC")
   ↓
   If found in cache:
      Return from Redis (fast!)
   ↓
   If NOT in cache:
      ↓
      Step 4: Query database
         ProductRepository.findAll(Pageable)
         ↓
      Step 5: Convert to DTOs
         Loop through products
         Convert each to ProductDTO
         ↓
      Step 6: Store in Redis
         Cache key: "products::all:0:10:name:ASC"
         TTL: 10 minutes
         ↓
      Step 7: Return response
```

**Redis Cache Structure:**
```
Key: "products::all:0:10:name:ASC"
Value: {
  "content": [
    { "id": 1, "name": "iPhone 15 Pro", ... },
    { "id": 2, "name": "MacBook Pro", ... }
  ],
  "totalElements": 10,
  "totalPages": 1
}
TTL: 600 seconds (10 minutes)
```

#### 2.3 Search Products Flow

**Request:**
```
POST /api/products/search
{
  "keyword": "iPhone",
  "categoryId": 1,
  "minPrice": 500,
  "maxPrice": 1500,
  "page": 0,
  "size": 10
}
```

**Flow:**
```
Step 1: ProductController.searchProducts()
   ↓
Step 2: ProductService.searchProducts()
   ↓
Step 3: Build dynamic query
   Specification<Product> spec = null
   ↓
   If keyword exists:
      Add: name LIKE '%keyword%' OR description LIKE '%keyword%'
   ↓
   If categoryId exists:
      Add: category_id = categoryId
   ↓
   If price range exists:
      Add: price BETWEEN minPrice AND maxPrice
   ↓
   If brand exists:
      Add: brand = brand
   ↓
Step 4: Execute query
   ProductRepository.findAll(spec, Pageable)
   ↓
Step 5: Return paginated results
```

**Generated SQL Example:**
```sql
SELECT * FROM products 
WHERE (name LIKE '%iPhone%' OR description LIKE '%iPhone%')
  AND category_id = 1
  AND price BETWEEN 500 AND 1500
  AND active = true
ORDER BY name ASC
LIMIT 10 OFFSET 0;
```

### Key Components:

**Entities:**
- `Product`: id, name, description, price, stock, imageUrl, category, brand, sku, active
- `Category`: id, name, description, products

**Caching Strategy:**
- Cache key pattern: `products::all:{page}:{size}:{sortBy}:{sortDirection}`
- Individual product: `products::{id}`
- TTL: 10 minutes
- Eviction: On create, update, delete

---

## 3. Cart Service Flow

### Purpose:
Shopping cart management with Redis storage

### Port: 8083

### Database: Redis (Primary storage)

### Architecture:
```
User Request (with JWT)
     ↓
JWT Filter (validate token)
     ↓
Controller (CartController)
     ↓
Service Layer (CartService)
     ↓
Product Service Integration (get product details)
     ↓
Redis Storage (cart data)
     ↓
Response to User
```

### Detailed Flow:

#### 3.1 Add to Cart Flow

**Request:**
```
POST /api/cart/items
Authorization: Bearer <USER_TOKEN>
{
  "productId": 1,
  "quantity": 2
}
```

**Flow:**
```
Step 1: JWT validation
   Extract user email from token
   userId = "john@email.com"
   ↓
Step 2: CartController.addToCart()
   ↓
Step 3: CartService.addToCart(userId, productId, quantity)
   ↓
Step 4: Fetch product details from Product Service
   ProductService.getProductById(productId)
   HTTP GET: http://localhost:8082/api/products/1
   ↓
   Receive: ProductDTO { id, name, price, stock, ... }
   ↓
Step 5: Validate product
   If not active → throw "Product is not available"
   If stock < quantity → throw "Insufficient stock"
   ↓
Step 6: Get existing cart from Redis
   RedisTemplate.opsForValue().get("cart:john@email.com")
   ↓
   If cart exists → Load cart
   If not → Create new cart
   ↓
Step 7: Check if product already in cart
   Loop through cart.items
   ↓
   If product exists:
      Update quantity: item.quantity += quantity
      Validate new quantity against stock
   ↓
   If product NOT in cart:
      Create new CartItem
      Add to cart.items
   ↓
Step 8: Update cart metadata
   cart.setUpdatedAt(LocalDateTime.now())
   ↓
Step 9: Save to Redis
   RedisTemplate.opsForValue().set(
      "cart:john@email.com",
      cart,
      7 days TTL
   )
   ↓
Step 10: Calculate totals
   totalItems = sum of all quantities
   totalPrice = sum of all subtotals
   ↓
Step 11: Convert to DTO and return
   CartResponse
```

**Redis Storage:**
```
Key: "cart:john@email.com"
Value: {
  "userId": "john@email.com",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro",
      "price": 999.99,
      "quantity": 2,
      "imageUrl": "..."
    }
  ],
  "createdAt": "2024-02-04T10:00:00",
  "updatedAt": "2024-02-04T10:05:00"
}
TTL: 604800 seconds (7 days)
```

#### 3.2 View Cart Flow

**Request:**
```
GET /api/cart
Authorization: Bearer <USER_TOKEN>
```

**Flow:**
```
Step 1: Extract userId from JWT
   ↓
Step 2: Get cart from Redis
   RedisTemplate.opsForValue().get("cart:john@email.com")
   ↓
   If cart exists:
      Return cart data
   ↓
   If cart NOT exists:
      Create empty cart
      Return empty cart
```

### Key Components:

**Models:**
- `Cart`: userId, items[], createdAt, updatedAt
- `CartItem`: productId, productName, price, quantity, imageUrl

**Redis Operations:**
- GET: Retrieve cart
- SET: Save/Update cart (with TTL)
- DELETE: Clear cart

**Integration:**
- RestTemplate calls to Product Service for validation

---

## 4. Order Service Flow

### Purpose:
Order processing and event publishing

### Port: 8084

### Databases:
- PostgreSQL: ecommerce_order
- RabbitMQ: Event publishing

### Architecture:
```
User Request (with JWT)
     ↓
Controller (OrderController)
     ↓
Service Layer (OrderService)
     ↓
Cart Service Integration (get cart)
     ↓
Create Order (PostgreSQL)
     ↓
Publish Events (RabbitMQ)
     ├─→ OrderCreatedEvent
     └─→ PaymentRequestEvent
     ↓
Clear Cart (Cart Service)
     ↓
Response to User
```

### Detailed Flow:

#### 4.1 Create Order Flow

**Request:**
```
POST /api/orders
Authorization: Bearer <USER_TOKEN>
{
  "shippingAddress": "123 Main St, City, Country"
}
```

**Complete Flow:**
```
Step 1: JWT validation
   userId = "john@email.com"
   ↓
Step 2: OrderController.createOrder()
   ↓
Step 3: OrderService.createOrder(userId, token, request)
   ↓
Step 4: Get cart from Cart Service
   CartService.getCart(token)
   HTTP GET: http://localhost:8083/api/cart
   Headers: Authorization: Bearer <token>
   ↓
   Receive: CartResponse with items
   ↓
Step 5: Validate cart
   If cart is empty → throw "Cannot create order from empty cart"
   ↓
Step 6: Create Order entity
   Order order = new Order()
   order.setUserId(userId)
   order.setStatus(PENDING)
   order.setTotalPrice(cart.getTotalPrice())
   order.setShippingAddress(request.getShippingAddress())
   ↓
Step 7: Create Order Items
   For each CartItem in cart:
      OrderItem orderItem = new OrderItem()
      orderItem.setProductId(cartItem.getProductId())
      orderItem.setProductName(cartItem.getProductName())
      orderItem.setPrice(cartItem.getPrice())
      orderItem.setQuantity(cartItem.getQuantity())
      order.addItem(orderItem)
   ↓
Step 8: Save to database
   OrderRepository.save(order)
   ↓
   Database transaction:
      INSERT INTO orders (...)
      INSERT INTO order_items (...) -- Multiple inserts
   ↓
Step 9: Publish OrderCreatedEvent to RabbitMQ
   OrderCreatedEvent event = OrderCreatedEvent.builder()
      .orderId(order.getId())
      .userId(userId)
      .totalPrice(order.getTotalPrice())
      .shippingAddress(order.getShippingAddress())
      .createdAt(order.getCreatedAt())
      .build()
   ↓
   RabbitMQProducer.publishOrderCreatedEvent(event)
   ↓
   RabbitTemplate.convertAndSend(
      "order.exchange",
      "order.created",
      event
   )
   ↓
Step 10: Publish PaymentRequestEvent to RabbitMQ
   PaymentRequestEvent event = PaymentRequestEvent.builder()
      .orderId(order.getId())
      .userId(userId)
      .amount(order.getTotalPrice())
      .build()
   ↓
   RabbitMQProducer.publishPaymentRequestEvent(event)
   ↓
   RabbitTemplate.convertAndSend(
      "order.exchange",
      "payment.request",
      event
   )
   ↓
Step 11: Clear cart
   CartService.clearCart(token)
   HTTP DELETE: http://localhost:8083/api/cart
   ↓
Step 12: Return order response
   OrderResponse
```

**Database Impact:**
```sql
-- Insert order
INSERT INTO orders (user_id, status, total_price, shipping_address, created_at, updated_at)
VALUES ('john@email.com', 'PENDING', 1999.98, '123 Main St', NOW(), NOW())
RETURNING id; -- id = 1

-- Insert order items
INSERT INTO order_items (order_id, product_id, product_name, price, quantity, image_url)
VALUES (1, 1, 'iPhone 15 Pro', 999.99, 2, 'url');
```

**RabbitMQ Impact:**
```
Message 1 to exchange "order.exchange" with routing key "order.created":
{
  "orderId": 1,
  "userId": "john@email.com",
  "totalPrice": 1999.98,
  "shippingAddress": "123 Main St",
  "createdAt": "2024-02-04T10:00:00"
}
→ Routed to queue: order.created.queue

Message 2 to exchange "order.exchange" with routing key "payment.request":
{
  "orderId": 1,
  "userId": "john@email.com",
  "amount": 1999.98
}
→ Routed to queue: payment.request.queue
```

### Key Components:

**Entities:**
- `Order`: id, userId, status, totalPrice, items[], shippingAddress
- `OrderItem`: id, order, productId, productName, price, quantity

**Events:**
- `OrderCreatedEvent`: For notification
- `PaymentRequestEvent`: For payment processing

**RabbitMQ:**
- Exchange: order.exchange (Topic)
- Queues: order.created.queue, payment.request.queue

---

## 5. Payment Service Flow

### Purpose:
Payment processing and event consumption/publishing

### Port: 8085

### Databases:
- PostgreSQL: ecommerce_payment
- RabbitMQ: Event consumption and publishing

### Architecture:
```
RabbitMQ (PaymentRequestEvent)
     ↓
Consumer (@RabbitListener)
     ↓
Service Layer (PaymentService)
     ↓
Mock Payment Processing (2 sec delay)
     ↓
Save Payment (PostgreSQL)
     ↓
Publish Result Event (RabbitMQ)
     ├─→ PaymentSuccessEvent (90%)
     └─→ PaymentFailedEvent (10%)
```

### Detailed Flow:

#### 5.1 Payment Processing Flow

**Trigger:**
```
PaymentRequestEvent received from RabbitMQ
(Published by Order Service)
```

**Flow:**
```
Step 1: Event received
   RabbitMQConsumer.consumePaymentRequestEvent(event)
   @RabbitListener(queues = "payment.request.queue")
   ↓
   Log: "Received PaymentRequestEvent for order ID: 1"
   ↓
Step 2: Call PaymentService
   PaymentService.processPayment(event)
   ↓
Step 3: Check if payment already exists
   PaymentRepository.existsByOrderId(orderId)
   ↓
   If exists:
      Log warning and return (prevent duplicate processing)
   ↓
   If NOT exists:
      Continue
   ↓
Step 4: Create Payment record
   Payment payment = Payment.builder()
      .orderId(event.getOrderId())
      .userId(event.getUserId())
      .amount(event.getAmount())
      .status(PENDING)
      .paymentMethod("MOCK_PAYMENT")
      .build()
   ↓
Step 5: Save to database
   PaymentRepository.save(payment)
   ↓
   Log: "Payment record created with ID: 1"
   ↓
Step 6: Simulate payment processing
   Thread.sleep(2000) // 2 seconds delay
   ↓
Step 7: Mock payment result (random)
   boolean isSuccess = Random.nextDouble() < 0.9 // 90% success rate
   ↓
   If SUCCESS (90% chance):
      ↓
      Step 8a: Update payment status
         transactionId = UUID.randomUUID()
         payment.setStatus(SUCCESS)
         payment.setTransactionId(transactionId)
         PaymentRepository.save(payment)
         ↓
         Log: "Payment SUCCESSFUL for order ID: 1"
         ↓
      Step 9a: Create PaymentResultEvent
         PaymentResultEvent resultEvent = PaymentResultEvent.builder()
            .orderId(payment.getOrderId())
            .paymentId(payment.getId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .status(SUCCESS)
            .transactionId(transactionId)
            .build()
         ↓
      Step 10a: Publish to RabbitMQ
         RabbitMQProducer.publishPaymentSuccessEvent(resultEvent)
         ↓
         RabbitTemplate.convertAndSend(
            "order.exchange",
            "payment.success",
            resultEvent
         )
         ↓
         Log: "PaymentSuccessEvent published"
   ↓
   If FAILED (10% chance):
      ↓
      Step 8b: Update payment status
         failureReason = "Insufficient funds or card declined (MOCK)"
         payment.setStatus(FAILED)
         payment.setFailureReason(failureReason)
         PaymentRepository.save(payment)
         ↓
         Log: "Payment FAILED for order ID: 1"
         ↓
      Step 9b: Create PaymentResultEvent
         PaymentResultEvent resultEvent = PaymentResultEvent.builder()
            .orderId(payment.getOrderId())
            .paymentId(payment.getId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .status(FAILED)
            .failureReason(failureReason)
            .build()
         ↓
      Step 10b: Publish to RabbitMQ
         RabbitMQProducer.publishPaymentFailedEvent(resultEvent)
         ↓
         RabbitTemplate.convertAndSend(
            "order.exchange",
            "payment.failed",
            resultEvent
         )
         ↓
         Log: "PaymentFailedEvent published"
```

**Database Impact:**
```sql
-- Create payment record
INSERT INTO payments (order_id, user_id, amount, status, payment_method, created_at, updated_at)
VALUES (1, 'john@email.com', 1999.98, 'PENDING', 'MOCK_PAYMENT', NOW(), NOW())
RETURNING id; -- id = 1

-- Update after processing (SUCCESS)
UPDATE payments
SET status = 'SUCCESS',
    transaction_id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    updated_at = NOW()
WHERE id = 1;

-- OR Update after processing (FAILED)
UPDATE payments
SET status = 'FAILED',
    failure_reason = 'Insufficient funds or card declined (MOCK)',
    updated_at = NOW()
WHERE id = 1;
```

**RabbitMQ Impact:**
```
SUCCESS case:
Message to exchange "order.exchange" with routing key "payment.success":
{
  "orderId": 1,
  "paymentId": 1,
  "userId": "john@email.com",
  "amount": 1999.98,
  "status": "SUCCESS",
  "transactionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
→ Routed to queue: payment.success.queue

FAILED case:
Message to exchange "order.exchange" with routing key "payment.failed":
{
  "orderId": 1,
  "paymentId": 1,
  "userId": "john@email.com",
  "amount": 1999.98,
  "status": "FAILED",
  "failureReason": "Insufficient funds or card declined (MOCK)"
}
→ Routed to queue: payment.failed.queue
```

### Key Components:

**Consumer:**
- `@RabbitListener` on payment.request.queue

**Producer:**
- Publishes to payment.success.queue OR payment.failed.queue

**Mock Logic:**
- 90% success rate
- 2-second processing delay
- Random transaction ID generation

---

## 6. Notification Service Flow

### Purpose:
Send notifications based on events

### Port: 8086

### Databases:
- PostgreSQL: ecommerce_notification
- RabbitMQ: Event consumption (3 queues)

### Architecture:
```
RabbitMQ Events
├─→ OrderCreatedEvent
├─→ PaymentSuccessEvent
└─→ PaymentFailedEvent
     ↓
Consumers (@RabbitListener × 3)
     ↓
Service Layer (EmailService, SMSService)
     ↓
Mock Notifications (Email + SMS)
     ↓
Save to Database (PostgreSQL)
```

### Detailed Flow:

#### 6.1 Order Created Notification Flow

**Trigger:**
```
OrderCreatedEvent received from RabbitMQ
(Published by Order Service)
```

**Flow:**
```
Step 1: Event received
   RabbitMQConsumer.consumeOrderCreatedEvent(event)
   @RabbitListener(queues = "order.created.queue")
   ↓
   Log: "📦 Received OrderCreatedEvent for order ID: 1"
   ↓
Step 2: Format data
   formattedPrice = $1,999.98
   ↓
Step 3: Send Email Notification
   EmailService.sendEmail(
      to: event.getUserId(),
      subject: "Order Confirmation - Order #1",
      message: "Dear Customer, Your order #1 has been successfully placed!
                Order Details:
                - Order ID: 1
                - Total Amount: $1,999.98
                - Shipping Address: 123 Main St
                - Order Date: 2024-02-04T10:00:00
                ..."
   )
   ↓
   Log: "📧 SENDING EMAIL"
   Log: "To: john@email.com"
   Log: "Subject: Order Confirmation - Order #1"
   ↓
Step 4: Save email notification to database
   Notification notification = Notification.builder()
      .userId("john@email.com")
      .type(EMAIL)
      .recipient("john@email.com")
      .subject("Order Confirmation - Order #1")
      .message("Dear Customer...")
      .eventType("ORDER_CREATED")
      .relatedId(1)
      .build()
   ↓
   NotificationRepository.save(notification)
   ↓
   Log: "✅ Email notification logged to database"
   ↓
Step 5: Send SMS Notification
   SMSService.sendSMS(
      to: event.getUserId(),
      message: "Order #1 confirmed! Total: $1,999.98. Track at ecommerce.com/orders/1"
   )
   ↓
   Log: "📱 SENDING SMS"
   Log: "To: john@email.com"
   ↓
Step 6: Save SMS notification to database
   Notification notification = Notification.builder()
      .userId("john@email.com")
      .type(SMS)
      .recipient("john@email.com")
      .subject("SMS Notification")
      .message("Order #1 confirmed!...")
      .eventType("ORDER_CREATED")
      .relatedId(1)
      .build()
   ↓
   NotificationRepository.save(notification)
   ↓
   Log: "✅ SMS notification logged to database"
   ↓
Step 7: Complete
   Log: "✅ Notifications sent successfully for order ID: 1"
```

#### 6.2 Payment Success Notification Flow

**Trigger:**
```
PaymentSuccessEvent received from RabbitMQ
(Published by Payment Service)
```

**Flow:**
```
Step 1: Event received
   RabbitMQConsumer.consumePaymentSuccessEvent(event)
   @RabbitListener(queues = "payment.success.queue")
   ↓
   Log: "💳 Received PaymentSuccessEvent for order ID: 1"
   ↓
Step 2: Send Email
   subject: "Payment Successful - Order #1"
   message: "Your payment has been successfully processed!
             Payment Details:
             - Order ID: 1
             - Payment ID: 1
             - Amount Paid: $1,999.98
             - Transaction ID: a1b2c3d4-..."
   ↓
Step 3: Save email notification
   ↓
Step 4: Send SMS
   message: "Payment successful! Order #1 paid: $1,999.98. Transaction: a1b2c3d4-..."
   ↓
Step 5: Save SMS notification
   ↓
   Log: "✅ Payment success notifications sent for order ID: 1"
```

#### 6.3 Payment Failed Notification Flow

**Trigger:**
```
PaymentFailedEvent received from RabbitMQ
(Published by Payment Service)
```

**Flow:**
```
Step 1: Event received
   RabbitMQConsumer.consumePaymentFailedEvent(event)
   @RabbitListener(queues = "payment.failed.queue")
   ↓
   Log: "❌ Received PaymentFailedEvent for order ID: 1"
   ↓
Step 2: Send Email
   subject: "Payment Failed - Order #1"
   message: "Unfortunately, your payment could not be processed.
             Reason: Insufficient funds or card declined (MOCK)
             Please try again or use a different payment method..."
   ↓
Step 3: Save email notification
   ↓
Step 4: Send SMS
   message: "Payment failed for Order #1. Reason: Insufficient funds. Please retry at ecommerce.com"
   ↓
Step 5: Save SMS notification
   ↓
   Log: "✅ Payment failure notifications sent for order ID: 1"
```

**Database Impact:**
```sql
-- For each notification
INSERT INTO notifications (user_id, type, recipient, subject, message, sent_at, event_type, related_id)
VALUES ('john@email.com', 'EMAIL', 'john@email.com', 'Order Confirmation - Order #1', 'Dear Customer...', NOW(), 'ORDER_CREATED', 1);

-- Typically 4 notifications per order:
-- 1. Order Created - Email
-- 2. Order Created - SMS
-- 3. Payment Success/Failed - Email
-- 4. Payment Success/Failed - SMS
```

### Key Components:

**3 Consumers:**
- Order Created consumer
- Payment Success consumer
- Payment Failed consumer

**2 Services:**
- EmailService (mock)
- SMSService (mock)

**Notification Types:**
- EMAIL
- SMS

---

## 7. API Gateway Flow

### Purpose:
Single entry point and request routing

### Port: 8080

### Architecture:
```
Client Request
     ↓
API Gateway (Port 8080)
     ↓
Route Matching (Spring Cloud Gateway)
     ↓
Target Service
├─→ Auth Service (8081)
├─→ Product Service (8082)
├─→ Cart Service (8083)
├─→ Order Service (8084)
├─→ Payment Service (8085)
└─→ Notification Service (8086)
     ↓
Response to Client
```

### Detailed Flow:

#### 7.1 Request Routing Flow

**Example Request:**
```
POST http://localhost:8080/api/products
```

**Flow:**
```
Step 1: Request received at API Gateway
   POST http://localhost:8080/api/products
   ↓
Step 2: Spring Cloud Gateway matches route
   Path pattern matching: /api/products/**
   ↓
   Matched route configuration:
   - id: product-service
   - uri: http://localhost:8082
   - predicates: Path=/api/products/**
   - filters: StripPrefix=0
   ↓
Step 3: Apply filters
   CORS filter applied (if configured)
   ↓
Step 4: Forward request to target service
   POST http://localhost:8082/api/products
   (All headers and body forwarded)
   ↓
Step 5: Target service processes request
   Product Service handles the request
   ↓
Step 6: Response received from target service
   ProductDTO response
   ↓
Step 7: Gateway forwards response to client
   Same response, same status code
```

#### 7.2 Routing Table

```
Request Path                    → Target Service
─────────────────────────────────────────────────────
/api/auth/register             → http://localhost:8081/api/auth/register
/api/auth/login                → http://localhost:8081/api/auth/login
/api/products                  → http://localhost:8082/api/products
/api/products/1                → http://localhost:8082/api/products/1
/api/products/search           → http://localhost:8082/api/products/search
/api/categories                → http://localhost:8082/api/categories
/api/cart                      → http://localhost:8083/api/cart
/api/cart/items                → http://localhost:8083/api/cart/items
/api/orders                    → http://localhost:8084/api/orders
/api/orders/1                  → http://localhost:8084/api/orders/1
/api/payments/order/1          → http://localhost:8085/api/payments/order/1
/api/notifications/user/email  → http://localhost:8086/api/notifications/user/email
```

### CORS Handling:

```
All routes have CORS configured:
- allowedOrigins: "*"
- allowedMethods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- allowedHeaders: "*"
- allowCredentials: false
```

### Fallback Handling:

```
If target service is down:
→ FallbackController.{service}Fallback()
→ Returns: {
     "timestamp": "...",
     "status": 503,
     "error": "Service Unavailable",
     "message": "X Service is currently unavailable"
   }
```

---

## 8. Complete System Flow

### End-to-End User Journey

```
USER JOURNEY: Order an iPhone
═══════════════════════════════════════════════════════

┌─────────────┐
│   Step 1    │  User Registration
└─────────────┘
User → API Gateway (8080) → Auth Service (8081)
POST /api/auth/register
↓
Auth Service:
  1. Encrypt password
  2. Save to PostgreSQL
  3. Generate JWT token
  4. Return token
↓
User receives JWT token

┌─────────────┐
│   Step 2    │  Admin Creates Products
└─────────────┘
Admin → API Gateway (8080) → Product Service (8082)
POST /api/products (with ADMIN token)
↓
Product Service:
  1. Validate JWT
  2. Check ADMIN role
  3. Save product to PostgreSQL
  4. Evict cache
  5. Return product
↓
Product created

┌─────────────┐
│   Step 3    │  User Browses Products
└─────────────┘
User → API Gateway (8080) → Product Service (8082)
GET /api/products
↓
Product Service:
  1. Check Redis cache
     ├─ Found? Return from cache
     └─ Not found?
         ├─ Query PostgreSQL
         ├─ Cache in Redis (10 min TTL)
         └─ Return results
↓
User sees product list

┌─────────────┐
│   Step 4    │  User Adds to Cart
└─────────────┘
User → API Gateway (8080) → Cart Service (8083)
POST /api/cart/items (with USER token)
{
  "productId": 1,
  "quantity": 2
}
↓
Cart Service:
  1. Validate JWT
  2. Call Product Service → Get product details
  3. Validate stock
  4. Get cart from Redis
  5. Add/Update item
  6. Save cart to Redis (7 day TTL)
  7. Return cart
↓
Cart updated in Redis

┌─────────────┐
│   Step 5    │  User Creates Order
└─────────────┘
User → API Gateway (8080) → Order Service (8084)
POST /api/orders (with USER token)
{
  "shippingAddress": "123 Main St"
}
↓
Order Service:
  1. Validate JWT
  2. Call Cart Service → Get cart
  3. Create Order entity
  4. Save to PostgreSQL
     ├─ INSERT into orders
     └─ INSERT into order_items
  5. Publish OrderCreatedEvent → RabbitMQ
     └─ To: order.created.queue
  6. Publish PaymentRequestEvent → RabbitMQ
     └─ To: payment.request.queue
  7. Call Cart Service → Clear cart
     └─ DELETE from Redis
  8. Return order
↓
Order created, events published

┌─────────────┐
│   Step 6    │  Notification Service Reacts
└─────────────┘
RabbitMQ (order.created.queue)
↓
Notification Service (8086):
  @RabbitListener receives OrderCreatedEvent
  ↓
  1. Send email notification (mock)
     Log: "📧 SENDING EMAIL"
     Log: "Subject: Order Confirmation - Order #1"
  2. Save email to PostgreSQL
  3. Send SMS notification (mock)
     Log: "📱 SENDING SMS"
  4. Save SMS to PostgreSQL
  ↓
User notified about order

┌─────────────┐
│   Step 7    │  Payment Service Processes Payment
└─────────────┘
RabbitMQ (payment.request.queue)
↓
Payment Service (8085):
  @RabbitListener receives PaymentRequestEvent
  ↓
  1. Create Payment record (PENDING)
  2. Save to PostgreSQL
  3. Simulate processing (2 second delay)
  4. Random success/failure (90% success)
  5. Update payment status
     ├─ SUCCESS: Generate transaction ID
     └─ FAILED: Set failure reason
  6. Save to PostgreSQL
  7. Publish PaymentResultEvent → RabbitMQ
     ├─ SUCCESS → payment.success.queue
     └─ FAILED → payment.failed.queue
  ↓
Payment processed

┌─────────────┐
│   Step 8    │  Payment Notification
└─────────────┘
RabbitMQ (payment.success.queue OR payment.failed.queue)
↓
Notification Service (8086):
  @RabbitListener receives PaymentResultEvent
  ↓
  SUCCESS case:
    1. Send "Payment Successful" email
    2. Send "Payment Successful" SMS
    3. Save both to PostgreSQL
  ↓
  FAILED case:
    1. Send "Payment Failed" email
    2. Send "Payment Failed" SMS
    3. Save both to PostgreSQL
  ↓
User notified about payment result

┌─────────────┐
│   Step 9    │  User Checks Status
└─────────────┘
User → API Gateway (8080) → Various Services
├─ GET /api/orders → Order Service
├─ GET /api/payments/order/1 → Payment Service
└─ GET /api/notifications/user/email → Notification Service
↓
User sees complete order status

═══════════════════════════════════════════════════════
COMPLETE!
```

### Data Flow Summary:

```
PostgreSQL Databases:
├─ ecommerce_auth: 1 user record
├─ ecommerce_product: 1 product record
├─ ecommerce_order: 1 order + 1 order_item
├─ ecommerce_payment: 1 payment record
└─ ecommerce_notification: 4 notification records (2 order + 2 payment)

Redis:
├─ products::all:0:10:name:ASC (cached product list)
├─ products::1 (cached individual product)
└─ cart:john@email.com (DELETED after order)

RabbitMQ Events:
├─ OrderCreatedEvent (consumed by Notification)
├─ PaymentRequestEvent (consumed by Payment)
└─ PaymentResultEvent (consumed by Notification)
```

### Time Flow:

```
T+0.0s  User creates order
T+0.1s  Order saved to DB
T+0.2s  Events published to RabbitMQ
T+0.3s  Notification receives OrderCreatedEvent
T+0.4s  Order confirmation email/SMS sent
T+0.5s  Payment receives PaymentRequestEvent
T+2.5s  Payment processed (2 sec delay)
T+2.6s  Payment result published
T+2.7s  Notification receives PaymentResultEvent
T+2.8s  Payment result email/SMS sent
T+3.0s  Complete!
```

---

## 🎯 Key Takeaways

### Service Responsibilities:

1. **Auth**: Authentication only
2. **Product**: Catalog + Caching
3. **Cart**: Temporary storage
4. **Order**: Orchestration + Event publishing
5. **Payment**: Async processing + Event consumption/publishing
6. **Notification**: Event consumption + Notification sending
7. **Gateway**: Routing only

### Integration Patterns:

- **Synchronous**: REST API calls (Cart → Product, Order → Cart)
- **Asynchronous**: RabbitMQ events (Order → Payment → Notification)
- **Caching**: Redis for performance (Product caching)
- **Storage**: Redis for ephemeral data (Cart)

### Data Persistence:

- **PostgreSQL**: Permanent data (Users, Products, Orders, Payments, Notifications)
- **Redis**: Temporary data (Cart - 7 days, Product cache - 10 min)
- **RabbitMQ**: Transient messages (Events processed immediately)

---

**This completes the detailed service flow documentation!**
