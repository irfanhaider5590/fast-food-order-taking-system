-- License Management Schema
-- Run this script to add license management functionality

-- License table
CREATE TABLE IF NOT EXISTS license (
    id BIGSERIAL PRIMARY KEY,
    license_key VARCHAR(255) NOT NULL UNIQUE,
    license_type VARCHAR(20) NOT NULL, -- 'TRIAL', 'MONTHLY', 'QUARTERLY', 'SEMI_ANNUAL', 'ANNUAL'
    duration_days INTEGER NOT NULL,
    activated_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    machine_id VARCHAR(255), -- Unique machine identifier
    client_name VARCHAR(255),
    client_email VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_license_key ON license(license_key);
CREATE INDEX IF NOT EXISTS idx_license_active ON license(is_active);
CREATE INDEX IF NOT EXISTS idx_license_expires ON license(expires_at);
CREATE INDEX IF NOT EXISTS idx_license_machine ON license(machine_id);

COMMENT ON TABLE license IS 'Stores license information and activation details';

-- System activation table (tracks first deployment)
CREATE TABLE IF NOT EXISTS system_activation (
    id BIGSERIAL PRIMARY KEY,
    machine_id VARCHAR(255) NOT NULL UNIQUE,
    first_activation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    license_key VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_check_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_activation_machine ON system_activation(machine_id);
CREATE INDEX IF NOT EXISTS idx_system_activation_active ON system_activation(is_active);

COMMENT ON TABLE system_activation IS 'Tracks first-time system activation and machine binding';

