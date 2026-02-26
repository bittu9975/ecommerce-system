# Order Service

## Overview
Order Processing microservice with **RabbitMQ event-driven architecture**. Creates orders from carts and publishes async events.

## Features
- ✅ Create orders from cart
- ✅ View order history
- ✅ Order status tracking
- ✅ **RabbitMQ event publishing**
- ✅ Integration with Cart Service
- ✅ JWT authentication
- ✅ PostgreSQL persistence

## Technology Stack
- Java 21
- Spring Boot 3.2.2
- **Spring AMQP (RabbitMQ)**
- Spring Data JPA
- PostgreSQL
- JWT
- Maven

## Database Schema
```sql
orders (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    shipping_address VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
)

order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    image_url VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id)
)
```

## Prerequisites
- Java 21
- Maven
- PostgreSQL (localhost:5432)
- **RabbitMQ** (localhost:5672) - **CRITICAL!**
- Cart Service (localhost:8083)
- Product Service (localhost:8082)
- Auth Service (localhost:8081)

## Setup

### 1. Create Database
```bash
psql -U postgres -c "CREATE DATABASE ecommerce_order"
```

### 2. Start RabbitMQ
```bash
# Windows (if installed as service)
# Check status
rabbitmqctl status

# Access Management UI
# http://localhost:15672 (guest/guest)
```

### 3. Run Order Service
```bash
cd order-service
mvn spring-boot:run
```

Service runs on **http://localhost:8084**

## RabbitMQ Configuration

### Exchange & Queues Created:
```
Exchange: order.exchange (Topic)

Queues:
- order.created.queue (routing-key: order.created)
- order.paid.queue (routing-key: order.paid)
- payment.request.queue (routing-key: payment.request)
```

### Events Published:
1. **OrderCreatedEvent** → Notification Service
2. **PaymentRequestEvent** → Payment Service

## API Endpoints

### Create Order
```http
POST http://localhost:8084/api/orders
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "shippingAddress": "123 Main St, City, Country"
}
```

**Flow:**
1. Get cart from Cart Service
2. Create order in database
3. Publish ORDER_CREATED event
4. Publish PAYMENT_REQUEST event
5. Clear cart

### Get User Orders
```http
GET http://localhost:8084/api/orders?page=0&size=10
Authorization: Bearer <JWT_TOKEN>
```

### Get Order by ID
```http
GET http://localhost:8084/api/orders/1
Authorization: Bearer <JWT_TOKEN>
```

## Testing Workflow

```bash
# 1. Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# 2. Add products to cart
curl -X POST http://localhost:8083/api/cart/items \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 3. Create order
curl -X POST http://localhost:8084/api/orders \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"123 Main St"}'

# 4. View orders
curl http://localhost:8084/api/orders \
  -H "Authorization: Bearer TOKEN"
```

## Verify RabbitMQ

```bash
# Access RabbitMQ Management UI
http://localhost:15672

# Login: guest/guest

# Check:
- Exchanges → order.exchange
- Queues → 3 queues created
- Messages published after creating order
```

## Resume Points
- ✅ Implemented **event-driven architecture** with RabbitMQ
- ✅ Async messaging between microservices
- ✅ Service integration (Cart Service)
- ✅ Transaction management
- ✅ Order workflow orchestration

**Order Service Complete!** 📦
