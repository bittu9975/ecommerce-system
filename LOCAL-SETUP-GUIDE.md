# 🚀 COMPLETE LOCAL SETUP GUIDE

## Step-by-Step Guide to Set Up E-Commerce Microservices System on Your Local Machine

---

## 📋 Table of Contents

1. [Prerequisites Installation](#1-prerequisites-installation)
2. [Download & Extract Project](#2-download--extract-project)
3. [Infrastructure Setup](#3-infrastructure-setup)
4. [Database Setup](#4-database-setup)
5. [Service Startup](#5-service-startup)
6. [Verification](#6-verification)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Prerequisites Installation

### Required Software:

#### 1.1 Install Java 21 JDK

**Windows:**
```
1. Download: https://www.oracle.com/java/technologies/downloads/#java21
2. Run installer
3. Set JAVA_HOME environment variable
4. Add to PATH: %JAVA_HOME%\bin
```

**Mac:**
```bash
brew install openjdk@21
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Verify:**
```bash
java -version
# Should show: openjdk version "21.0.x"
```

#### 1.2 Install Maven

**Windows:**
```
1. Download: https://maven.apache.org/download.cgi
2. Extract to C:\Program Files\Apache\maven
3. Add to PATH: C:\Program Files\Apache\maven\bin
```

**Mac:**
```bash
brew install maven
```

**Linux:**
```bash
sudo apt install maven
```

**Verify:**
```bash
mvn -version
# Should show: Apache Maven 3.8.x or higher
```

#### 1.3 Install PostgreSQL

**Windows:**
```
1. Download: https://www.postgresql.org/download/windows/
2. Run installer
3. Set password for 'postgres' user
4. Port: 5432 (default)
```

**Mac:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Linux:**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**Verify:**
```bash
psql --version
# Should show: psql (PostgreSQL) 15.x or 16.x
```

**Set Password:**
```bash
# Login to PostgreSQL
sudo -u postgres psql

# Set password
ALTER USER postgres PASSWORD 'postgres';

# Exit
\q
```

#### 1.4 Install Redis

**Windows:**
```
1. Download: https://github.com/microsoftarchive/redis/releases
2. Extract and run redis-server.exe
OR
Use WSL and follow Linux instructions
```

**Mac:**
```bash
brew install redis
brew services start redis
```

**Linux:**
```bash
sudo apt install redis-server
sudo systemctl start redis
```

**Verify:**
```bash
redis-cli ping
# Should respond: PONG
```

#### 1.5 Install RabbitMQ

**Windows:**
```
1. Install Erlang: https://www.erlang.org/downloads
2. Download RabbitMQ: https://www.rabbitmq.com/download.html
3. Run installer
4. Enable Management Plugin:
   rabbitmq-plugins enable rabbitmq_management
```

**Mac:**
```bash
brew install rabbitmq
brew services start rabbitmq
```

**Linux:**
```bash
sudo apt install rabbitmq-server
sudo systemctl start rabbitmq-server
sudo rabbitmq-plugins enable rabbitmq_management
```

**Verify:**
```bash
rabbitmqctl status
# Should show: Status of node rabbit@...

# Access Management UI
# http://localhost:15672
# Username: guest
# Password: guest
```

#### 1.6 Install curl (for testing)

**Windows:**
```
Already included in Windows 10+
```

**Mac/Linux:**
```bash
# Usually pre-installed, verify:
curl --version
```

#### 1.7 Install Git (optional but recommended)

**Windows:**
```
Download: https://git-scm.com/download/win
```

**Mac:**
```bash
brew install git
```

**Linux:**
```bash
sudo apt install git
```

---

## 2. Download & Extract Project

### Option 1: Download ZIP

1. **Download the ZIP file:** `ecommerce-system.zip`

2. **Extract:**
   - Windows: Right-click → Extract All
   - Mac/Linux: `unzip ecommerce-system.zip`

3. **Navigate to project:**
   ```bash
   cd ecommerce-system
   ```

### Option 2: Using Git (if available)

```bash
# If you have the project in a Git repository
git clone <repository-url>
cd ecommerce-system
```

### Project Structure:

```
ecommerce-system/
├── pom.xml                    # Parent POM
├── auth-service/
├── product-service/
├── cart-service/
├── order-service/
├── payment-service/
├── notification-service/
├── api-gateway/
└── docs/
```

---

## 3. Infrastructure Setup

### 3.1 Verify PostgreSQL is Running

**Check Status:**
```bash
# Windows
pg_ctl status

# Mac
brew services list | grep postgresql

# Linux
sudo systemctl status postgresql
```

**Start if not running:**
```bash
# Windows
pg_ctl start

# Mac
brew services start postgresql@16

# Linux
sudo systemctl start postgresql
```

### 3.2 Verify Redis is Running

**Check Status:**
```bash
redis-cli ping
# Should respond: PONG
```

**Start if not running:**
```bash
# Windows
redis-server

# Mac
brew services start redis

# Linux
sudo systemctl start redis
```

### 3.3 Verify RabbitMQ is Running

**Check Status:**
```bash
rabbitmqctl status
```

**Start if not running:**
```bash
# Windows
rabbitmq-server

# Mac
brew services start rabbitmq

# Linux
sudo systemctl start rabbitmq-server
```

**Access Management UI:**
```
http://localhost:15672
Username: guest
Password: guest
```

---

## 4. Database Setup

### 4.1 Create All Databases

**Method 1: Using psql command line**

```bash
# Login to PostgreSQL
psql -U postgres

# Enter password: postgres

# Create databases
CREATE DATABASE ecommerce_auth;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_order;
CREATE DATABASE ecommerce_payment;
CREATE DATABASE ecommerce_notification;

# Verify databases created
\l

# Exit
\q
```

**Method 2: Using single command**

```bash
psql -U postgres -c "CREATE DATABASE ecommerce_auth;"
psql -U postgres -c "CREATE DATABASE ecommerce_product;"
psql -U postgres -c "CREATE DATABASE ecommerce_order;"
psql -U postgres -c "CREATE DATABASE ecommerce_payment;"
psql -U postgres -c "CREATE DATABASE ecommerce_notification;"
```

**Method 3: Using SQL file**

Create a file `setup-databases.sql`:
```sql
CREATE DATABASE ecommerce_auth;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_order;
CREATE DATABASE ecommerce_payment;
CREATE DATABASE ecommerce_notification;
```

Execute:
```bash
psql -U postgres -f setup-databases.sql
```

### 4.2 Verify Databases

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

### 4.3 Database Configuration

All services are pre-configured with:
- **Host:** localhost
- **Port:** 5432
- **Username:** postgres
- **Password:** postgres

**If you used different credentials**, update `application.yml` in each service:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

---

## 5. Service Startup

### 5.1 Build Parent Project (Optional)

```bash
# From project root
cd ecommerce-system
mvn clean install -DskipTests
```

This builds all services at once.

### 5.2 Start Services in Order

**IMPORTANT:** Start services in this order for dependencies to work properly.

#### Terminal 1: Auth Service (Port 8081)

```bash
cd auth-service
mvn clean spring-boot:run
```

**Wait for:**
```
Started AuthServiceApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8081/api/auth/health
# Should respond: Auth Service is running!
```

#### Terminal 2: Product Service (Port 8082)

```bash
cd product-service
mvn clean spring-boot:run
```

**Wait for:**
```
Started ProductServiceApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8082/api/products/health
# Should respond: Product Service is running!
```

#### Terminal 3: Cart Service (Port 8083)

```bash
cd cart-service
mvn clean spring-boot:run
```

**Wait for:**
```
Started CartServiceApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8083/api/cart/health
# Should respond: Cart Service is running!
```

#### Terminal 4: Order Service (Port 8084)

```bash
cd order-service
mvn clean spring-boot:run
```

**Wait for:**
```
Started OrderServiceApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8084/api/orders/health
# Should respond: Order Service is running!
```

#### Terminal 5: Payment Service (Port 8085)

```bash
cd payment-service
mvn clean spring-boot:run
```

**Wait for:**
```
Started PaymentServiceApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8085/api/payments/health
# Should respond: Payment Service is running!
```

#### Terminal 6: Notification Service (Port 8086)

```bash
cd notification-service
mvn clean spring-boot:run
```

**Wait for:**
```
Started NotificationServiceApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8086/api/notifications/health
# Should respond: Notification Service is running!
```

#### Terminal 7: API Gateway (Port 8080)

```bash
cd api-gateway
mvn clean spring-boot:run
```

**Wait for:**
```
Started ApiGatewayApplication in X.XXX seconds
```

**Test:**
```bash
curl http://localhost:8080/
# Should respond with JSON containing service info
```

---

## 6. Verification

### 6.1 Quick Health Check Script

Create a file `check-services.sh` (Mac/Linux) or `check-services.bat` (Windows):

**Mac/Linux:**
```bash
#!/bin/bash
echo "Checking all services..."
echo "1. Auth Service (8081):"
curl -s http://localhost:8081/api/auth/health
echo -e "\n2. Product Service (8082):"
curl -s http://localhost:8082/api/products/health
echo -e "\n3. Cart Service (8083):"
curl -s http://localhost:8083/api/cart/health
echo -e "\n4. Order Service (8084):"
curl -s http://localhost:8084/api/orders/health
echo -e "\n5. Payment Service (8085):"
curl -s http://localhost:8085/api/payments/health
echo -e "\n6. Notification Service (8086):"
curl -s http://localhost:8086/api/notifications/health
echo -e "\n7. API Gateway (8080):"
curl -s http://localhost:8080/health
echo -e "\nAll services checked!"
```

Run:
```bash
chmod +x check-services.sh
./check-services.sh
```

### 6.2 Check Infrastructure

**PostgreSQL:**
```bash
psql -U postgres -l | grep ecommerce
# Should show 5 databases
```

**Redis:**
```bash
redis-cli ping
# Should respond: PONG
```

**RabbitMQ:**
```bash
# Check queues (should be empty initially)
rabbitmqadmin list queues
```

### 6.3 Access Management Interfaces

**RabbitMQ Management UI:**
```
http://localhost:15672
Username: guest
Password: guest
```

**PostgreSQL (via psql):**
```bash
psql -U postgres -d ecommerce_auth
```

---

## 7. Troubleshooting

### Common Issues:

#### Issue 1: Port Already in Use

**Error:**
```
Port 8081 is already in use
```

**Solution:**
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8081
kill -9 <PID>
```

#### Issue 2: Database Connection Failed

**Error:**
```
Connection to localhost:5432 refused
```

**Solution:**
```bash
# Check if PostgreSQL is running
# Windows
pg_ctl status

# Mac/Linux
sudo systemctl status postgresql

# Start PostgreSQL
# Windows
pg_ctl start

# Mac
brew services start postgresql@16

# Linux
sudo systemctl start postgresql
```

#### Issue 3: Redis Connection Failed

**Error:**
```
Unable to connect to Redis at localhost:6379
```

**Solution:**
```bash
# Check if Redis is running
redis-cli ping

# If no response, start Redis
# Windows
redis-server

# Mac
brew services start redis

# Linux
sudo systemctl start redis
```

#### Issue 4: RabbitMQ Connection Failed

**Error:**
```
Connection refused: localhost:5672
```

**Solution:**
```bash
# Check RabbitMQ status
rabbitmqctl status

# Start RabbitMQ
# Windows
rabbitmq-server

# Mac
brew services start rabbitmq

# Linux
sudo systemctl start rabbitmq-server
```

#### Issue 5: Maven Build Failed

**Error:**
```
Failed to execute goal
```

**Solution:**
```bash
# Clean and rebuild
mvn clean install -U -DskipTests

# If still fails, delete .m2 repository
# Windows
rmdir /s %USERPROFILE%\.m2\repository

# Mac/Linux
rm -rf ~/.m2/repository

# Then rebuild
mvn clean install
```

#### Issue 6: Java Version Mismatch

**Error:**
```
Unsupported class file major version
```

**Solution:**
```bash
# Check Java version
java -version

# Should be Java 21
# If not, install Java 21 and set JAVA_HOME
```

#### Issue 7: Tables Not Created

**Error:**
```
Table "users" doesn't exist
```

**Solution:**
- Hibernate auto-creates tables on first run
- Restart the service
- Check `application.yml` has `ddl-auto: update`

#### Issue 8: JWT Token Issues

**Error:**
```
Invalid JWT token
```

**Solution:**
- Make sure all services have the same JWT secret
- Check `application.yml` in auth, product, cart, order services
- All should have: `secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`

---

## 📚 Next Steps

Once all services are running:

1. **Follow Testing Guide:** See `INDIVIDUAL-SERVICE-TESTING-GUIDE.md`
2. **Run Integration Tests:** See `INTEGRATION-TESTING-GUIDE.md`
3. **Explore API:** See individual service README files
4. **Monitor RabbitMQ:** http://localhost:15672

---

## 🎯 Quick Start Summary

```bash
# 1. Install prerequisites (Java 21, Maven, PostgreSQL, Redis, RabbitMQ)

# 2. Create databases
psql -U postgres -c "CREATE DATABASE ecommerce_auth;"
psql -U postgres -c "CREATE DATABASE ecommerce_product;"
psql -U postgres -c "CREATE DATABASE ecommerce_order;"
psql -U postgres -c "CREATE DATABASE ecommerce_payment;"
psql -U postgres -c "CREATE DATABASE ecommerce_notification;"

# 3. Start infrastructure
redis-server &
# RabbitMQ should already be running

# 4. Start services (in separate terminals)
cd auth-service && mvn spring-boot:run          # Terminal 1
cd product-service && mvn spring-boot:run       # Terminal 2
cd cart-service && mvn spring-boot:run          # Terminal 3
cd order-service && mvn spring-boot:run         # Terminal 4
cd payment-service && mvn spring-boot:run       # Terminal 5
cd notification-service && mvn spring-boot:run  # Terminal 6
cd api-gateway && mvn spring-boot:run           # Terminal 7

# 5. Test
curl http://localhost:8080/
```

---

## 🆘 Getting Help

**Check Logs:**
- Each service outputs logs to console
- Look for errors in red
- Check for "Started ...Application" messages

**Common Log Locations:**
- Console output (Terminal)
- `target/` folder in each service

**Verify Services:**
```bash
# Check all services at once
curl http://localhost:8080/actuator/gateway/routes
```

---

## ✅ Setup Complete!

Once all services show "Started" messages and health checks pass, you're ready to use the system!

**Access the API Gateway:**
```
http://localhost:8080
```

**All requests should go through the API Gateway for proper routing.**

---

**Happy Coding! 🚀**
