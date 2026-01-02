# Fix Admin Password

## Problem
The admin password hash in the database might not match "Admin@123".

## Solution

### Option 1: Use the Password Utility Endpoint (Recommended)

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Generate a new password hash:
   ```bash
   curl -X POST http://localhost:8080/fast-food-order-api/api/public/password-utility/generate \
     -H "Content-Type: application/json" \
     -d '{"password":"Admin@123"}'
   ```

3. Copy the `hash` value from the response

4. Update the database:
   ```sql
   UPDATE users 
   SET password_hash = '<hash_from_response>' 
   WHERE username = 'admin';
   ```

### Option 2: Check Application Logs

When the application starts, it will log the password hash. Look for:
```
Password Hash Generator
Password: Admin@123
BCrypt Hash: $2a$10$...
```

Then update the database with that hash.

### Option 3: Verify Current Hash

To verify if your current hash matches "Admin@123":
```bash
curl -X POST http://localhost:8080/fast-food-order-api/api/public/password-utility/verify \
  -H "Content-Type: application/json" \
  -d '{"password":"Admin@123","hash":"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"}'
```

## Default Credentials
- **Username**: `admin`
- **Password**: `Admin@123`

