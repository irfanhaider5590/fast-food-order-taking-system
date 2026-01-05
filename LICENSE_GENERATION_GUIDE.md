# License Generation Guide

## Overview
This guide explains how to generate licenses for different time intervals (1 month, 3 months, 6 months, 1 year) for your clients.

## License Types Available

1. **TRIAL** - 30 days trial period
2. **MONTHLY** - 1 month (30 days)
3. **QUARTERLY** - 3 months (90 days)
4. **SEMI_ANNUAL** - 6 months (180 days)
5. **ANNUAL** - 1 year (365 days)

## How to Generate License (Admin Only)

### Method 1: Using Settings Screen (Recommended)

1. Login as **ADMIN** user
2. Navigate to **Settings** page
3. Scroll down to **License Management** section
4. You'll see **"Generate New License (Admin Only)"** section
5. Fill in the form:
   - **License Type**: Select from dropdown (TRIAL, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL)
   - **Client Name**: Enter client's name (required)
   - **Client Email**: Enter client's email (optional)
   - **Notes**: Any additional notes (optional)
6. Click **"Generate License"** button
7. The generated license key will be displayed in a success notification
8. Copy the license key and share it with your client

### Method 2: Using API Directly

**Endpoint:** `POST /api/admin/licenses/generate`

**Headers:**
```
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "licenseType": "MONTHLY",
  "clientName": "Client Name",
  "clientEmail": "client@example.com",
  "notes": "Optional notes"
}
```

**Response:**
```json
{
  "licenseKey": "ABCD-EFGH-IJKL-MNOP",
  "licenseType": "MONTHLY",
  "durationDays": 30,
  "clientName": "Client Name",
  "clientEmail": "client@example.com",
  "message": "License generated successfully"
}
```

## License Key Format

License keys are generated in format: `XXXX-XXXX-XXXX-XXXX` (16 characters, 4 groups separated by hyphens)

Example: `A3B7-C9D2-E5F8-G1H4`

## Client Activation Process

1. Client receives license key from you
2. Client logs into the system
3. If license is invalid/expired, client will see a warning
4. Client goes to **Settings** page
5. In **License Management** section, client enters the license key
6. Clicks **"Activate License"**
7. System validates and activates the license
8. License is bound to client's machine (cannot be shared)

## Important Notes

- **Machine Binding**: Each license is bound to a unique machine ID. Once activated, it cannot be transferred to another machine.
- **One License Per Machine**: A machine can only have one active license at a time.
- **License Expiration**: System automatically checks license validity. When expired, all modules except Settings are hidden/disabled.
- **Admin Access**: Only ADMIN role users can generate licenses.
- **License Sharing Prevention**: License keys are unique and bound to specific machines, preventing unauthorized sharing.

## Viewing All Licenses

As an admin, you can view all generated licenses in the Settings page under **"All Licenses"** section. This shows:
- License Key
- License Type
- Client Name
- Status (Active/Inactive)
- Expiration Date

## Troubleshooting

### License Not Generating
- Ensure you're logged in as ADMIN
- Check that all required fields are filled
- Verify API endpoint is accessible

### License Not Activating
- Check license key is correct
- Verify license is not already activated on another machine
- Ensure license is active in database (not deactivated)

### License Expired
- Generate a new license with longer duration
- Client needs to activate the new license key

## Best Practices

1. **Keep Records**: Maintain a record of all generated licenses with client information
2. **Set Appropriate Duration**: Choose license type based on client's subscription plan
3. **Secure Storage**: Store license keys securely and share only with authorized clients
4. **Regular Monitoring**: Check license expiration dates and renew before expiry
5. **Client Communication**: Inform clients before license expiration

