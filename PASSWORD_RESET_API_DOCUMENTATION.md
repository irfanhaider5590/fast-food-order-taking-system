# Password Reset API Documentation

## Overview
This API provides endpoints for resetting user passwords. There are three endpoints:
1. **Reset by Username** - Admin can reset any user's password (requires admin authentication)
2. **Reset by User ID** - Admin can reset any user's password by ID (requires admin authentication)
3. **Emergency Admin Reset** - Reset admin password when admin forgets password (requires secret key)

---

## 1. Reset Password by Username

**Endpoint:** `POST /api/auth/password-reset/reset-by-username`

**Authentication:** Required (Admin role)

**Description:** Allows admin users to reset password for any user by username.

### Request Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Request Body
```json
{
  "username": "admin",
  "newPassword": "NewPassword@123"
}
```

### Response (Success)
```json
{
  "success": true,
  "message": "Password reset successfully for user: admin"
}
```

### Response (Error)
```json
{
  "success": false,
  "message": "User not found with username: admin"
}
```

### cURL Example
```bash
curl --location 'http://localhost:8080/fast-food-order-api/api/auth/password-reset/reset-by-username' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "admin",
  "newPassword": "NewPassword@123"
}'
```

---

## 2. Reset Password by User ID

**Endpoint:** `POST /api/auth/password-reset/reset-by-id`

**Authentication:** Required (Admin role)

**Description:** Allows admin users to reset password for any user by user ID.

### Request Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Request Body
```json
{
  "userId": 1,
  "newPassword": "NewPassword@123"
}
```

### Response (Success)
```json
{
  "success": true,
  "message": "Password reset successfully for user ID: 1"
}
```

### Response (Error)
```json
{
  "success": false,
  "message": "User not found with ID: 1"
}
```

### cURL Example
```bash
curl --location 'http://localhost:8080/fast-food-order-api/api/auth/password-reset/reset-by-id' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--header 'Content-Type: application/json' \
--data-raw '{
  "userId": 1,
  "newPassword": "NewPassword@123"
}'
```

---

## 3. Emergency Admin Password Reset

**Endpoint:** `POST /api/auth/password-reset/reset-admin`

**Authentication:** Not required (protected by secret key)

**Description:** Emergency endpoint to reset admin password when admin forgets password. This endpoint does not require authentication but requires a secret key configured in `application.yml`.

### Configuration
Add to `application.yml`:
```yaml
app:
  password:
    reset:
      admin-secret-key: ${ADMIN_PASSWORD_RESET_SECRET_KEY:EMERGENCY_ADMIN_RESET_KEY}
```

Or set environment variable:
```bash
export ADMIN_PASSWORD_RESET_SECRET_KEY=your-secret-key-here
```

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "newPassword": "NewPassword@123",
  "adminSecretKey": "EMERGENCY_ADMIN_RESET_KEY"
}
```

### Response (Success)
```json
{
  "success": true,
  "message": "Admin password reset successfully"
}
```

### Response (Error)
```json
{
  "success": false,
  "message": "Invalid admin secret key"
}
```

### cURL Example
```bash
curl --location 'http://localhost:8080/fast-food-order-api/api/auth/password-reset/reset-admin' \
--header 'Content-Type: application/json' \
--data-raw '{
  "newPassword": "NewPassword@123",
  "adminSecretKey": "EMERGENCY_ADMIN_RESET_KEY"
}'
```

---

## Password Requirements

- Minimum length: 6 characters
- Cannot be empty or null
- Password is automatically hashed using BCrypt before storage

---

## Security Notes

1. **Admin Endpoints** (`/reset-by-username`, `/reset-by-id`):
   - Require JWT authentication
   - Require ADMIN role
   - Log who performed the reset

2. **Emergency Admin Reset** (`/reset-admin`):
   - Does not require authentication (admin forgot password)
   - Protected by secret key configured in `application.yml`
   - **IMPORTANT:** Change the default secret key in production!
   - Only resets password for user with username "admin"

3. **Password Storage**:
   - Passwords are hashed using BCrypt
   - Original passwords are never stored

---

## Existing Password Utility API

The existing API endpoint `/api/public/password-utility/verify` only **verifies** if a password matches a hash. It does NOT reset the password.

**Endpoint:** `POST /api/public/password-utility/verify`

**Body:**
```json
{
  "password": "Admin@123",
  "hash": "$2a$10$xuJrnpRq97PYBg6Hk8HPUeTs21lxkwQUn/v4v8TD9KEs.taeisWOi"
}
```

**Response:**
```json
{
  "matches": true,
  "message": "Password matches!"
}
```

This is useful for testing/verification but does not change the password in the database.

---

## Usage Scenarios

### Scenario 1: Admin wants to reset a user's password
1. Admin logs in
2. Admin calls `/api/auth/password-reset/reset-by-username` or `/reset-by-id`
3. Password is reset

### Scenario 2: Admin forgot password
1. Admin cannot log in
2. Use emergency reset endpoint `/api/auth/password-reset/reset-admin`
3. Provide secret key from `application.yml`
4. Admin password is reset
5. Admin can now log in with new password

---

## Error Messages

- `"User not found with username: <username>"` - Username doesn't exist
- `"User not found with ID: <id>"` - User ID doesn't exist
- `"New password cannot be empty"` - Password is empty
- `"Password must be at least 6 characters long"` - Password too short
- `"Invalid admin secret key"` - Wrong secret key for emergency reset
- `"Admin user not found"` - Admin user doesn't exist (username "admin")

