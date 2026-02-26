# Quick Start Guide - E-Commerce System

This guide will help you get the Auth Service up and running in 5 minutes!

## Step 1: Verify Prerequisites ✅

Open terminal/command prompt and verify:

```bash
# Check Java version (should be 21)
java -version

# Check Maven
mvn -version

# Check PostgreSQL
psql --version

# Check Redis
redis-cli --version

# Check RabbitMQ (optional for now)
rabbitmqctl version
```

## Step 2: Start Services 🚀

### Start PostgreSQL
PostgreSQL should be running as a service. If not:

**Windows:**
```bash
# Open Services (services.msc) and start PostgreSQL
# Or use pg_ctl
pg_ctl -D "C:\Program Files\PostgreSQL\15\data" start
```

**Linux/Mac:**
```bash
sudo systemctl start postgresql
# or
sudo service postgresql start
```

### Start Redis
```bash
# Windows (if installed)
redis-server

# Linux
sudo systemctl start redis
# or
redis-server

# Mac
brew services start redis
```

Verify Redis is running:
```bash
redis-cli ping
# Should respond: PONG
```

## Step 3: Create Database 💾

```bash
# Login to PostgreSQL
psql -U postgres

# You'll be prompted for password
```

In PostgreSQL prompt:
```sql
-- Create database
CREATE DATABASE ecommerce_auth;

-- Verify
\l

-- Exit
\q
```

## Step 4: Run Auth Service 🎯

```bash
# Navigate to project directory
cd ecommerce-system/auth-service

# Run the application
mvn spring-boot:run
```

**Wait for this message:**
```
Started AuthServiceApplication in X.XXX seconds
```

## Step 5: Test the API 🧪

### Option A: Using Browser
Open: http://localhost:8081/api/auth/health

You should see: `Auth Service is running!`

### Option B: Using cURL

**1. Register a new user:**
```bash
curl -X POST http://localhost:8081/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"test123\",\"role\":\"USER\"}"
```

**2. Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"test123\"}"
```

Copy the `token` from the response!

**3. Get current user (replace YOUR_TOKEN):**
```bash
curl http://localhost:8081/api/auth/me ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Option C: Using Postman (Recommended)

1. Import: `docs/Auth-Service-Postman-Collection.json`
2. Run "Register User" request
3. Copy the token from response
4. Run "Get Current User" and paste token in Authorization header

## Common Issues & Solutions 🔧

### Issue 1: Port 8081 already in use
```bash
# Find process using port 8081
netstat -ano | findstr :8081

# Kill the process (Windows)
taskkill /PID <PID> /F

# Or change port in application.yml
server:
  port: 8082
```

### Issue 2: Database connection failed
- Check PostgreSQL is running
- Verify database exists: `psql -U postgres -l`
- Check credentials in `application.yml`
- Default: username=postgres, password=postgres

### Issue 3: Redis connection failed
- Check Redis is running: `redis-cli ping`
- Redis is not required for Auth Service yet (needed for Cart Service)

### Issue 4: Maven build fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

### Issue 5: "Cannot find symbol" errors
Make sure you have Lombok plugin installed in your IDE:
- **IntelliJ IDEA**: File → Settings → Plugins → Search "Lombok" → Install
- **Eclipse**: Download lombok.jar and run it
- **VS Code**: Install "Lombok Annotations Support" extension

## Next Steps 🎓

1. ✅ Test all Auth Service endpoints
2. 📝 Understand the code structure
3. 🔍 Review the JWT implementation
4. 🚀 Move to Product Service (next tutorial)

## Useful Commands 📝

```bash
# Build without running
mvn clean package

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Check application logs
# Logs are in console by default

# Stop the application
# Press Ctrl+C in terminal
```

## Database Quick Commands 🗄️

```bash
# Connect to database
psql -U postgres -d ecommerce_auth

# View all tables
\dt

# View users table structure
\d users

# View all users
SELECT * FROM users;

# Delete all users (for testing)
DELETE FROM users;

# Exit
\q
```

## What You've Accomplished 🎉

- ✅ Set up development environment
- ✅ Created Auth Service with JWT
- ✅ Tested Registration & Login
- ✅ Understood REST API structure
- ✅ Learned Spring Security basics

## Resume Bullet Points 💼

You can now add:
- "Developed JWT-based authentication service using Spring Boot and Spring Security"
- "Implemented user registration and login with BCrypt password encryption"
- "Created RESTful APIs with proper validation and exception handling"
- "Worked with PostgreSQL for persistent data storage"

## Need Help? 🆘

- Check the main README.md
- Review auth-service/README.md
- Check application logs in console
- Verify all services are running

---

**Congratulations! You've successfully set up and tested the Auth Service! 🎊**

Next: Product Service Development
