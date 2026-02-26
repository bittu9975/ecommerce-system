# Payment Service

## Overview
Payment Processing microservice with **RabbitMQ event consumption**. Listens for payment requests and processes mock payments asynchronously.

## Features
- ✅ **RabbitMQ event consumer** (listens to payment.request.queue)
- ✅ **Mock payment processing** (90% success rate)
- ✅ **Event publishing** (publishes OrderPaidEvent)
- ✅ Payment status tracking
- ✅ Transaction ID generation
- ✅ PostgreSQL persistence
- ✅ Async payment processing

## Technology Stack
- Java 21
- Spring Boot 3.2.2
- **Spring AMQP (RabbitMQ Consumer)**
- Spring Data JPA
- PostgreSQL
- Maven

## Event-Driven Architecture

### Consumes:
```
Order Service → RabbitMQ → Payment Service
(PaymentRequestEvent)
```

### Produces:
```
Payment Service → RabbitMQ → Order/Notification Services
(OrderPaidEvent)
```

## Database Schema
```sql
payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
)
```

## Prerequisites
- Java 21
- Maven
- PostgreSQL (localhost:5432)
- **RabbitMQ** (localhost:5672) - **CRITICAL!**
- Order Service (for testing integration)

## Setup

### 1. Create Database
```bash
psql -U postgres -c "CREATE DATABASE ecommerce_payment"
```

### 2. Verify RabbitMQ Running
```bash
rabbitmqctl status

# Management UI
http://localhost:15672 (guest/guest)
```

### 3. Run Payment Service
```bash
cd payment-service
mvn spring-boot:run
```

Service runs on **http://localhost:8085**

## How It Works

### Payment Flow:
1. **Order Service** creates order → publishes `PaymentRequestEvent`
2. **Payment Service** listens to `payment.request.queue`
3. Receives event with order ID, user ID, amount
4. Creates payment record (status: PROCESSING)
5. Simulates 2-second payment delay
6. Mock payment processing (90% success rate)
7. Updates payment status (SUCCESS or FAILED)
8. If successful → publishes `OrderPaidEvent`
9. Order Service can update order status to PAID

### Mock Payment Logic:
```java
// 90% success rate (configurable)
payment.mock.success-rate: 90

// Random success simulation
int random = 0-99
if (random < 90) → SUCCESS
else → FAILED (Insufficient funds)
```

## API Endpoints

### Health Check
```http
GET http://localhost:8085/api/payments/health
```

### Get Payment by Order ID
```http
GET http://localhost:8085/api/payments/order/1
```

**Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "userId": "test@test.com",
  "amount": 1999.98,
  "status": "SUCCESS",
  "paymentMethod": "MOCK_PAYMENT",
  "transactionId": "TXN-A1B2C3D4",
  "failureReason": null,
  "createdAt": "2024-02-03T10:00:00",
  "updatedAt": "2024-02-03T10:00:02"
}
```

## Testing Complete Flow

```bash
# 1. Start all services
# - Auth (8081)
# - Product (8082)
# - Cart (8083)
# - Order (8084)
# - Payment (8085)

# 2. Login and get token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# 3. Add product to cart
curl -X POST http://localhost:8083/api/cart/items \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 4. Create order (triggers payment processing!)
curl -X POST http://localhost:8084/api/orders \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"123 Main St"}'

# 5. Check payment status
curl http://localhost:8085/api/payments/order/1

# 6. Check RabbitMQ
# http://localhost:15672
# - Queues tab
# - See payment.request.queue consumed
# - See order.paid.queue has message
```

## Verify RabbitMQ Messages

### In RabbitMQ Management UI:
1. Go to **Queues** tab
2. Find `payment.request.queue`
   - Should show messages consumed
3. Find `order.paid.queue`
   - Should show messages published (if payment succeeded)

### View Message Details:
```bash
# In RabbitMQ Management UI
Queues → payment.request.queue → Get Messages

You'll see:
{
  "orderId": 1,
  "userId": "test@test.com",
  "amount": 999.99
}
```

## Payment Status Values

```
PENDING     - Payment record created
PROCESSING  - Payment being processed
SUCCESS     - Payment successful (90% chance)
FAILED      - Payment failed (10% chance)
REFUNDED    - Payment refunded (future feature)
```

## Configuration

### Adjust Success Rate:
```yaml
# application.yml
payment:
  mock:
    enabled: true
    success-rate: 90  # Change to 100 for always success
```

### Disable Mock (Always Success):
```yaml
payment:
  mock:
    enabled: false  # Real payment gateway integration
```

## Troubleshooting

### Payment Not Processing?
```bash
# Check RabbitMQ logs
# Check Payment Service logs - should see:
# "Received PaymentRequestEvent for order: X"

# Verify queue exists
rabbitmqctl list_queues
```

### Payment Always Fails?
```bash
# Check success rate in application.yml
# Set to 100 for testing:
payment.mock.success-rate: 100
```

### No OrderPaidEvent Published?
- Payment must be successful (90% chance)
- Check Payment Service logs
- Check RabbitMQ order.paid.queue

## Resume Points
- ✅ Implemented **event-driven payment processing**
- ✅ **RabbitMQ consumer** with @RabbitListener
- ✅ Async message processing
- ✅ Event publishing for order updates
- ✅ Mock payment gateway simulation
- ✅ Transaction management

## Production Considerations

**For Production:**
- Replace mock with real payment gateway (Stripe, PayPal, etc.)
- Add payment retry logic
- Implement idempotency (prevent duplicate payments)
- Add webhook endpoints for payment updates
- Implement refund functionality
- Add payment method validation
- Store encrypted payment details (PCI compliance)
- Add payment notifications

**Current Implementation:**
- ✅ Event-driven architecture ready
- ✅ Database schema production-ready
- ✅ Async processing implemented
- ✅ Error handling in place
- ✅ Logging configured

---

**Payment Service Complete! 💳**
