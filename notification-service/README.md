# Notification Service

## Overview
Event-driven Notification Service that **listens to RabbitMQ events** and sends email/SMS notifications.

## Features
- ✅ **3 RabbitMQ Consumers** (multiple event listeners!)
- ✅ Email notifications (mock)
- ✅ SMS notifications (mock)
- ✅ Notification logging (PostgreSQL)
- ✅ Event-driven architecture
- ✅ Listens to Order & Payment events

## Technology Stack
- Java 21
- Spring Boot 3.2.2
- **Spring AMQP (RabbitMQ)**
- Spring Data JPA
- PostgreSQL
- Maven

## Event-Driven Architecture

```
Order Service → OrderCreatedEvent
     ↓ (RabbitMQ)
Notification Service
     ↓
📧 Email + 📱 SMS

Payment Service → PaymentSuccessEvent
     ↓ (RabbitMQ)
Notification Service
     ↓
📧 Email + 📱 SMS

Payment Service → PaymentFailedEvent
     ↓ (RabbitMQ)
Notification Service
     ↓
📧 Email + 📱 SMS
```

## RabbitMQ Consumers

### 1. Order Created Consumer
- **Listens to:** `order.created.queue`
- **Event:** `OrderCreatedEvent`
- **Sends:** Order confirmation email + SMS

### 2. Payment Success Consumer
- **Listens to:** `payment.success.queue`
- **Event:** `PaymentResultEvent`
- **Sends:** Payment success email + SMS

### 3. Payment Failed Consumer
- **Listens to:** `payment.failed.queue`
- **Event:** `PaymentResultEvent`
- **Sends:** Payment failure email + SMS

## Database Schema

```sql
notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    event_type VARCHAR(50),
    related_id BIGINT
)
```

## Prerequisites
- Java 21
- Maven
- PostgreSQL (localhost:5432)
- **RabbitMQ** (localhost:5672)
- Order Service (8084)
- Payment Service (8085)

## Setup

### 1. Create Database
```bash
psql -U postgres -c "CREATE DATABASE ecommerce_notification"
```

### 2. Start RabbitMQ
```bash
rabbitmqctl status
```

### 3. Run Notification Service
```bash
cd notification-service
mvn spring-boot:run
```

Service runs on **http://localhost:8086**

## How It Works

### Complete Flow:

1. **User creates order**
   - Order Service publishes `OrderCreatedEvent`
   - Notification Service receives event
   - Sends order confirmation email + SMS
   - Logs notifications to database

2. **Payment is processed**
   - Payment Service publishes `PaymentResultEvent`
   - Notification Service receives event
   - Sends payment success/failure email + SMS
   - Logs notifications to database

3. **User can view notifications**
   - GET `/api/notifications/user/{userId}`
   - See all sent notifications

## API Endpoints

### Health Check
```http
GET http://localhost:8086/api/notifications/health
```

### Get User Notifications
```http
GET http://localhost:8086/api/notifications/user/test@test.com
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": "test@test.com",
    "type": "EMAIL",
    "recipient": "test@test.com",
    "subject": "Order Confirmation - Order #1",
    "message": "Dear Customer...",
    "sentAt": "2024-02-03T10:00:00",
    "eventType": "ORDER_CREATED",
    "relatedId": 1
  },
  {
    "id": 2,
    "userId": "test@test.com",
    "type": "SMS",
    "recipient": "test@test.com",
    "subject": "SMS Notification",
    "message": "Order #1 confirmed!...",
    "sentAt": "2024-02-03T10:00:01",
    "eventType": "ORDER_CREATED",
    "relatedId": 1
  }
]
```

### Get All Notifications
```http
GET http://localhost:8086/api/notifications
```

## Testing the Complete Flow

### End-to-End Test:

```bash
# 1. Make sure all services are running:
# - Auth (8081)
# - Product (8082)
# - Cart (8083)
# - Order (8084)
# - Payment (8085)
# - Notification (8086) ← NEW!
# - RabbitMQ

# 2. Watch Notification Service logs
cd notification-service
mvn spring-boot:run

# 3. In another terminal, create an order
curl -X POST http://localhost:8084/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"123 Main St"}'

# 4. Watch the Notification Service terminal!
# You'll see:
# "📦 Received OrderCreatedEvent"
# "📧 SENDING EMAIL"
# "📱 SENDING SMS"
# "✅ Email notification logged"
# "✅ SMS notification logged"

# Wait 2-3 seconds for payment processing...

# Then you'll see:
# "💳 Received PaymentSuccessEvent" (or Failed)
# "📧 SENDING EMAIL"
# "📱 SENDING SMS"

# 5. View notifications
curl http://localhost:8086/api/notifications/user/test@test.com
```

## Notification Examples

### Order Created Email:
```
Subject: Order Confirmation - Order #1

Dear Customer,

Your order #1 has been successfully placed!

Order Details:
- Order ID: 1
- Total Amount: $1,999.98
- Shipping Address: 123 Main St, City
- Order Date: 2024-02-03T10:00:00

Your order is being processed and payment is pending.

Thank you for shopping with us!

Best regards,
E-Commerce Team
```

### Payment Success Email:
```
Subject: Payment Successful - Order #1

Dear Customer,

Your payment has been successfully processed!

Payment Details:
- Order ID: 1
- Payment ID: 1
- Amount Paid: $1,999.98
- Transaction ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
- Status: PAID

Your order is now being prepared for shipment.

Thank you for your payment!

Best regards,
E-Commerce Team
```

### Payment Failed Email:
```
Subject: Payment Failed - Order #1

Dear Customer,

Unfortunately, your payment could not be processed.

Payment Details:
- Order ID: 1
- Payment ID: 1
- Amount: $1,999.98
- Reason: Insufficient funds or card declined (MOCK)

Please try again or use a different payment method.
Visit: ecommerce.com/orders/1 to retry payment.

If you need assistance, please contact our support team.

Best regards,
E-Commerce Team
```

## Configuration

### Enable/Disable Notifications (application.yml):
```yaml
notification:
  email:
    from: noreply@ecommerce.com
    enabled: true
  sms:
    enabled: true
```

Change to:
- `enabled: false` - Disable email/SMS
- `from: custom@email.com` - Change sender

## Project Structure

```
notification-service/
├── src/main/java/com/ecommerce/notificationservice/
│   ├── NotificationServiceApplication.java
│   ├── config/
│   │   └── RabbitMQConfig.java
│   ├── controller/
│   │   └── NotificationController.java
│   ├── entity/
│   │   ├── Notification.java
│   │   └── NotificationType.java
│   ├── event/
│   │   ├── OrderCreatedEvent.java
│   │   └── PaymentResultEvent.java
│   ├── repository/
│   │   └── NotificationRepository.java
│   └── service/
│       ├── EmailService.java
│       ├── SMSService.java
│       ├── NotificationService.java
│       └── RabbitMQConsumer.java  ← 3 LISTENERS!
└── resources/
    └── application.yml
```

## Resume Points

- ✅ Built **multi-consumer notification service** with RabbitMQ
- ✅ Implemented **3 event listeners** (@RabbitListener)
- ✅ **Event-driven notification system**
- ✅ Email and SMS notification patterns
- ✅ Notification logging and history
- ✅ **Reactive microservices architecture**

## What Makes This Special

### Multiple Event Listeners:
- One service, 3 consumers
- Different queues, different actions
- Fully decoupled from other services

### Real-World Pattern:
```
Order → Notification (immediate)
Payment Success → Notification (2 sec later)
Payment Failed → Notification (2 sec later)
```

This is how production systems work:
- SendGrid/Twilio webhooks
- AWS SNS/SQS notifications
- Kafka event consumers

---

**Notification Service Complete!** 📧📱

Next: API Gateway (final service!)
