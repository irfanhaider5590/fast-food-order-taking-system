# License Management System

## Overview
This license management system prevents unauthorized sharing of the application and ensures that the system only runs with valid licenses. The system tracks first-time deployment and enforces license expiration.

## Features

1. **Machine Binding**: Each license is bound to a unique machine ID (generated from system properties)
   - **What is Machine ID?** Machine ID is a unique identifier generated from your computer's system properties (OS name, version, username, hostname)
   - **Why do we need it?** It prevents license sharing between different machines. Once activated on one machine, the license cannot be used on another machine
   - **Format:** UUID format (e.g., `531fdd24-9608-3be3-8a83-90c29ebd236c`)
   - **Security:** This ensures your license is secure and cannot be shared with others
2. **First-Time Activation**: System tracks first deployment and requires license activation
3. **License Types**: 
   - TRIAL (30 days)
   - MONTHLY (30 days)
   - QUARTERLY (90 days)
   - SEMI_ANNUAL (180 days)
   - ANNUAL (365 days)
   - LIFETIME (Never expires)
4. **Automatic Expiration**: System hides modules when license expires (Settings always accessible)
5. **License Validation**: All API requests (except license/settings endpoints) are validated
6. **Expiration Warnings**: Automatic warnings at 15, 10, 5, 4, 3, 2, and 1 days before expiration

## Database Setup

1. Run the license management schema:
```sql
\i database/license-management-schema.sql
```

2. (Optional) Generate sample licenses for testing:
```sql
\i database/generate-sample-licenses.sql
```

## How It Works

### First-Time Deployment
1. When the system runs for the first time, it generates a unique Machine ID based on:
   - Operating System name and version
   - Username
   - Hostname/Computer name
2. System checks if it's activated - if not, user must activate with a license key
3. Once activated, the license is bound to that machine ID

### License Activation Flow
1. Admin generates a license using Admin API: `POST /api/admin/licenses/generate`
2. Client receives the license key
3. Client activates license: `POST /api/license/activate` with license key
4. License is bound to the machine ID
5. License expiration date is calculated based on license type

### License Validation
- All API requests (except `/api/license/**`, `/api/auth/**`, `/api/public/**`) are intercepted
- License validity is checked on every request
- If license is expired or invalid, request is blocked with 403 Forbidden

## API Endpoints

### Public Endpoints (No License Required)
- `GET /api/license/status` - Get current license status
- `POST /api/license/activate` - Activate license with license key
- `GET /api/license/machine-id` - Get machine ID

### Admin Endpoints (Requires ADMIN role)
- `POST /api/admin/licenses/generate` - Generate new license
- `GET /api/admin/licenses` - Get all licenses
- `GET /api/admin/licenses/{id}` - Get license details
- `POST /api/admin/licenses/{id}/activate` - Activate a license
- `POST /api/admin/licenses/{id}/deactivate` - Deactivate a license

## Frontend Integration

### License Activation Component
The license activation component is available at `/license` route. Users can:
- View current license status
- Activate a new license
- See expiration date and days remaining

### Adding License Check to App
Add license validation check in `app.component.ts`:

```typescript
import { LicenseService } from './services/license.service';

// In ngOnInit or constructor
this.licenseService.getLicenseStatus().subscribe({
  next: (status) => {
    if (!status.isValid) {
      // Redirect to license activation page
      this.router.navigate(['/license']);
    }
  }
});
```

## Generating Licenses

### Using Admin API
```bash
POST /api/admin/licenses/generate
Content-Type: application/json
Authorization: Bearer <admin_token>

{
  "licenseType": "MONTHLY",  // or QUARTERLY, SEMI_ANNUAL, ANNUAL, TRIAL
  "clientName": "Client Name",
  "clientEmail": "client@example.com",
  "notes": "Optional notes"
}
```

Response includes the generated license key.

### License Key Format
License keys are generated in format: `XXXX-XXXX-XXXX-XXXX` (16 characters, 4 groups)

## Security Features

1. **Machine Binding**: License cannot be shared between machines
2. **One-Time Activation**: Once activated on a machine, license cannot be moved
3. **Expiration Enforcement**: System automatically blocks access when license expires
4. **Request Interception**: All API requests are validated (except license/auth endpoints)

## Troubleshooting

### License Not Activating
- Check if license key is correct
- Verify license is not already activated on another machine
- Check if license is active in database

### License Expired
- Contact administrator to renew license
- Admin can generate new license with longer duration

### Machine ID Changed
- Machine ID is generated from system properties
- If system properties change significantly, machine ID may change
- Contact administrator to reset license binding

## Database Tables

### `license` Table
- Stores license information
- Tracks activation and expiration
- Binds license to machine ID

### `system_activation` Table
- Tracks first-time system activation
- Stores machine ID and activation date
- Prevents multiple activations on same machine

## Notes

- License validation happens on every API request
- Machine ID is generated once per system and stored
- License expiration is checked against current date/time
- System blocks all functionality when license expires

