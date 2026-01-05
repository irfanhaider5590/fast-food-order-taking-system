# License Generation API Documentation

## Overview
This API is used to generate licenses for clients. It is **NOT exposed in the UI** and requires a special API key for authentication.

## Endpoint

**POST** `/api/license/generate`

**Note:** If your application uses a context-path (e.g., `/fast-food-order-api`), include it in the URL:
- Full URL: `http://localhost:8080/fast-food-order-api/api/license/generate`

## Authentication

This endpoint requires a special API key passed in the request header:

```
X-License-API-Key: <your-secret-api-key>
```

The API key is configured in `application.yml`:
```yaml
app:
  license:
    generation:
      api-key: ${LICENSE_GENERATION_API_KEY:CHANGE_THIS_SECRET_KEY}
```

**Important:** Change the default API key in production! Set it via environment variable `LICENSE_GENERATION_API_KEY`.

## Request

### Headers
```
Content-Type: application/json
X-License-API-Key: <your-secret-api-key>
```

### Body
```json
{
  "licenseType": "MONTHLY",
  "clientName": "Client Name",
  "clientEmail": "client@example.com",
  "notes": "Optional notes"
}
```

### License Types
- `TRIAL` - 30 days trial
- `MONTHLY` - 1 month (30 days)
- `QUARTERLY` - 3 months (90 days)
- `SEMI_ANNUAL` - 6 months (180 days)
- `ANNUAL` - 1 year (365 days)
- `LIFETIME` - Never expires

## Response

### Success Response (200 OK)
```json
{
  "licenseKey": "A3B7-C9D2-E5F8-G1H4",
  "licenseType": "MONTHLY",
  "durationDays": 30,
  "clientName": "Client Name",
  "clientEmail": "client@example.com",
  "message": "License generated successfully",
  "expiresAt": "Will be set on activation"
}
```

For LIFETIME licenses:
```json
{
  "licenseKey": "X9Y8-Z7W6-V5U4-T3S2",
  "licenseType": "LIFETIME",
  "durationDays": "LIFETIME",
  "clientName": "Client Name",
  "clientEmail": "client@example.com",
  "message": "License generated successfully",
  "expiresAt": "Never"
}
```

### Error Responses

#### Invalid API Key (401 Unauthorized)
```json
{
  "error": "Unauthorized: Invalid API key",
  "code": "INVALID_API_KEY"
}
```

#### Invalid License Type (400 Bad Request)
```json
{
  "error": "Invalid license type: INVALID_TYPE",
  "code": "INVALID_LICENSE_TYPE"
}
```

#### Server Error (500 Internal Server Error)
```json
{
  "error": "Failed to generate license: <error message>",
  "code": "GENERATION_ERROR"
}
```

## Example Usage

### Using cURL

**⚠️ CRITICAL: Use POST method, NOT GET!**

```bash
# Correct command (POST method):
curl -X POST http://localhost:8080/fast-food-order-api/api/license/generate \
  -H "Content-Type: application/json" \
  -H "X-License-API-Key: xxyy123123123" \
  -d '{
    "licenseType": "TRIAL",
    "clientName": "Premium Client",
    "clientEmail": "premium@client.com",
    "notes": "Trial license"
  }'

# Example with ANNUAL license:
curl -X POST http://localhost:8080/fast-food-order-api/api/license/generate \
  -H "Content-Type: application/json" \
  -H "X-License-API-Key: YOUR_SECRET_API_KEY" \
  -d '{
    "licenseType": "ANNUAL",
    "clientName": "ABC Restaurant",
    "clientEmail": "admin@abcrestaurant.com",
    "notes": "Annual subscription for 2024"
  }'

# Example with LIFETIME license:
curl -X POST http://localhost:8080/fast-food-order-api/api/license/generate \
  -H "Content-Type: application/json" \
  -H "X-License-API-Key: YOUR_SECRET_API_KEY" \
  -d '{
    "licenseType": "LIFETIME",
    "clientName": "Premium Client",
    "clientEmail": "premium@client.com",
    "notes": "Lifetime license"
  }'

# If no context-path, use:
curl -X POST http://localhost:8080/api/license/generate \
  -H "Content-Type: application/json" \
  -H "X-License-API-Key: YOUR_SECRET_API_KEY" \
  -d '{
    "licenseType": "ANNUAL",
    "clientName": "ABC Restaurant",
    "clientEmail": "admin@abcrestaurant.com",
    "notes": "Annual subscription for 2024"
  }'
```

### Using Postman
1. Method: POST
2. URL: `http://localhost:8080/fast-food-order-api/api/license/generate` (include context-path if configured)
3. Headers:
   - `Content-Type: application/json`
   - `X-License-API-Key: YOUR_SECRET_API_KEY`
4. Body (raw JSON):
```json
{
  "licenseType": "LIFETIME",
  "clientName": "Premium Client",
  "clientEmail": "premium@client.com",
  "notes": "Lifetime license"
}
```

### Using JavaScript/Node.js
```javascript
const axios = require('axios');

const generateLicense = async () => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/license/generate',
      {
        licenseType: 'QUARTERLY',
        clientName: 'Test Client',
        clientEmail: 'test@client.com',
        notes: 'Test license'
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'X-License-API-Key': 'YOUR_SECRET_API_KEY'
        }
      }
    );
    
    console.log('License Key:', response.data.licenseKey);
    return response.data;
  } catch (error) {
    console.error('Error:', error.response.data);
  }
};
```

## Security Notes

1. **API Key Protection**: Never expose the API key in client-side code or public repositories
2. **Environment Variable**: Always set the API key via environment variable in production
3. **HTTPS**: Use HTTPS in production to protect the API key during transmission
4. **Key Rotation**: Regularly rotate the API key for security
5. **Access Control**: Limit access to this API to authorized personnel only

## License Key Format

License keys are generated in format: `XXXX-XXXX-XXXX-XXXX` (16 characters, 4 groups)

Example: `A3B7-C9D2-E5F8-G1H4`

## Client Activation

After generating a license:
1. Share the license key with the client securely
2. Client logs into the system
3. Client goes to Settings → License Management
4. Client enters the license key and clicks "Activate License"
5. License is bound to client's machine and activated

## Lifetime Licenses

Lifetime licenses (`LIFETIME` type) never expire:
- `expiresAt` is set to `null` in database
- `isExpired()` always returns `false`
- Duration is `Integer.MAX_VALUE` days

## Troubleshooting

### API Key Not Working
- Verify the API key matches exactly (case-sensitive)
- Check environment variable `LICENSE_GENERATION_API_KEY` is set
- Ensure header name is exactly `X-License-API-Key`

### Invalid License Type
- Use one of the valid types: TRIAL, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL, LIFETIME
- Type is case-sensitive

### License Generation Fails
- Check server logs for detailed error messages
- Verify database connection
- Ensure license table exists and is accessible

