-- Default stock warning settings (missing on DBs that only ran brand seed without stock keys)

INSERT INTO brand_config (config_key, config_value, description) VALUES
('STOCK_WARNING_INTERVAL_HOURS', '2', 'Stock warning check interval in hours'),
('STOCK_ALERTS_ENABLED', 'true', 'Enable/disable stock alerts notifications')
ON CONFLICT (config_key) DO NOTHING;
