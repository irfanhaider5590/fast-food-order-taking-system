-- Generate Sample Licenses
-- Run this script to create sample licenses for testing
-- Note: In production, use AdminLicenseController to generate licenses

-- Sample licenses (format: XXXX-XXXX-XXXX-XXXX)
-- You can generate these using the LicenseGenerationService API

-- Example: 1 Month License
INSERT INTO license (license_key, license_type, duration_days, is_active, client_name, client_email, notes)
VALUES ('DEMO-MNTH-2024-0001', 'MONTHLY', 30, true, 'Demo Client', 'demo@example.com', 'Sample 1 month license')
ON CONFLICT (license_key) DO NOTHING;

-- Example: 3 Months License
INSERT INTO license (license_key, license_type, duration_days, is_active, client_name, client_email, notes)
VALUES ('DEMO-QTR-2024-0001', 'QUARTERLY', 90, true, 'Demo Client', 'demo@example.com', 'Sample 3 months license')
ON CONFLICT (license_key) DO NOTHING;

-- Example: 6 Months License
INSERT INTO license (license_key, license_type, duration_days, is_active, client_name, client_email, notes)
VALUES ('DEMO-SEMI-2024-0001', 'SEMI_ANNUAL', 180, true, 'Demo Client', 'demo@example.com', 'Sample 6 months license')
ON CONFLICT (license_key) DO NOTHING;

-- Example: 1 Year License
INSERT INTO license (license_key, license_type, duration_days, is_active, client_name, client_email, notes)
VALUES ('DEMO-ANNUAL-2024-0001', 'ANNUAL', 365, true, 'Demo Client', 'demo@example.com', 'Sample 1 year license')
ON CONFLICT (license_key) DO NOTHING;

-- Example: Trial License (30 days)
INSERT INTO license (license_key, license_type, duration_days, is_active, client_name, client_email, notes)
VALUES ('DEMO-TRIAL-2024-0001', 'TRIAL', 30, true, 'Demo Client', 'demo@example.com', 'Sample trial license')
ON CONFLICT (license_key) DO NOTHING;

-- Note: To generate proper license keys, use the AdminLicenseController API endpoint:
-- POST /api/admin/licenses/generate
-- Body: {
--   "licenseType": "MONTHLY|QUARTERLY|SEMI_ANNUAL|ANNUAL|TRIAL",
--   "clientName": "Client Name",
--   "clientEmail": "client@example.com",
--   "notes": "Optional notes"
-- }

