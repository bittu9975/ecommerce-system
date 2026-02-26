# E-Commerce Backend System

A scalable, microservices-based e-commerce backend system built with Spring Boot, PostgreSQL, Redis, and RabbitMQ.

## 🎯 Project Overview

This project demonstrates a production-ready microservices architecture for an e-commerce platform similar to Amazon/Flipkart, featuring:

- **User Authentication & Authorization** (JWT-based)
- **Product Catalog & Search**
- **Shopping Cart Management**
- **Order Processing**
- **Payment Integration** (Mock)
- **Asynchronous Communication** (RabbitMQ)
- **Caching** (Redis)
- **API Gateway**
- **Event-Driven Architecture**

## 🏗️ Architecture

```
[ Client (Web / Mobile / Postman) ]
                |
          [ API Gateway ]
                |
 ---------------------------------------------------------
 | Auth | Product | Cart | Order | Payment | Notification |
 ---------------------------------------------------------
        |        |        |       |           |
     PostgreSQL PostgreSQL PostgreSQL PostgreSQL PostgreSQL
                |
              Redis
                |
             RabbitMQ
```

## 🛠️ Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.2.2**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **Spring Cloud Gateway**

### Databases
- **PostgreSQL** (Persistent Storage)
- **Redis** (Caching & Cart Storage)

### Messaging
- **RabbitMQ** (Asynchronous Communication)

### Tools
- **Maven** (Build Tool)
- **Lombok** (Code Generation)
- **Postman** (API Testing)

## 📦 Microservices

| Service | Port | Description | Status |
|---------|------|-------------|--------|
| **Auth Service** | 8081 | User authentication & authorization | ✅ Complete |
| **Product Service** | 8082 | Product catalog & search | ✅ Complete |
| **Cart Service** | 8083 | Shopping cart management | 📋 Planned |
| **Order Service** | 8084 | Order processing | 📋 Planned |
| **Payment Service** | 8085 | Payment processing (mock) | 📋 Planned |
| **Notification Service** | 8086 | Notifications (email, SMS) | 📋 Planned |
| **API Gateway** | 8080 | Single entry point | 📋 Planned |

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- ✅ Java 21
- ✅ Maven 3.8+
- ✅ PostgreSQL 15+
- ✅ Redis 7+
- ✅ RabbitMQ 3.12+
- ✅ Postman (for API testing)

### Installation Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/ecommerce-system.git
cd ecommerce-system
```

#### 2. Setup PostgreSQL Databases
```bash
# Login to PostgreSQL
psql -U postgres

# Run the setup script
\i docs/database-setup.sql
```

Or create databases manually:
```sql
CREATE DATABASE ecommerce_auth;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_order;
CREATE DATABASE ecommerce_payment;
```

#### 3. Start Redis
```bash
# Windows (if installed as service)
redis-server

# Or check if running
redis-cli ping
# Should respond with PONG
```

#### 4. Start RabbitMQ
```bash
# Windows (if installed as service)
# RabbitMQ usually starts automatically

# Check status
rabbitmqctl status

# Access Management UI
# http://localhost:15672 (guest/guest)
```

#### 5. Build All Services
```bash
# From root directory
mvn clean install
```

#### 6. Run Auth Service
```bash
cd auth-service
mvn spring-boot:run
```

Service will start on **http://localhost:8081**

## 📝 Testing the Services

### Using Postman

1. Import the Postman collection: `docs/Auth-Service-Postman-Collection.json`
2. Test the endpoints:

**Health Check:**
```http
GET http://localhost:8081/api/auth/health
```

**Register User:**
```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "USER"
}
```

**Login:**
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123"
}
```

### Using cURL

```bash
# Health Check
curl http://localhost:8081/api/auth/health

# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123","role":"USER"}'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

## 📂 Project Structure

```
ecommerce-system/
├── api-gateway/              # API Gateway service
├── auth-service/             # Authentication service ✅
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ecommerce/authservice/
│   │   │   │   ├── config/          # Security config
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # DTOs
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── exception/       # Exception handling
│   │   │   │   ├── repository/      # Repositories
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── util/            # JWT utilities
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── pom.xml
│   └── README.md
├── product-service/          # Product catalog service 🚧
├── cart-service/             # Shopping cart service 📋
├── order-service/            # Order processing service 📋
├── payment-service/          # Payment service 📋
├── notification-service/     # Notification service 📋
├── docs/                     # Documentation & SQL scripts
│   ├── database-setup.sql
│   └── Auth-Service-Postman-Collection.json
├── pom.xml                   # Parent POM
└── README.md                 # This file
```

## 🔐 Security

- **JWT Authentication**: Secure token-based authentication
- **BCrypt Password Encoding**: Industry-standard password hashing
- **Role-Based Access Control**: USER and ADMIN roles
- **Stateless Sessions**: No server-side session management

## 📊 Database Schema

### Auth Service - Users Table
```sql
users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
)
```

## 🎯 Development Roadmap

### Phase 1: Core Services ✅
- [x] Auth Service (Registration, Login, JWT)
- [ ] Product Service (CRUD, Search, Categories)
- [ ] Cart Service (Redis-based cart)
- [ ] Order Service (Order creation, history)

### Phase 2: Integration 🚧
- [ ] API Gateway (Routing, Load balancing)
- [ ] RabbitMQ Integration
- [ ] Payment Service (Mock payment)
- [ ] Notification Service

### Phase 3: Enhancement 📋
- [ ] Swagger/OpenAPI Documentation
- [ ] Unit & Integration Tests
- [ ] Logging (ELK Stack)
- [ ] Monitoring (Prometheus, Grafana)

### Phase 4: DevOps 📋
- [ ] Docker Containerization
- [ ] Docker Compose
- [ ] CI/CD Pipeline
- [ ] Cloud Deployment (AWS/Azure)

## 🤝 Contributing

This is a portfolio project. Feel free to fork and modify for your own learning!

## 📄 License

This project is open-source and available for learning purposes.

## 👤 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/bittu9975)
- LinkedIn: [Your LinkedIn](https://www.linkedin.com/in/durgeshdhurve/)

## 🙏 Acknowledgments

- Spring Boot Documentation
- Baeldung Tutorials
- Java Brains YouTube Channel

---

## 📞 Support

If you have any questions or issues, please open an issue on GitHub.

**Happy Coding! 🚀**
