-- Initialize stock warning interval configuration
-- Run this script to set up the default warning interval

INSERT INTO brand_config (config_key, config_value, description) VALUES
('STOCK_WARNING_INTERVAL_HOURS', '4', 'Stock warning check interval in hours (default: 4 hours)')
ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    updated_at = CURRENT_TIMESTAMP;

