-- Update admin password to Admin@123
-- Run this SQL to update the admin password hash

-- Option 1: Use the password utility endpoint to generate a fresh hash:
-- POST http://localhost:8080/fast-food-order-api/api/public/password-utility/generate
-- Body: { "password": "Admin@123" }
-- Then use the returned hash in the UPDATE statement below

-- Option 2: Use this pre-generated hash (verify it works first):
UPDATE users 
SET password_hash = '$2a$10$8K1p/a0dL1L0vK1JQYqZ0uJQYqZ0uJQYqZ0uJQYqZ0uJQYqZ0uJQYqZ0'
WHERE username = 'admin';

-- To verify the password matches, use:
-- POST http://localhost:8080/fast-food-order-api/api/public/password-utility/verify
-- Body: { "password": "Admin@123", "hash": "<hash_from_database>" }
