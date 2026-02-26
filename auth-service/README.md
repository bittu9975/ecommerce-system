# Auth Service

## Overview
Authentication and Authorization microservice for the E-Commerce System. Handles user registration, login, and JWT token generation.

## Features
- User Registration
- User Login
- JWT Token Generation & Validation
- Role-based Access Control (USER, ADMIN)
- Password Encryption using BCrypt
- Spring Security Integration

## Technology Stack
- Java 21
- Spring Boot 3.2.2
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Tokens)
- Lombok
- Maven

## Database Schema
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

## Prerequisites
- Java 21
- Maven
- PostgreSQL (running on localhost:5432)
- PostgreSQL database named `ecommerce_auth`

## Setup Instructions

### 1. Create Database
```bash
# Open PostgreSQL terminal
psql -U postgres

# Create database
CREATE DATABASE ecommerce_auth;
```

### 2. Update Configuration (if needed)
Edit `src/main/resources/application.yml` to update database credentials:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

### 3. Build the Project
```bash
cd auth-service
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The service will start on port **8081**.

## API Endpoints

### Health Check
```http
GET http://localhost:8081/api/auth/health
```

### Register User
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

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Login
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Get Current User
```http
GET http://localhost:8081/api/auth/me
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER",
    "createdAt": "2024-02-02T10:30:00"
}
```

## Testing with Postman

1. **Register a new user** using the `/register` endpoint
2. Copy the `token` from the response
3. **Login** using the `/login` endpoint (optional)
4. **Get current user** by adding `Authorization: Bearer <token>` header

## Project Structure
```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/authservice/
│   │   │   ├── config/          # Security & JWT configuration
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Exception handling
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   ├── util/            # Utility classes (JWT)
│   │   │   └── AuthServiceApplication.java
│   │   └── resources/
│   │       └── application.yml  # Configuration
│   └── test/
├── pom.xml
└── README.md
```

## Security Configuration
- JWT secret key is stored in `application.yml`
- Token expiration: 24 hours (86400000 ms)
- Password encryption: BCrypt
- CORS: Disabled (enable in production)
- CSRF: Disabled (stateless JWT)

## Error Handling
The service provides proper error responses:
- **400 Bad Request**: Validation errors, user already exists
- **401 Unauthorized**: Invalid credentials
- **500 Internal Server Error**: Unexpected errors

## Next Steps
- Integrate with Product Service
- Add API Gateway
- Implement refresh tokens
- Add email verification
- Add forgot password functionality

## Notes for Resume
- Implemented JWT-based authentication
- Used Spring Security for access control
- Role-based authorization (USER, ADMIN)
- Password encryption using BCrypt
- RESTful API design
- Exception handling
- Input validation
